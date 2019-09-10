/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Institution;

import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@Named("institutionController")
@SessionScoped
public class InstitutionController implements Serializable {

    /*
    EJBs
     */
    @EJB
    private lk.gov.health.phsp.facade.InstitutionFacade ejbFacade;

    /*
    Class Variables
     */
    private Institution current;
    private List<Institution> items = null;
    private List<Institution> providers = null;
    private List<Institution> clients = null;

    public InstitutionController() {
    }

    public List<Institution> getInstitutions(InstitutionType type) {
        Map m = new HashMap();
        String j = "Select i from Institution i where "
                + " i.retired=false ";

        if (type != null) {
            m.put("t", type);
            j += " and i.institutionType=:t ";
        }

        j += " order by i.name";

        return getFacade().findBySQL(j, m);
    }

    public List<Institution> completeInstitutions(String qry) {
        String j = "Select i from Institution i where "
                + " i.retired=false "
                + " and lower(i.name) like :t "
                + " order by i.name";
        Map m = new HashMap();
        m.put("t", "%" + qry.trim().toLowerCase() + "%");
        return getFacade().findBySQL(j, m);
    }

    private InstitutionFacade getFacade() {
        return ejbFacade;
    }

    public String prepareListOfProviders() {
        recreateModel();
        return "/institution/Manage";
    }

    public String prepareListOfClients() {
        recreateModel();
        return "/client/Manage";
    }

    public String prepareView() {
        return "View";
    }

    public String prepareCreate() {
        current = new Institution();
        return "Create";
    }

    public String createProvider() {
        try {
            current.setInstitutionType(InstitutionType.Provincial_Department_of_Health_Services);
            getFacade().create(current);
            JsfUtil.addSuccessMessage(("InstitutionCreated"));
            return prepareListOfProviders();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String createClient() {
        try {
            current.setInstitutionType(InstitutionType.Regional_Department_of_Health_Department);
            getFacade().create(current);
            JsfUtil.addSuccessMessage(("InstitutionCreated"));
            return prepareListOfClients();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        return "Edit";
    }

    public String prepareEditCompany() {
        String j = "Select i from Institution i where "
                + " i.retired=false "
                + " and i.institutionType=:t "
                + " order by i.name";
        Map m = new HashMap();
        m.put("t", InstitutionType.Ministry_of_Health);
        current = getFacade().findFirstBySQL(j, m);
        if (current == null) {
            current = new Institution();
            current.setInstitutionType(InstitutionType.Ministry_of_Health);
            current.setName("");
            getFacade().create(current);
        }
        return "/institution/Company";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("InstitutionUpdated"));
            return "Manage";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String updateCompany() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("InstitutionUpdated"));
            return "/system_management";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        performDestroy();
        recreateModel();
        return "Manage";
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(("InstitutionDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
        }
    }

    public List<Institution> getItems() {
        if (items == null) {
            items = getInstitutions(null);
        }
        return items;
    }

    private void recreateModel() {
        items = null;
        providers = null;
        clients = null;
    }

    public List<Institution> getItemsAvailableSelectMany() {
        return ejbFacade.findAll();
    }

    public List<Institution> getItemsAvailableSelectOne() {
        return ejbFacade.findAll();
    }

    public Institution getInstitution(java.lang.Long id) {
        return ejbFacade.find(id);
    }
    
    
    public Institution getInstitution(String name, InstitutionType type, boolean createNew){
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Institution a "
                + " where (upper(a.name) =:n)  ";
        if(type!=null){
            j+= " and a.institutionType=:t ";
            m.put("t", type);
        }
        m.put("n", name.toUpperCase());
        Institution ti = getFacade().findFirstBySQL(j, m);
        if(createNew==true && ti==null){
            ti = new Institution();
            ti.setName(name);
            ti.setCreatedAt(new Date());
            ti.setInstitutionType(type);
            getFacade().create(ti);
        }
        return ti ;
    }

    public Institution getCurrent() {
        return current;
    }

    public void setCurrent(Institution current) {
        this.current = current;
    }

    public List<Institution> getProviders() {
        if (providers == null) {
            providers = getInstitutions(InstitutionType.Provincial_Department_of_Health_Services);
        }
        return providers;
    }

    public void setProviders(List<Institution> providers) {
        this.providers = providers;
    }

    public List<Institution> getClients() {
        if (clients == null) {
            clients = getInstitutions(InstitutionType.Regional_Department_of_Health_Department);
        }
        return clients;
    }

    public void setClients(List<Institution> clients) {
        this.clients = clients;
    }

    @FacesConverter(forClass = Institution.class)
    public static class InstitutionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getInstitution(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
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
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Institution.class.getName());
            }
        }

    }

}
