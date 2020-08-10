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
package lk.gov.health.phsp.pojcs;

import java.util.Date;
import javax.persistence.Temporal;

/**
 *
 * @author buddhika
 */
public class ClientEncounterComponentBasicData {
    
    
    private String name;
    private String code;
    
    private String itemCode;

    private String shortTextValue;
    private Integer integerNumberValue;
    private Long longNumberValue;
    private Double realNumberValue;
    private Boolean booleanValue;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateValue;
    private String itemValueCode;

    public ClientEncounterComponentBasicData() {
    }

    public ClientEncounterComponentBasicData(String name, String code, String itemCode, String shortTextValue, Integer integerNumberValue, Long longNumberValue, Double realNumberValue, Boolean booleanValue, Date dateValue, String itemValueCode) {
        this.name = name;
        this.code = code;
        this.itemCode = itemCode;
        this.shortTextValue = shortTextValue;
        this.integerNumberValue = integerNumberValue;
        this.longNumberValue = longNumberValue;
        this.realNumberValue = realNumberValue;
        this.booleanValue = booleanValue;
        this.dateValue = dateValue;
        this.itemValueCode = itemValueCode;
    }
    
    public ClientEncounterComponentBasicData(String name, String code, String shortTextValue, Integer integerNumberValue, Long longNumberValue, Double realNumberValue, Boolean booleanValue, Date dateValue, String itemValueCode) {
        this.name = name;
        this.code = code;
        this.shortTextValue = shortTextValue;
        this.integerNumberValue = integerNumberValue;
        this.longNumberValue = longNumberValue;
        this.realNumberValue = realNumberValue;
        this.booleanValue = booleanValue;
        this.dateValue = dateValue;
        this.itemValueCode = itemValueCode;
    }
    
    public ClientEncounterComponentBasicData(String name, String code, String itemCode, String shortTextValue, Integer integerNumberValue, Long longNumberValue, Double realNumberValue, Boolean booleanValue, Date dateValue) {
        this.name = name;
        this.code = code;
        this.itemCode = itemCode;
        this.shortTextValue = shortTextValue;
        this.integerNumberValue = integerNumberValue;
        this.longNumberValue = longNumberValue;
        this.realNumberValue = realNumberValue;
        this.booleanValue = booleanValue;
        this.dateValue = dateValue;
    }
    
    public ClientEncounterComponentBasicData(String name, String code, String shortTextValue, Integer integerNumberValue, Long longNumberValue, Double realNumberValue, Boolean booleanValue, Date dateValue) {
        this.name = name;
        this.code = code;
        this.shortTextValue = shortTextValue;
        this.integerNumberValue = integerNumberValue;
        this.longNumberValue = longNumberValue;
        this.realNumberValue = realNumberValue;
        this.booleanValue = booleanValue;
        this.dateValue = dateValue;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShortTextValue() {
        return shortTextValue;
    }

    public void setShortTextValue(String shortTextValue) {
        this.shortTextValue = shortTextValue;
    }

    public Integer getIntegerNumberValue() {
        return integerNumberValue;
    }

    public void setIntegerNumberValue(Integer integerNumberValue) {
        this.integerNumberValue = integerNumberValue;
    }

    public Long getLongNumberValue() {
        return longNumberValue;
    }

    public void setLongNumberValue(Long longNumberValue) {
        this.longNumberValue = longNumberValue;
    }

    public Double getRealNumberValue() {
        return realNumberValue;
    }

    public void setRealNumberValue(Double realNumberValue) {
        this.realNumberValue = realNumberValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }



    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemValueCode() {
        return itemValueCode;
    }

    public void setItemValueCode(String itemValueCode) {
        this.itemValueCode = itemValueCode;
    }
    
    
    
    
    
}
