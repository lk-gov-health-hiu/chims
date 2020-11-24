package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Imports">
import lk.gov.health.phsp.entity.Component;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Phn;
import lk.gov.health.phsp.facade.PhnFacade;
// </editor-fold>

@Named
@SessionScoped
public class PhnController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private PhnFacade ejbFacade;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Contollers">
    @Inject
    ApplicationController applicationController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public PhnController() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Functions">
    
    public Phn getPhnById(java.lang.Long id) {
        return getFacade().find(id);
    }
    // </editor-fold>
    
    
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public PhnFacade getFacade() {
        return ejbFacade;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Converter">

    @FacesConverter(forClass = Phn.class)
    public static class PhnControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PhnController controller = (PhnController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "phnController");
            return controller.getPhnById(getKey(value));
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
            if (object instanceof Phn) {
                Phn o = (Phn) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Component.class.getName()});
                return null;
            }
        }

    }
    
    // </editor-fold>

}
