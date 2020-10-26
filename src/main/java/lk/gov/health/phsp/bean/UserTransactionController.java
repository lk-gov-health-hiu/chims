/*
 * The MIT License
 *
 * Copyright 2020 buddhika.
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

// <editor-fold defaultstate="collapsed" desc="Imports">
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.UserTransaction;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.UserTransactionFacade;
// </editor-fold>

@Named
@SessionScoped
public class UserTransactionController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private UserTransactionFacade facede;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="CDIs">
    @Inject
    private WebUserController webUserController;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private UserTransaction selected;
    private List<UserTransaction> items;
    private Date fromDate;
    private Date toDate;
    private String searchText;
    private String ip;
    private String data;
    private WebUser user;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Constructors">
    public UserTransactionController() {
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Navigation Functions">
    public String toSearchUserTransactions(){
        items = null;
        return "/webUser/transactions";
    }
    
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Main Functions">
    public void clearSearch() {
        fromDate = null;
        toDate = null;
        searchText = null;
        user = null;
        ip = null;
        data = null;
    }

    public void search() {
        String j = "select u "
                + " from UserTrancaction u "
                + " u.transactionStart between :fd and :td ";
        Map m = new HashMap();
        m.put("fd", getFromDate());
        m.put("td", getToDate());

        if (searchText != null && !searchText.trim().equals("")) {
            j += " and lower(u.transactionName)=:t ";
            m.put("t", searchText.trim().toLowerCase());
        }
        if (user != null) {
            j += " and u.webUser=:wu ";
            m.put("wu", user);
        }
        if (ip != null && !ip.trim().equals("")) {
            j += " and u.ipAddress=:ip ";
            m.put("ip", ip.trim());
        }
        if (data != null && !data.trim().equals("")) {
            j += " and u.transactionData=:data ";
            m.put("data", data.trim());
        }
        
        j+=" order by u.id";
        items = getFacede().findByJpql(j, m, 1000);
    }

    public void recordTransaction(String action) {
        recordTransaction(action, "");
    }
    
    public void recordTransaction(String action, String sessionId) {
        UserTransaction t = new UserTransaction();
        t.setTransactionName(action);
        t.setTransactionStart(new Date());
        t.setWebUser(webUserController.getLoggedUser());
        t.setIpAddress(webUserController.getIpAddress());
        t.setTransactionData(sessionId);
        getFacede().create(t);
    }

    public void save(UserTransaction us) {
        if (us == null) {
            return;
        }
        if (us.getId() == null) {
            getFacede().create(us);
        } else {
            getFacede().edit(us);
        }
    }

    public void save() {
        save(selected);
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public UserTransaction getSelected() {
        return selected;
    }

    public void setSelected(UserTransaction selected) {
        this.selected = selected;
    }

    public List<UserTransaction> getItems() {
        return items;
    }

    private UserTransactionFacade getFacede() {
        return facede;
    }

    private WebUserController getWebUserController() {
        return webUserController;
    }

    public void setItems(List<UserTransaction> items) {
        this.items = items;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonController.endOfTheDate();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public WebUser getUser() {
        return user;
    }

    public void setUser(WebUser user) {
        this.user = user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

// </editor-fold>
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
