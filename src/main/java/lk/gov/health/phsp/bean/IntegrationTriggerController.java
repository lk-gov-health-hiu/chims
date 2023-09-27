package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.FhirOperationResult;
import lk.gov.health.phsp.entity.FhirResourceLink;
import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.entity.IntegrationTrigger;
import lk.gov.health.phsp.enums.CommunicationProtocol;
import lk.gov.health.phsp.enums.IntegrationEvent;
import lk.gov.health.phsp.facade.FhirResourceLinkFacade;
import lk.gov.health.phsp.facade.IntegrationTriggerFacade;
import lk.gov.health.phsp.pojcs.SearchQueryData;

/**
 *
 * @author buddh
 */
@Named
@ApplicationScoped
public class IntegrationTriggerController implements Serializable {

    @EJB
    private IntegrationTriggerFacade ejbFacade;
    @EJB
    FhirResourceLinkFacade fhirResourceLinkFacade;
    private List<IntegrationTrigger> items = null;
    private IntegrationTrigger selected;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    FhirR4Controller fhirR4Controller;
//    @Inject
//    FhirR5Controller fhirR5Controller;

    @Resource
    private ManagedExecutorService executorService;

    public CompletableFuture<List<Client>> fetchClientsFromEndpoints(SearchQueryData sqd) {
        if (sqd != null) {
        }
        return CompletableFuture.supplyAsync(() -> {
            List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.PATIENT_SEARCH);
            if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
                return Collections.emptyList();
            }

            List<CompletableFuture<List<Client>>> futureClientsList = itemsNeededToBeTriggered.stream()
                    .filter(it -> it.getIntegrationEndpoint() != null && it.getIntegrationEndpoint().getCommunicationProtocol() != null)
                    .map(it -> {
                        if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                            return fhirR4Controller.fetchClientsFromEndpoints(sqd, it.getIntegrationEndpoint());
                        } else if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R5) {
                            // Handle FHIR R5 if needed
                            return null;
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureClientsList.toArray(new CompletableFuture[0]));

            return allFutures.thenApply(v -> futureClientsList.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList()))
                    .join();
        }, executorService);
    }

    public CompletableFuture<List<FhirOperationResult>> createNewClientsToEndpoints(Client client) {
        return CompletableFuture.supplyAsync(() -> {
            List<FhirOperationResult> outcomes = new ArrayList<>();
            List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.PATIENT_SAVE);
            if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
                return outcomes;
            }
            List<CompletableFuture<FhirOperationResult>> futureOutcomes = new ArrayList<>();
            for (IntegrationTrigger it : itemsNeededToBeTriggered) {
                if (it.getIntegrationEndpoint() == null) {
                    continue;
                }
                if (it.getIntegrationEndpoint().getCommunicationProtocol() == null) {
                    continue;
                }
                CompletableFuture<FhirOperationResult> futureOutcome;
                if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                    String oldId = findFhirResourceLinkId(client, it.getIntegrationEndpoint());
                    if (oldId == null) {
                        futureOutcome = fhirR4Controller.createPatientInFhirServerAsync(client, it.getIntegrationEndpoint());
                    } else {
                        futureOutcome = fhirR4Controller.updatePatientInFhirServerAsync(client, it.getIntegrationEndpoint(),oldId );
                    }
                    futureOutcomes.add(futureOutcome);
                } else if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R5) {
                    // TODO : Handle FHIR R5 
                    continue;
                } else {
                    continue;
                }
            }
            futureOutcomes.forEach(futureOutcome -> futureOutcome.thenAccept(outcomes::add));
            CompletableFuture.allOf(futureOutcomes.toArray(new CompletableFuture[0])).join();
            return outcomes;
        }, executorService).exceptionally(ex -> {
            ex.printStackTrace(); // Or log the exception
            return null; // Or handle the exception as needed
        });
    }

    public String findFhirResourceLinkId(Object object, IntegrationEndpoint endPoint) {
        // Search first for a record, if exists, nothing is needed, if not, create a new record
        String jpql = "select l "
                + " from FhirResourceLink l "
                + " where l.integrationEndpoint = :ep "
                + " and l.objectType = :objectType "
                + " and l.objectId = :objectId"; // Completed JPQL
        Map<String, Object> m = new HashMap<>();
        m.put("ep", endPoint);
        m.put("objectType", object.getClass().getName()); // Assuming you want to search by object's class name
        m.put("objectId", getObjectID(object)); // Assuming you have a method to get the object's ID
        FhirResourceLink l = fhirResourceLinkFacade.findFirstByJpql(jpql, m);
        if (l == null) {
            return null;
        }
        return l.getFhirResourceId();
    }

    private Long getObjectID(Object object) {
        try {
            Method getIdMethod = object.getClass().getMethod("getId");
            return (Long) getIdMethod.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String navigateToAddNew() {
        selected = new IntegrationTrigger();
        return "/systemAdmin/integrationTrigger/edit";
    }

    public String navigateToView() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger to view");
            return "";
        }
        return "/systemAdmin/integrationTrigger/view";
    }

    public String navigateToEdit() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger to edit");
            return "";
        }
        return "/systemAdmin/integrationTrigger/edit";
    }

    public String deleteIntegrationTrigger() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger to delete");
            return "";
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        items = null;
        selected = null;
        return navigateToList();
    }

    public String saveOrUpdateIntegrationTrigger() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an IntegrationTrigger");
            return "";
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        items = null;
        selected = null;
        return navigateToList();
    }

    public String navigateToList() {
        items = fillAllItems();
        return "/systemAdmin/integrationTrigger/list";
    }

    public List<IntegrationEvent> getIntegrationEvents() {
        return Arrays.asList(IntegrationEvent.values());
    }

    public IntegrationTriggerController() {
    }

    public IntegrationTrigger getSelected() {
        return selected;
    }

    public void setSelected(IntegrationTrigger selected) {
        this.selected = selected;
    }

    private IntegrationTriggerFacade getFacade() {
        return ejbFacade;
    }

    public List<IntegrationTrigger> getItems() {
        if (items == null) {
            items = fillAllItems();
        }
        return items;
    }

    private List<IntegrationTrigger> fillAllItems() {
        String jpql = "select i "
                + " from IntegrationTrigger i "
                + " where i.retired=:ret "
                + " order by i.integrationEvent";
        Map m = new HashMap();
        m.put("ret", false);
        return getFacade().findByJpql(jpql, m);
    }

    private List<IntegrationTrigger> fillItems(IntegrationEvent ie) {
        String jpql = "select i "
                + " from IntegrationTrigger i "
                + " where i.retired=:ret "
                + " and i.integrationEvent=:ie "
                + " order by i.integrationEvent";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("ie", ie);
        return getFacade().findByJpql(jpql, m);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = IntegrationTrigger.class)
    public static class IntegrationTriggerControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            IntegrationTriggerController controller = (IntegrationTriggerController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "integrationTriggerController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException e) {
                key = 0l;
            }
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof IntegrationTrigger) {
                IntegrationTrigger o = (IntegrationTrigger) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), IntegrationTrigger.class.getName()});
                return null;
            }
        }

    }
// </editor-fold>

}
