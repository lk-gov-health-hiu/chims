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
package lk.gov.health.phsp.ejb;

import java.util.Date;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

/**
 *
 * @author buddhika
 */
@Stateless
public class AnalysisBean {

    boolean nowTimeToProcessCounts;

//    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "9-17", dayOfMonth = "*", year = "*", minute = "*", second = "0", persistent = false)
//    public void myTimer() {
//        System.out.println("Timer event: " + new Date());
//    }

    @Schedule(hour = "19-04", dayOfMonth = "*", year = "*", minute = "*/15", second = "0", persistent = false)
    public void startProcessingCounts() {
        nowTimeToProcessCounts = true;
        System.out.println("startProcessingCounts");
    }

    @Schedule(month = "*", hour = "05-18", dayOfMonth = "*", year = "*", minute = "*/15", second = "30", persistent = false)
    public void endProcessingCounts() {
        nowTimeToProcessCounts = false;
        System.out.println("endProcessingCounts");
    }

    @Schedule(month = "*", hour = "21-5", dayOfMonth = "*", year = "*", minute = "*/5", second = "0", persistent = false)
    public void processCounts() {
        System.out.println("processCounts");
        if (nowTimeToProcessCounts) {
            System.out.println("Now Processing");
        }else{
            System.out.println("Not Processing");
        }

    }


}
