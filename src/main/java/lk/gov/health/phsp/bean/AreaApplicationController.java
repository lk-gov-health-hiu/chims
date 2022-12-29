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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AreaFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class AreaApplicationController {

    @EJB
    private AreaFacade areaFacade;

    private List<Area> gnAreas;
    private List<Area> allAreas;

    /**
     * Creates a new instance of AreaApplicationController
     */
    public AreaApplicationController() {
    }

    public List<Area> getGnAreas() {
        if (gnAreas == null) {
            gnAreas = getAllGnAreas();
        }
        return gnAreas;
    }

    public void setGnAreas(List<Area> gnAreas) {
        this.gnAreas = gnAreas;
    }

    public List<Area> getAllAreas() {
        if (allAreas == null) {
            allAreas = fillAllAreas();
        }
        return allAreas;
    }
    
    public void reloadAreas(){
        allAreas = null;
        getAllAreas();
    }

    private List<Area> fillAllAreas() {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.retired=:ret ";
        j += " order by a.name";
        m.put("ret", false);
        return areaFacade.findByJpql(j, m);
    }

    public List<Area> getAllGnAreas() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.GN) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllAreas(AreaType at) {
        List<Area> tas = new ArrayList<>();
        if (at != null) {
            for (Area a : getAllAreas()) {
                if (a.getType() != null && a.getType().equals(at)) {
                    tas.add(a);
                }
            }
        } else {
            tas = getAllAreas();
        }
        return tas;
    }

    public List<Area> completeGnAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getGnAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeGnAreas(String qry, Area dsArea) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getGnAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                if (a.getParentArea().equals(dsArea)) {
                    tas.add(a);
                }
            }
        }
        return tas;
    }

    

}
