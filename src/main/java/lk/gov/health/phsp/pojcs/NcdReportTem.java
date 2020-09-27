/*
 * The MIT License
 *
 * Copyright 2020 MoH LK.
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
 * @author Dr MHB Ariyaratne
 */
public class NcdReportTem {

    
    
    private long TotalNoOfParticipants;
    private long Age20To34;
    private long Age35To65;
    private long AgeGt65;
    private long NumberWithAlreadyDiagnosedDiseasesDM;
    private long NumberWithAlreadyDiagnosedDiseasesHypertension;
    private long NumberWithAlreadyDiagnosedDiseasesCVA;
    private long NumberWithAlreadyDiagnosedDiseasesHeartDiseases;
    private long NumberWithAlreadyDiagnosedDiseasesCKD;
    private long NumberWithAlreadyDiagnosedDiseasesCancer;
    private long NumberWithAlreadyDiagnosedDiseasesOther;
    private long PhysicalActivitySedentary;
    private long PhysicalActivityNonSedentary;
    private long RiskFactorsNoOfSmokers;
    private long RiskFactorsNoOfChewingTobacco;
    private long RiskFactorsNoOfOtherSmokelessTobaccoUsers;
    private long NoOfAlcoholUsers;
    private long BmiLt18_5;
    private long Bmi18_5to22_9;
    private long Bmi23to24_9;
    private long Bmi25to29_9;
    private long BmiGt30;
    private long WaistCircumstanceMaleGt90cmOrFemaleGt80cm;
    private long Sbp120andDbp80;
    private long Sbo120To139AndDbp80To89;
    private long Sbp140AndDbp90OrAbove;
    private long NoOfParticipantsWhoHaveUndergoneTheOralExamination;
    private long NoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin;
    private long NoOfParticipantsWhoHaveUndergoneTheTyroidExamination;
    private long NoOfParticipantsWhoHaveUndergoneThePapSmearExamination;
    private long RbsLt140;
    private long Rbs140To199;
    private long RbsGte200;
    private long FbsLt100;
    private long Fbs100To125;
    private long FbsGt126;
    private long SerumCreatininNormal;
    private long SerumCreatininHigh;
    private long SerumCholosterolMgDlLt270;
    private long SerumCholosterolMgDlLt270To299;
    private long SerumCholosterolMgDlLtGt300;
    private long CvsRiskLt10;
    private long CvsRisk10to20;
    private long CvsRisk20to30;
    private long CvsRiskGt30;
    private long ReferralMedicalClinicOfTheInstitution;
    private long ReferralSpecialMedicalClinic;
    private long ReferralHealthyLifestyleCenter;
    private long ReferralWellWomanClinic;
    private long ReferralDentalClinic;

    public long getTotalNoOfParticipants() {
        return TotalNoOfParticipants;
    }

    public void setTotalNoOfParticipants(long TotalNoOfParticipants) {
        this.TotalNoOfParticipants = TotalNoOfParticipants;
    }

    public long getAge20To34() {
        return Age20To34;
    }

    public void setAge20To34(long Age20To34) {
        this.Age20To34 = Age20To34;
    }

    public long getAge35To65() {
        return Age35To65;
    }

    public void setAge35To65(long Age35To65) {
        this.Age35To65 = Age35To65;
    }

    public long getAgeGt65() {
        return AgeGt65;
    }

    public void setAgeGt65(long AgeGt65) {
        this.AgeGt65 = AgeGt65;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesDM() {
        return NumberWithAlreadyDiagnosedDiseasesDM;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesDM(long NumberWithAlreadyDiagnosedDiseasesDM) {
        this.NumberWithAlreadyDiagnosedDiseasesDM = NumberWithAlreadyDiagnosedDiseasesDM;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesHypertension() {
        return NumberWithAlreadyDiagnosedDiseasesHypertension;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesHypertension(long NumberWithAlreadyDiagnosedDiseasesHypertension) {
        this.NumberWithAlreadyDiagnosedDiseasesHypertension = NumberWithAlreadyDiagnosedDiseasesHypertension;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesCVA() {
        return NumberWithAlreadyDiagnosedDiseasesCVA;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesCVA(long NumberWithAlreadyDiagnosedDiseasesCVA) {
        this.NumberWithAlreadyDiagnosedDiseasesCVA = NumberWithAlreadyDiagnosedDiseasesCVA;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesHeartDiseases() {
        return NumberWithAlreadyDiagnosedDiseasesHeartDiseases;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesHeartDiseases(long NumberWithAlreadyDiagnosedDiseasesHeartDiseases) {
        this.NumberWithAlreadyDiagnosedDiseasesHeartDiseases = NumberWithAlreadyDiagnosedDiseasesHeartDiseases;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesCKD() {
        return NumberWithAlreadyDiagnosedDiseasesCKD;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesCKD(long NumberWithAlreadyDiagnosedDiseasesCKD) {
        this.NumberWithAlreadyDiagnosedDiseasesCKD = NumberWithAlreadyDiagnosedDiseasesCKD;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesCancer() {
        return NumberWithAlreadyDiagnosedDiseasesCancer;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesCancer(long NumberWithAlreadyDiagnosedDiseasesCancer) {
        this.NumberWithAlreadyDiagnosedDiseasesCancer = NumberWithAlreadyDiagnosedDiseasesCancer;
    }

    public long getNumberWithAlreadyDiagnosedDiseasesOther() {
        return NumberWithAlreadyDiagnosedDiseasesOther;
    }

    public void setNumberWithAlreadyDiagnosedDiseasesOther(long NumberWithAlreadyDiagnosedDiseasesOther) {
        this.NumberWithAlreadyDiagnosedDiseasesOther = NumberWithAlreadyDiagnosedDiseasesOther;
    }

    public long getPhysicalActivitySedentary() {
        return PhysicalActivitySedentary;
    }

    public void setPhysicalActivitySedentary(long PhysicalActivitySedentary) {
        this.PhysicalActivitySedentary = PhysicalActivitySedentary;
    }

    public long getPhysicalActivityNonSedentary() {
        return PhysicalActivityNonSedentary;
    }

    public void setPhysicalActivityNonSedentary(long PhysicalActivityNonSedentary) {
        this.PhysicalActivityNonSedentary = PhysicalActivityNonSedentary;
    }

    public long getRiskFactorsNoOfSmokers() {
        return RiskFactorsNoOfSmokers;
    }

    public void setRiskFactorsNoOfSmokers(long RiskFactorsNoOfSmokers) {
        this.RiskFactorsNoOfSmokers = RiskFactorsNoOfSmokers;
    }

    public long getRiskFactorsNoOfChewingTobacco() {
        return RiskFactorsNoOfChewingTobacco;
    }

    public void setRiskFactorsNoOfChewingTobacco(long RiskFactorsNoOfChewingTobacco) {
        this.RiskFactorsNoOfChewingTobacco = RiskFactorsNoOfChewingTobacco;
    }

    public long getRiskFactorsNoOfOtherSmokelessTobaccoUsers() {
        return RiskFactorsNoOfOtherSmokelessTobaccoUsers;
    }

    public void setRiskFactorsNoOfOtherSmokelessTobaccoUsers(long RiskFactorsNoOfOtherSmokelessTobaccoUsers) {
        this.RiskFactorsNoOfOtherSmokelessTobaccoUsers = RiskFactorsNoOfOtherSmokelessTobaccoUsers;
    }

    public long getNoOfAlcoholUsers() {
        return NoOfAlcoholUsers;
    }

    public void setNoOfAlcoholUsers(long NoOfAlcoholUsers) {
        this.NoOfAlcoholUsers = NoOfAlcoholUsers;
    }

    public long getBmiLt18_5() {
        return BmiLt18_5;
    }

    public void setBmiLt18_5(long BmiLt18_5) {
        this.BmiLt18_5 = BmiLt18_5;
    }

    public long getBmi18_5to22_9() {
        return Bmi18_5to22_9;
    }

    public void setBmi18_5to22_9(long Bmi18_5to22_9) {
        this.Bmi18_5to22_9 = Bmi18_5to22_9;
    }

    public long getBmi23to24_9() {
        return Bmi23to24_9;
    }

    public void setBmi23to24_9(long Bmi23to24_9) {
        this.Bmi23to24_9 = Bmi23to24_9;
    }

    public long getBmi25to29_9() {
        return Bmi25to29_9;
    }

    public void setBmi25to29_9(long Bmi25to29_9) {
        this.Bmi25to29_9 = Bmi25to29_9;
    }

    public long getBmiGt30() {
        return BmiGt30;
    }

    public void setBmiGt30(long BmiGt30) {
        this.BmiGt30 = BmiGt30;
    }

    public long getWaistCircumstanceMaleGt90cmOrFemaleGt80cm() {
        return WaistCircumstanceMaleGt90cmOrFemaleGt80cm;
    }

    public void setWaistCircumstanceMaleGt90cmOrFemaleGt80cm(long WaistCircumstanceMaleGt90cmOrFemaleGt80cm) {
        this.WaistCircumstanceMaleGt90cmOrFemaleGt80cm = WaistCircumstanceMaleGt90cmOrFemaleGt80cm;
    }

    public long getSbp120andDbp80() {
        return Sbp120andDbp80;
    }

    public void setSbp120andDbp80(long Sbp120andDbp80) {
        this.Sbp120andDbp80 = Sbp120andDbp80;
    }

    public long getSbo120To139AndDbp80To89() {
        return Sbo120To139AndDbp80To89;
    }

    public void setSbo120To139AndDbp80To89(long Sbo120To139AndDbp80To89) {
        this.Sbo120To139AndDbp80To89 = Sbo120To139AndDbp80To89;
    }

    public long getSbp140AndDbp90OrAbove() {
        return Sbp140AndDbp90OrAbove;
    }

    public void setSbp140AndDbp90OrAbove(long Sbp140AndDbp90OrAbove) {
        this.Sbp140AndDbp90OrAbove = Sbp140AndDbp90OrAbove;
    }

    public long getNoOfParticipantsWhoHaveUndergoneTheOralExamination() {
        return NoOfParticipantsWhoHaveUndergoneTheOralExamination;
    }

    public void setNoOfParticipantsWhoHaveUndergoneTheOralExamination(long NoOfParticipantsWhoHaveUndergoneTheOralExamination) {
        this.NoOfParticipantsWhoHaveUndergoneTheOralExamination = NoOfParticipantsWhoHaveUndergoneTheOralExamination;
    }

    public long getNoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin() {
        return NoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin;
    }

    public void setNoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin(long NoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin) {
        this.NoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin = NoOfParticipantsWhoHaveUndergoneTheBreastExaminatioin;
    }

    public long getNoOfParticipantsWhoHaveUndergoneTheTyroidExamination() {
        return NoOfParticipantsWhoHaveUndergoneTheTyroidExamination;
    }

    public void setNoOfParticipantsWhoHaveUndergoneTheTyroidExamination(long NoOfParticipantsWhoHaveUndergoneTheTyroidExamination) {
        this.NoOfParticipantsWhoHaveUndergoneTheTyroidExamination = NoOfParticipantsWhoHaveUndergoneTheTyroidExamination;
    }

    public long getNoOfParticipantsWhoHaveUndergoneThePapSmearExamination() {
        return NoOfParticipantsWhoHaveUndergoneThePapSmearExamination;
    }

    public void setNoOfParticipantsWhoHaveUndergoneThePapSmearExamination(long NoOfParticipantsWhoHaveUndergoneThePapSmearExamination) {
        this.NoOfParticipantsWhoHaveUndergoneThePapSmearExamination = NoOfParticipantsWhoHaveUndergoneThePapSmearExamination;
    }

    public long getRbsLt140() {
        return RbsLt140;
    }

    public void setRbsLt140(long RbsLt140) {
        this.RbsLt140 = RbsLt140;
    }

    public long getRbs140To199() {
        return Rbs140To199;
    }

    public void setRbs140To199(long Rbs140To199) {
        this.Rbs140To199 = Rbs140To199;
    }

    public long getRbsGte200() {
        return RbsGte200;
    }

    public void setRbsGte200(long RbsGte200) {
        this.RbsGte200 = RbsGte200;
    }

    public long getFbsLt100() {
        return FbsLt100;
    }

    public void setFbsLt100(long FbsLt100) {
        this.FbsLt100 = FbsLt100;
    }

    public long getFbs100To125() {
        return Fbs100To125;
    }

    public void setFbs100To125(long Fbs100To125) {
        this.Fbs100To125 = Fbs100To125;
    }

    public long getFbsGt126() {
        return FbsGt126;
    }

    public void setFbsGt126(long FbsGt126) {
        this.FbsGt126 = FbsGt126;
    }

    public long getSerumCreatininNormal() {
        return SerumCreatininNormal;
    }

    public void setSerumCreatininNormal(long SerumCreatininNormal) {
        this.SerumCreatininNormal = SerumCreatininNormal;
    }

    public long getSerumCreatininHigh() {
        return SerumCreatininHigh;
    }

    public void setSerumCreatininHigh(long SerumCreatininHigh) {
        this.SerumCreatininHigh = SerumCreatininHigh;
    }

    public long getSerumCholosterolMgDlLt270() {
        return SerumCholosterolMgDlLt270;
    }

    public void setSerumCholosterolMgDlLt270(long SerumCholosterolMgDlLt270) {
        this.SerumCholosterolMgDlLt270 = SerumCholosterolMgDlLt270;
    }

    public long getSerumCholosterolMgDlLt270To299() {
        return SerumCholosterolMgDlLt270To299;
    }

    public void setSerumCholosterolMgDlLt270To299(long SerumCholosterolMgDlLt270To299) {
        this.SerumCholosterolMgDlLt270To299 = SerumCholosterolMgDlLt270To299;
    }

    public long getSerumCholosterolMgDlLtGt300() {
        return SerumCholosterolMgDlLtGt300;
    }

    public void setSerumCholosterolMgDlLtGt300(long SerumCholosterolMgDlLtGt300) {
        this.SerumCholosterolMgDlLtGt300 = SerumCholosterolMgDlLtGt300;
    }

    public long getCvsRiskLt10() {
        return CvsRiskLt10;
    }

    public void setCvsRiskLt10(long CvsRiskLt10) {
        this.CvsRiskLt10 = CvsRiskLt10;
    }

    public long getCvsRisk10to20() {
        return CvsRisk10to20;
    }

    public void setCvsRisk10to20(long CvsRisk10to20) {
        this.CvsRisk10to20 = CvsRisk10to20;
    }

    public long getCvsRisk20to30() {
        return CvsRisk20to30;
    }

    public void setCvsRisk20to30(long CvsRisk20to30) {
        this.CvsRisk20to30 = CvsRisk20to30;
    }

    public long getCvsRiskGt30() {
        return CvsRiskGt30;
    }

    public void setCvsRiskGt30(long CvsRiskGt30) {
        this.CvsRiskGt30 = CvsRiskGt30;
    }

    public long getReferralMedicalClinicOfTheInstitution() {
        return ReferralMedicalClinicOfTheInstitution;
    }

    public void setReferralMedicalClinicOfTheInstitution(long ReferralMedicalClinicOfTheInstitution) {
        this.ReferralMedicalClinicOfTheInstitution = ReferralMedicalClinicOfTheInstitution;
    }

    public long getReferralSpecialMedicalClinic() {
        return ReferralSpecialMedicalClinic;
    }

    public void setReferralSpecialMedicalClinic(long ReferralSpecialMedicalClinic) {
        this.ReferralSpecialMedicalClinic = ReferralSpecialMedicalClinic;
    }

    public long getReferralHealthyLifestyleCenter() {
        return ReferralHealthyLifestyleCenter;
    }

    public void setReferralHealthyLifestyleCenter(long ReferralHealthyLifestyleCenter) {
        this.ReferralHealthyLifestyleCenter = ReferralHealthyLifestyleCenter;
    }

    public long getReferralWellWomanClinic() {
        return ReferralWellWomanClinic;
    }

    public void setReferralWellWomanClinic(long ReferralWellWomanClinic) {
        this.ReferralWellWomanClinic = ReferralWellWomanClinic;
    }

    public long getReferralDentalClinic() {
        return ReferralDentalClinic;
    }

    public void setReferralDentalClinic(long ReferralDentalClinic) {
        this.ReferralDentalClinic = ReferralDentalClinic;
    }

    
    
    
}
