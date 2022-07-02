/*
 * The MIT License
 *
 * Copyright 2022 buddhika.
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

/**
 *
 * @author buddhika
 */
public class ObservationValueCount {
    
    private String stringValue;
    private Long count;
    private Integer intValue;
    private Long longValue;
    private Double realNumberValue;
    
    
    
    

    public ObservationValueCount() {
    }

    public ObservationValueCount(Long count) {
        this.count = count;
    }
    
    

    public ObservationValueCount(Double realNumberValue, Long count) {
        this.count = count;
        this.realNumberValue = realNumberValue;
    }
    
    

    public ObservationValueCount(String stringValue, Long count) {
        this.stringValue = stringValue;
        this.count = count;
    }

    public ObservationValueCount(Integer intValue, Long count) {
        this.count = count;
        this.intValue = intValue;
    }

    public ObservationValueCount(Long longValue, Long count) {
        this.count = count;
        this.longValue = longValue;
    }

    
    
    
    
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Double getRealNumberValue() {
        return realNumberValue;
    }

    public void setRealNumberValue(Double realNumberValue) {
        this.realNumberValue = realNumberValue;
    }
    
    
    
}
