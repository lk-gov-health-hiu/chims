package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.UploadFacade;
import java.io.IOException;
import java.io.InputStream;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Component;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.UploadedFile;

@Named
@SessionScoped
public class UploadController implements Serializable {

    @EJB
    private lk.gov.health.phsp.facade.UploadFacade ejbFacade;
    @Inject
    private WebUserController webUserController;
    private List<Upload> items = null;
    private Upload selected;
    private Component selectedComponent;
    private UploadedFile file;

    public String toUploadComponentUploadSingle() {
        if (selectedComponent == null) {
            JsfUtil.addErrorMessage("No Component");
            return "";
        }
        String j = "select u from Upload u "
                + " where u.retired<>:ret "
                + " and u.component=:com";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("com", selectedComponent);

        selected = getFacade().findFirstByJpql(j, m);
        if (selected == null) {
            selected = new Upload();
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            selected.setComponent(selectedComponent);
            getFacade().create(selected);
        }
        return "/queryComponent/upload_query";
    }

    public UploadController() {
    }

    public Upload getSelected() {
        return selected;
    }

    public void setSelected(Upload selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private UploadFacade getFacade() {
        return ejbFacade;
    }

    public Upload prepareCreate() {
        selected = new Upload();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("UploadCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("UploadUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("UploadDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Upload> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
        return items;
    }
    
    
    public void uploadFile1() {


        if (file == null) {
            
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("Error in Uploading file. No such file");
            return ;
        }

        if (file.getFileName() == null) {
            
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("Error in Uploading file. No such file name.");
            return ;
        }

        if (selected == null) {
            
            JsfUtil.addErrorMessage("No file. Error");
            return ;
        }

        selected.setFileName(file.getFileName());
        selected.setFileType(file.getContentType());


        InputStream in;

        try {
            in = getFile().getInputstream();
            selected.setBaImage(IOUtils.toByteArray(in));
            
        } catch (IOException e) {
            
        }

        if (selected.getId() == null) {
            getFacade().create(selected);
        } else {
            getFacade().edit(selected);
        }

        selected.setFileName(file.getFileName());
        selected.setFileType(file.getContentType());


        getFacade().edit(selected);

        

    }
    

    public String uploadFile() {


        if (file == null) {
            
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("Error in Uploading file. No such file");
            return "";
        }

        if (file.getFileName() == null) {
            
            lk.gov.health.phsp.facade.util.JsfUtil.addErrorMessage("Error in Uploading file. No such file name.");
            return "";
        }

        if (selected == null) {
            
            JsfUtil.addErrorMessage("No file. Error");
            return "/queryComponent/query";
        }

        selected.setFileName(file.getFileName());
        selected.setFileType(file.getContentType());


        InputStream in;

        try {
            in = getFile().getInputstream();
            selected.setBaImage(IOUtils.toByteArray(in));
            
        } catch (IOException e) {
            
        }

        if (selected.getId() == null) {
            getFacade().create(selected);
        } else {
            getFacade().edit(selected);
        }

        selected.setFileName(file.getFileName());
        selected.setFileType(file.getContentType());


        getFacade().edit(selected);

        return "/queryComponent/query";

    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Upload getUpload(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Upload> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Upload> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.UploadFacade getEjbFacade() {
        return ejbFacade;
    }

    public Component getSelectedComponent() {
        return selectedComponent;
    }

    public void setSelectedComponent(Component selectedComponent) {
        this.selectedComponent = selectedComponent;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    @FacesConverter(forClass = Upload.class)
    public static class UploadControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UploadController controller = (UploadController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "uploadController");
            return controller.getUpload(getKey(value));
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
            if (object instanceof Upload) {
                Upload o = (Upload) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Upload.class.getName()});
                return null;
            }
        }

    }

}
