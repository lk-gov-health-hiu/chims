/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.imageio.stream.FileImageOutputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.DataRepresentationType;
import lk.gov.health.phsp.facade.ComponentFacade;
import org.primefaces.event.CaptureEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Buddhika
 */
@Named
@RequestScoped
public class ImageController implements Serializable {

    @Inject
    ItemController itemController;
    @Inject
    ClientEncounterComponentFormSetController clientEncounterComponentFormSetController;
    @EJB
    ComponentFacade componentFacade;

    private List<String> photos = new ArrayList<>();

    private String getRandomImageName() {
        int i = (int) (Math.random() * 10000000);

        return String.valueOf(i);
    }

    public List<String> getPhotos() {
        return photos;
    }

    @Inject
    ClientController clientController;

    public ClientController getClientController() {
        return clientController;
    }

    public StreamedContent getClientPhoto() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        } else {
            if (getClientController().getSelected() == null) {
                return new DefaultStreamedContent();
            }
            ClientEncounterComponentItem dp = clientEncounterComponentFormSetController.fillClientValue(getClientController().getSelected(), "client_default_photo");
            if (dp == null) {
                return new DefaultStreamedContent();
            }
            byte[] p = dp.getByteArrayValue();
            if (p == null) {
                return new DefaultStreamedContent();
            }
//            return new DefaultStreamedContent(new ByteArrayInputStream(p), dp.getShortTextValue(), dp.getLongTextValue());
            return null;
        }
    }

    public void oncapturePatientPhoto(CaptureEvent captureEvent) {
        if (getClientController().getSelected() == null || getClientController().getSelected().getId() == null) {
            JsfUtil.addErrorMessage("Client ?");
            return;
        }

        Item defaultPhoto = itemController.findItemByCode("client_default_photo");
        Item photo = itemController.findItemByCode("client_photo");

        List<ClientEncounterComponentItem> ps = clientEncounterComponentFormSetController.fillClientValues(getClientController().getSelected(), "client_default_photo");
        for (ClientEncounterComponentItem i : ps) {
            i.setItem(photo);
            componentFacade.edit(i);
        }

        ClientEncounterComponentItem ip = new ClientEncounterComponentItem();
        ip.setClient(getClientController().getSelected());
        ip.setClientValue(getClientController().getSelected());
        ip.setItem(defaultPhoto);
        ip.setByteArrayValue(captureEvent.getData());
        ip.setShortTextValue("image/png");
        ip.setLongTextValue("client_image_" + getClientController().getSelected().getId() + ".png");
        ip.setDataRepresentationType(DataRepresentationType.Client);
        componentFacade.create(ip);

        clientController.finishCapturingPhotoWithWebCam();
        
        JsfUtil.addSuccessMessage("Photo captured from webcam.");
    }

    public void oncapture(CaptureEvent captureEvent) {
        String photo = getRandomImageName();
        this.photos.add(0, photo);
        byte[] data = captureEvent.getData();

        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String newFileName = servletContext.getRealPath("") + File.separator + "photocam" + File.separator + photo + ".png";

        FileImageOutputStream imageOutput;
        try {
            imageOutput = new FileImageOutputStream(new File(newFileName));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
        } catch (IOException e) {
            throw new FacesException("Error in writing captured image.");
        }
    }

    /**
     * Creates a new instance of PhotoCamBean
     */
    public ImageController() {
    }

}
