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

import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.facade.ItemFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
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
import lk.gov.health.phsp.facade.util.JsfUtil.PersistAction;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named("itemController")
@SessionScoped
public class ItemController implements Serializable {

    @EJB
    private ItemFacade ejbFacade;
    private List<Item> items = null;
    private Item selected;
    private List<Item> sectors = null;
    private List<Item> costUnits = null;
    private List<Item> sourcesOfFunds = null;

    public ItemController() {
    }

    public Item getSelected() {
        return selected;
    }

    public void setSelected(Item selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ItemFacade getFacade() {
        return ejbFacade;
    }

    public Item prepareCreate() {
        selected = new Item();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(JsfUtil.PersistAction.CREATE, "Item Created");
        if (!JsfUtil.isValidationFailed()) {
            items = null;
            sourcesOfFunds = null;
            costUnits = null;
            sectors = null;

        }
    }

    public void update() {
        persist(JsfUtil.PersistAction.UPDATE, "Updated");
    }

    public void destroy() {
        persist(JsfUtil.PersistAction.DELETE, "Deleted");
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;  
            sourcesOfFunds = null;
            costUnits = null;
            sectors = null;
// Invalidate list of items to trigger re-query.
        }
    }

    public List<Item> getItems(ItemType type) {
        String j;
        j = "select i from Item i "
                + " where i.type=:t "
                + " order by i.name";
        Map m = new HashMap();
        m.put("t", type);
        return getFacade().findBySQL(j, m);
    }

    public List<Item> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(JsfUtil.PersistAction persistAction, String successMessage) {
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
                    JsfUtil.addErrorMessage(ex, "Error");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, "Error");
            }
        }
    }

    public Item getItem(java.lang.Long id) {
        return getFacade().find(id);
    }
    
    
    public Item getItem(String name, ItemType type, boolean createNew){
         String j;
        Map m = new HashMap();
        j = "select a "
                + " from Item a "
                + " where (upper(a.name) =:n)  ";
        if(type!=null){
            j+= " and a.type=:t ";
            m.put("t", type);
        }
        m.put("n", name.toUpperCase());
        Item ti = getFacade().findFirstBySQL(j, m);
        if(createNew==true && ti==null){
            ti = new Item();
            ti.setName(name);
            ti.setCreatedAt(new Date());
            ti.setItemType(type);
            getFacade().create(ti);
        }
        return ti ;
    }

    public List<Item> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Item> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public List<Item> getSectors() {
        if(sectors==null){
            sectors = getItems(ItemType.Dictionary_Item);
        }
        return sectors;
    }

    public void setSectors(List<Item> sectors) {
        this.sectors = sectors;
    }

    public List<Item> getCostUnits() {
        if(costUnits==null){
            costUnits = getItems(ItemType.Dictionary_Category);
        }
        return costUnits;
    }

    public void setCostUnits(List<Item> costUnits) {
        this.costUnits = costUnits;
    }

    public List<Item> getSourcesOfFunds() {
        if(sourcesOfFunds==null){
            sourcesOfFunds = getItems(ItemType.Other);
        }
        return sourcesOfFunds;
    }

    public void setSourcesOfFunds(List<Item> sourcesOfFunds) {
        this.sourcesOfFunds = sourcesOfFunds;
    }

    public ItemFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(ItemFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    
    
    
    
    
    @FacesConverter(forClass = Item.class)
    public static class ItemControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getItem(getKey(value));
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
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Item.class.getName()});
                return null;
            }
        }

    }

}
