/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.WebUserFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class WebUserApplicationController {

    /*
    EJBs
     */
    @EJB
    private WebUserFacade facade;

    @Inject
    private UserTransactionController userTransactionController;
    private List<WebUser> items = null;
    private List<String> suspiciousIps;
    private List<String> suspiciousUsers;
    private List<SuspeciousIpCount> suspeciousIpCounts;
    private List<SuspeciousUserCount> suspeciousUserCounts;
    private String selectedIp;
    private String selectedUsername;

    private int ipBlockLimit = 5;
    private int userBlockLimit = 3;

    /**
     * Creates a new instance of WebUserApplicationController
     */
    public WebUserApplicationController() {
    }

    public boolean ipBlocked(String ip){
        boolean ipFoundAmongBlocked=false;
        for(String tip:getSuspiciousIps()){
            if(tip.equalsIgnoreCase(ip)){
                ipFoundAmongBlocked=true;
                return ipFoundAmongBlocked;
            }
        }
        return ipFoundAmongBlocked;
    }
    
    public boolean userBlocked(String username){
        boolean userFoundAmongBlocked=false;
        for(String tuser: getSuspiciousUsers()){
            if(tuser.equalsIgnoreCase(username)){
                userFoundAmongBlocked=true;
                return userFoundAmongBlocked;
            }
        }
        return userFoundAmongBlocked;
    }
    
    public void addFailedAttempt(String ip, String username) {
        SuspeciousIpCount ipFound = null;
        for (SuspeciousIpCount bip : getSuspeciousIpCounts()) {
            if (bip.ip.equalsIgnoreCase(ip)) {
                ipFound=bip;
            }
        }
        if(ipFound!=null){
            ipFound.count++;
            if(ipFound.count > ipBlockLimit){
                getSuspiciousIps().add(ip);
                getSuspeciousIpCounts().remove(ipFound);
            }
        }else{
            ipFound = new SuspeciousIpCount();
            ipFound.ip=ip;
            ipFound.count=1;
            getSuspeciousIpCounts().add(ipFound);
        }
        SuspeciousUserCount userFound = null;
        for(SuspeciousUserCount buser:getSuspeciousUserCounts()){
            if(buser.user.equalsIgnoreCase(username)){
                userFound = buser;
            }
        }
        if(userFound!=null){
            userFound.count++;
            if(userFound.count > userBlockLimit){
                getSuspiciousUsers().add(username);
                getSuspeciousUserCounts().remove(userFound);
            }
        }else{
            userFound = new SuspeciousUserCount();
            userFound.user=username;
            userFound.count=1;
            getSuspeciousUserCounts().add(userFound);
        }
    }

    public String toSuspiciousData() {
        return "/systemAdmin/suspicious_data";
    }

    public List<WebUser> getItems() {
        if (items == null) {
            fillWebUsers();
        }
        return items;
    }

    public void setItems(List<WebUser> items) {
        this.items = items;
    }

    private void fillWebUsers() {
        String j = "select u from WebUser u "
                + " where u.retired=false ";
        items = facade.findByJpql(j);
        userTransactionController.recordTransaction("To List All Users");
    }

    public void resetWebUsers() {
        items = null;
    }

    public List<String> getSuspiciousIps() {
        if (suspiciousIps == null) {
            suspiciousIps = new ArrayList<>();
        }
        return suspiciousIps;
    }

    public void setSuspiciousIps(List<String> suspiciousIps) {
        this.suspiciousIps = suspiciousIps;
    }

    public List<String> getSuspiciousUsers() {
        if (suspiciousUsers == null) {
            suspiciousUsers = new ArrayList<>();
        }
        return suspiciousUsers;
    }

    public void setSuspiciousUsers(List<String> suspiciousUsers) {
        this.suspiciousUsers = suspiciousUsers;
    }

    public String getSelectedIp() {
        return selectedIp;
    }

    public void setSelectedIp(String selectedIp) {
        this.selectedIp = selectedIp;
    }

    public String getSelectedUsername() {
        return selectedUsername;
    }

    public void setSelectedUsername(String selectedUsername) {
        this.selectedUsername = selectedUsername;
    }

    public int getIpBlockLimit() {
        return ipBlockLimit;
    }

    public void setIpBlockLimit(int ipBlockLimit) {
        this.ipBlockLimit = ipBlockLimit;
    }

    public int getUserBlockLimit() {
        return userBlockLimit;
    }

    public void setUserBlockLimit(int userBlockLimit) {
        this.userBlockLimit = userBlockLimit;
    }

    public List<SuspeciousIpCount> getSuspeciousIpCounts() {
        if (suspeciousIpCounts == null) {
            suspeciousIpCounts = new ArrayList<>();
        }
        return suspeciousIpCounts;
    }

    public void setSuspeciousIpCounts(List<SuspeciousIpCount> suspeciousIpCounts) {
        this.suspeciousIpCounts = suspeciousIpCounts;
    }

    public List<SuspeciousUserCount> getSuspeciousUserCounts() {
        if (suspeciousUserCounts == null) {
            suspeciousUserCounts = new ArrayList<>();
        }
        return suspeciousUserCounts;
    }

    public void setSuspeciousUserCounts(List<SuspeciousUserCount> suspeciousUserCounts) {
        this.suspeciousUserCounts = suspeciousUserCounts;
    }

    class SuspeciousIpCount {

        String ip;
        int count;

    }

    class SuspeciousUserCount {

        String user;
        int count;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + Objects.hashCode(this.user);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SuspeciousUserCount other = (SuspeciousUserCount) obj;
            if (!Objects.equals(this.user, other.user)) {
                return false;
            }
            return true;
        }

    }

}
