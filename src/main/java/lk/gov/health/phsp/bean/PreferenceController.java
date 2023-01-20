package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Preference;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.facade.PreferenceFacade;

@Named
@SessionScoped
public class PreferenceController implements Serializable {

    @EJB
    private PreferenceFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    private UserTransactionController userTransactionController;

    private String dictionaryServiceBaseUrl;
    private String dictionaryServiceKey;
    private String facilityRegistryBaseUrl;
    private String facilityRegistryKey;
    private String procedureRoomBaseUrl;
    private String procedureRoomKey;
    private String limsBaseUrl;
    private String limsKey;
    private String pharmacyBaseUrl;
    private String pharmacyKey;
    private String reCAPTCHASiteKey;
    private String reCAPTCHASecreatKey;
    private String logoLink;
    private String footerText;
    private String loginDescreption;
    private String headerText;
    
    

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    private PreferenceFacade getFacade() {
        return ejbFacade;
    }

    @PostConstruct
    public void PreferenceController() {
        loadPreferances();
    }

    public void init() {
        loadPreferances();
    }

    public String findApplicationPreferanceValue(String name) {
        Preference p = findApplicationPreferance(name);
        if (p != null) {
            return p.getLongTextValue();
        } else {
            return null;
        }
    }

    public String toManagePreferences() {
        loadPreferances();
        return "/systemAdmin/preferences";
    }

    public void loadPreferances() {
        reCAPTCHASecreatKey = findApplicationPreferanceValue("reCAPTCHASecreatKey");
        reCAPTCHASiteKey = findApplicationPreferanceValue("reCAPTCHASiteKey");
        
        
        loginDescreption = findApplicationPreferanceValue("loginDescreption");
        logoLink = findApplicationPreferanceValue("logoLink");
        headerText = findApplicationPreferanceValue("headerText");
        footerText = findApplicationPreferanceValue("footerText");
        
    }

    public void savePreferences() {
        savePreference("dictionaryServiceBaseUrl", dictionaryServiceBaseUrl);
        savePreference("dictionaryServiceKey", dictionaryServiceKey);
        savePreference("facilityRegistryBaseUrl", facilityRegistryBaseUrl);
        savePreference("facilityRegistryKey", facilityRegistryKey);
        savePreference("procedureRoomBaseUrl", procedureRoomBaseUrl);
        savePreference("procedureRoomKey", procedureRoomKey);
        savePreference("limsBaseUrl", limsBaseUrl);
        savePreference("limsKey", limsKey);
        savePreference("pharmacyBaseUrl", pharmacyBaseUrl);
        savePreference("pharmacyKey", pharmacyKey);
        savePreference("reCAPTCHASecreatKey", reCAPTCHASecreatKey);
        savePreference("reCAPTCHASiteKey", reCAPTCHASiteKey);
        
        savePreference("logoLink", logoLink);
        savePreference("headerText", headerText);
        savePreference("footerText", footerText);
        savePreference("loginDescreption", loginDescreption);
        
    }

    public Preference findApplicationPreferance(String name) {
        if (name == null) {
            return null;
        }
        String j = "select p "
                + " from Preference p "
                + " where p.applicationPreferance=:ap "
                + " and p.name=:n";
        Map m = new HashMap();
        m.put("ap", true);
        m.put("n", name);
        Preference p = getFacade().findFirstByJpql(j, m);
        if (p == null) {
            p = new Preference();
            p.setApplicationPreferance(true);
            p.setName(name);
            savePreference(p);
        }
        return p;
    }

    public void savePreference(String name, String value) {
        Preference p = findApplicationPreferance(name);
        if (p != null) {
            p.setLongTextValue(value);
            savePreference(p);
        }
    }

    public void savePreference(Preference p) {
        if (p == null) {
            return;
        }
        if (p.getId() == null) {
            p.setCreatedAt(new Date());
            p.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(p);
        } else {
            p.setLastEditBy(webUserController.getLoggedUser());
            p.setLastEditeAt(new Date());
            getFacade().edit(p);
        }
    }

    public String getDictionaryServiceBaseUrl() {
        return dictionaryServiceBaseUrl;
    }

    public void setDictionaryServiceBaseUrl(String dictionaryServiceBaseUrl) {
        this.dictionaryServiceBaseUrl = dictionaryServiceBaseUrl;
    }

    public String getDictionaryServiceKey() {
        return dictionaryServiceKey;
    }

    public void setDictionaryServiceKey(String dictionaryServiceKey) {
        this.dictionaryServiceKey = dictionaryServiceKey;
    }

    public String getFacilityRegistryBaseUrl() {
        return facilityRegistryBaseUrl;
    }

    public void setFacilityRegistryBaseUrl(String facilityRegistryBaseUrl) {
        this.facilityRegistryBaseUrl = facilityRegistryBaseUrl;
    }

    public String getFacilityRegistryKey() {
        return facilityRegistryKey;
    }

    public void setFacilityRegistryKey(String facilityRegistryKey) {
        this.facilityRegistryKey = facilityRegistryKey;
    }

    public String getProcedureRoomBaseUrl() {
        return procedureRoomBaseUrl;
    }

    public void setProcedureRoomBaseUrl(String procedureRoomBaseUrl) {
        this.procedureRoomBaseUrl = procedureRoomBaseUrl;
    }

    public String getProcedureRoomKey() {
        return procedureRoomKey;
    }

    public void setProcedureRoomKey(String procedureRoomKey) {
        this.procedureRoomKey = procedureRoomKey;
    }

    public String getLimsBaseUrl() {
        return limsBaseUrl;
    }

    public void setLimsBaseUrl(String limsBaseUrl) {
        this.limsBaseUrl = limsBaseUrl;
    }

    public String getLimsKey() {
        return limsKey;
    }

    public void setLimsKey(String limsKey) {
        this.limsKey = limsKey;
    }

    public String getPharmacyBaseUrl() {
        return pharmacyBaseUrl;
    }

    public void setPharmacyBaseUrl(String pharmacyBaseUrl) {
        this.pharmacyBaseUrl = pharmacyBaseUrl;
    }

    public String getPharmacyKey() {
        return pharmacyKey;
    }

    public void setPharmacyKey(String pharmacyKey) {
        this.pharmacyKey = pharmacyKey;
    }

    public PreferenceFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PreferenceFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }
    
    

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public String getReCAPTCHASiteKey() {
        return reCAPTCHASiteKey;
    }

    public void setReCAPTCHASiteKey(String reCAPTCHASiteKey) {
        this.reCAPTCHASiteKey = reCAPTCHASiteKey;
    }

    public String getReCAPTCHASecreatKey() {
        return reCAPTCHASecreatKey;
    }

    public void setReCAPTCHASecreatKey(String reCAPTCHASecreatKey) {
        this.reCAPTCHASecreatKey = reCAPTCHASecreatKey;
    }

    public String getLogoLink() {
        return logoLink;
    }

    public void setLogoLink(String logoLink) {
        this.logoLink = logoLink;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public String getLoginDescreption() {
        return loginDescreption;
    }

    public void setLoginDescreption(String loginDescreption) {
        this.loginDescreption = loginDescreption;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Preference.class)
    public static class PreferenceControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PreferenceController controller = (PreferenceController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "preferenceController");
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
            if (object instanceof Preference) {
                Preference o = (Preference) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Preference.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
