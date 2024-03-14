package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
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
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.FhirOperationResult;
import lk.gov.health.phsp.entity.FhirResourceLink;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.IntegrationEndpoint;
import lk.gov.health.phsp.entity.IntegrationTrigger;
import lk.gov.health.phsp.enums.CommunicationProtocol;
import lk.gov.health.phsp.enums.IntegrationEvent;
import lk.gov.health.phsp.facade.FhirResourceLinkFacade;
import lk.gov.health.phsp.facade.IntegrationTriggerFacade;
import lk.gov.health.phsp.pojcs.SearchQueryData;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public List<Client> fetchClientsFromEndpoints(SearchQueryData sqd) {
        if (sqd != null) {
        }

        List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.PATIENT_SEARCH);
        if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<Client>> clientsList = new ArrayList<>();
        for (IntegrationTrigger it : itemsNeededToBeTriggered) {
            if (it.getIntegrationEndpoint() != null && it.getIntegrationEndpoint().getCommunicationProtocol() != null) {
                if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                    List<Client> clients = fhirR4Controller.fetchClientsFromEndpoints(sqd, it.getIntegrationEndpoint());
                    if (clients != null) {
                        clientsList.add(clients);
                    }
                } else if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R5) {
                    // Handle FHIR R5 if needed
                }
                // Other protocols could be handled here
            }
        }

        return clientsList.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<ServiceRequest> fetchServiceRequestsFromEndpoints() {
        List<IntegrationTrigger> itemsToTrigger = fillItems(IntegrationEvent.SERVICE_REQUEST_SEARCH);
        if (itemsToTrigger == null || itemsToTrigger.isEmpty()) {
            return Collections.emptyList();
        }

        List<ServiceRequest> serviceRequests = new ArrayList<>();
        for (IntegrationTrigger trigger : itemsToTrigger) {
            IntegrationEndpoint endpoint = trigger.getIntegrationEndpoint();
            if (endpoint != null && endpoint.getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                serviceRequests.addAll(fhirR4Controller.fetchServiceRequestsFromEndpoints(endpoint));
            }
            // Additional communication protocols can be handled here if necessary
        }
        return serviceRequests;
    }

    public List<FhirOperationResult> createNewClientsToEndpoints(Client client) {
        List<FhirOperationResult> outcomes = new ArrayList<>();
        List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.PATIENT_SAVE);
        if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
            return outcomes;
        }

        for (IntegrationTrigger it : itemsNeededToBeTriggered) {
            if (it.getIntegrationEndpoint() == null || it.getIntegrationEndpoint().getCommunicationProtocol() == null) {
                continue;
            }

            FhirOperationResult outcome;
            if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                String oldId = findFhirResourceLinkId(client, it.getIntegrationEndpoint());
                if (oldId == null) {
                    outcome = fhirR4Controller.createPatientInFhirServer(client, it.getIntegrationEndpoint());
                } else {
                    outcome = fhirR4Controller.updatePatientInFhirServer(client, it.getIntegrationEndpoint(), oldId);
                }
                outcomes.add(outcome);
            } else if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R5) {
                // TODO: Handle FHIR R5
                continue;
            } else {
                continue;
            }
        }

        return outcomes;
    }

    
    
    
    
    
    
    
    public String formatDateForFhir(Date date) {
        SimpleDateFormat fhirDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return fhirDateFormat.format(date);
    }

    public List<FhirOperationResult> updateServiceRequestInFhirServer(ServiceRequest sr) {
        List<FhirOperationResult> outcomes = new ArrayList<>();
        List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.SERVICE_REQUEST_UPDATE);
        if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
            return outcomes;
        }

        for (IntegrationTrigger it : itemsNeededToBeTriggered) {
            if (it.getIntegrationEndpoint() == null || it.getIntegrationEndpoint().getCommunicationProtocol() == null) {
                continue;
            }

            FhirOperationResult outcome;
            if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
//                String oldId = findFhirResourceLinkId(sr, it.getIntegrationEndpoint());
                outcome = fhirR4Controller.updateServiceRequestInFhirServer(sr, it.getIntegrationEndpoint());
//                if (oldId == null) {
//                    outcome = fhirR4Controller.updateServiceRequestInFhirServer(sr, it.getIntegrationEndpoint());
//                } else {
//                    outcome = fhirR4Controller.updateServiceRequestInFhirServer(sr, it.getIntegrationEndpoint(), oldId);
//                }
                outcomes.add(outcome);
            } else if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R5) {
                // TODO: Handle FHIR R5
                continue;
            } else {
                continue;
            }
        }

        return outcomes;
    }

    public List<FhirOperationResult> postToMediators(String jsonPlayLoad) {
        List<FhirOperationResult> outcomes = new ArrayList<>();
        List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.MEDIATORS);

        if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
            return outcomes;
        }

        for (IntegrationTrigger it : itemsNeededToBeTriggered) {
            if (it.getIntegrationEndpoint() == null) {
                continue;
            }
            if (it.getIntegrationEndpoint().getCommunicationProtocol() == null) {
                continue;
            }

            if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                FhirOperationResult outcome;
                outcome = fhirR4Controller.postJsonPayloadToFhirServer(jsonPlayLoad, it.getIntegrationEndpoint());
                outcomes.add(outcome);
            }
        }
        return outcomes;
    }

    // Modified by Dr M H B Ariyaratne with assistance from ChatGPT from OpenAI
    public List<FhirOperationResult> createNewFormsetToEndpoints(ClientEncounterComponentFormSet cecf) {
        List<FhirOperationResult> outcomes = new ArrayList<>();
        List<IntegrationTrigger> itemsNeededToBeTriggered = fillItems(IntegrationEvent.ENCOUNTER_SAVE);

        if (itemsNeededToBeTriggered == null || itemsNeededToBeTriggered.isEmpty()) {
            return outcomes;
        }


        for (IntegrationTrigger it : itemsNeededToBeTriggered) {

            if (it.getIntegrationEndpoint() == null) {
                continue;
            }


            if (it.getIntegrationEndpoint().getCommunicationProtocol() == null) {
                continue;
            }

            FhirOperationResult outcome;
            if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R4) {
                String oldId = findFhirResourceLinkId(cecf, it.getIntegrationEndpoint());

                if (oldId == null) {
                    outcome = fhirR4Controller.createFormsetInFhirServer(cecf, it.getIntegrationEndpoint());
                } else {
                    outcome = fhirR4Controller.updateFormsetInFhirServerAsync(cecf, it.getIntegrationEndpoint(), oldId);
                }

                outcomes.add(outcome);

            } else if (it.getIntegrationEndpoint().getCommunicationProtocol() == CommunicationProtocol.FHIR_R5) {
                // TODO: Handle FHIR R5 
                continue;
            } else {
                continue;
            }
        }

        return outcomes;
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
