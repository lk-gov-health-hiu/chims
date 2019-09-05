package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.facade.UploadFacade;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named("uploadController")
@RequestScoped
public class UploadController implements Serializable {

    private Upload current;
    private List<Upload> items = null;
    @EJB
    private lk.gov.health.phsp.facade.UploadFacade ejbFacade;

    @Inject
    private WebUserController webUserController;

    private StreamedContent downloadingFile;

    public void downloadCurrentFile() {

    }

    public StreamedContent getDownloadingFile() {
        current = getWebUserController().getCurrentUpload();
        if (current == null) {
            return null;
        }
        InputStream stream = new ByteArrayInputStream(current.getBaImage());
        downloadingFile = new DefaultStreamedContent(stream, current.getFileType(), current.getFileName());
        return downloadingFile;
    }

    public UploadController() {
    }

    private UploadFacade getFacade() {
        return ejbFacade;
    }

    public Upload getUpload(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    public Upload getCurrent() {
        return current;
    }

    public void setCurrent(Upload current) {
        this.current = current;
    }

    public List<Upload> getItems() {
        return items;
    }

    public void setItems(List<Upload> items) {
        this.items = items;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
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
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Upload.class.getName());
            }
        }

    }

}
