/*
 * The MIT License
 *
 * Copyright 2020 MHB Ariyaratne.
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
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.TimePeriodType;

/**
 *
 * @author chims
 */
public class ReportTimePeriod {

    private TimePeriodType timePeriodType;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Integer dateOfMonth;
    private Date from;
    private Date to;
    private String label;
    private String value;
    private Quarter quarterEnum;
    private String longDateFormat;

    public TimePeriodType getTimePeriodType() {
        if (timePeriodType == null) {
            timePeriodType = TimePeriodType.Dates;
        }
        return timePeriodType;
    }

    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
    }

    public Integer getYear() {
        if (year == null) {
            year = CommonController.getYear(to);
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        if (quarter == null) {
            quarter = CommonController.getQuarter();
        }
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
        if (quarter != null) {
            switch (quarter) {
                case 1:
                    quarterEnum = Quarter.First;
                    break;
                case 2:
                    quarterEnum = Quarter.Second;
                    break;
                case 3:
                    quarterEnum = Quarter.Third;
                    break;
                case 4:
                    quarterEnum = Quarter.Fourth;
                    break;
            }
        }
    }

    public Integer getMonth() {
        if (month == null) {
            month = CommonController.getMonth();
        }
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDateOfMonth() {
        if (dateOfMonth == null) {
            dateOfMonth = CommonController.getDateOfMonth();
        }
        return dateOfMonth;
    }

    public void setDateOfMonth(Integer dateOfMonth) {
        this.dateOfMonth = dateOfMonth;
    }

    public Date getFrom() {
        if (from == null) {
            from = new Date();
        }
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        if (to == null) {
            to = new Date();
        }
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getLabel() {
        switch (getTimePeriodType()) {
            case Dates:
                label = "Period";
                break;
            case Monthly:
                label = "Month";
                break;
            case Quarterly:
                label = "Quarter";
                break;
            case Yearley:
                label = "Year";
                break;
        }
        return label;
    }

    public String getValue() {
        switch (getTimePeriodType()) {
            case Dates:
                value = "From : ";
                value += CommonController.dateTimeToString(getFrom(), getLongDateFormat());
                value += " To: ";
                value += CommonController.dateTimeToString(getTo(), getLongDateFormat());
                break;
            case Monthly:
                value = CommonController.monthString(getMonth());
                break;
            case Quarterly:
                value = CommonController.quarterString(getQuarterEnum());
                break;
            case Yearley:
                value = getYear() + "";
                break;
        }
        return value;
    }

    public Quarter getQuarterEnum() {
        if (quarterEnum == null) {
            switch (getQuarter()) {
                case 1:
                    quarterEnum = Quarter.First;
                    break;
                case 2:
                    quarterEnum = Quarter.Second;
                    break;
                case 3:
                    quarterEnum = Quarter.Third;
                    break;
                case 4:
                    quarterEnum = Quarter.Fourth;
            }
        }
        return quarterEnum;
    }

    public void setQuarterEnum(Quarter quarterEnum) {
        if (quarterEnum != null) {
            switch (quarterEnum) {
                case First:
                    quarter = 1;
                    break;
                case Second:
                    quarter = 2;
                    break;
                case Third:
                    quarter = 3;
                    break;
                case Fourth:
                    quarter = 4;
            }
        }
        this.quarterEnum = quarterEnum;
    }

    public String getLongDateFormat() {
        if (longDateFormat == null || longDateFormat.trim().equals("")) {
            longDateFormat = "dd MMMM yyyy";
        }
        return longDateFormat;
    }

    public void setLongDateFormat(String longDateFormat) {
        this.longDateFormat = longDateFormat;
    }

}
