package view.common;

import java.io.Serializable;

import java.util.Date;

public class WorkerDto implements Serializable{
    @SuppressWarnings("compatibility:-9084088172937510594")
    private static final long serialVersionUID = 657020917173035099L;

    private Long serialNumber;
    private Long qId;
    private String name;
    private String nationalityCode;
    private String gender ;
    private String socialStatus;
    private Long spouseQid;
    private Date birthDate;
    private Date hiringDate;
    private String jobNumber;
    private String jobName;
    private String contractType;
    private String educationalLevel;
    private String specialization;
    private Double basicSalary;
    private Double socialAllowance;
    private Double transportAllowance;
    private Double workNatureAllowance;
    private Double housingAllowance;
    private Double otherAllowance;
    private Double totalMonthlySalary;
    private String housingType;
    private String additionalNotes;


    public void setSerialNumber(Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public void setQId(Long qId) {
        this.qId = qId;
    }

    public Long getQId() {
        return qId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNationalityCode(String nationalityCode) {
        this.nationalityCode = nationalityCode;
    }

    public String getNationalityCode() {
        return nationalityCode;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setSocialStatus(String socialStatus) {
        this.socialStatus = socialStatus;
    }

    public String getSocialStatus() {
        return socialStatus;
    }

    public void setSpouseQid(Long spouseQid) {
        this.spouseQid = spouseQid;
    }

    public Long getSpouseQid() {
        return spouseQid;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setHiringDate(Date hiringDate) {
        this.hiringDate = hiringDate;
    }

    public Date getHiringDate() {
        return hiringDate;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getContractType() {
        return contractType;
    }

    public void setEducationalLevel(String educationalLevel) {
        this.educationalLevel = educationalLevel;
    }

    public String getEducationalLevel() {
        return educationalLevel;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setSocialAllowance(Double socialAllowance) {
        this.socialAllowance = socialAllowance;
    }

    public Double getSocialAllowance() {
        return socialAllowance;
    }

    public void setTransportAllowance(Double transportAllowance) {
        this.transportAllowance = transportAllowance;
    }

    public Double getTransportAllowance() {
        return transportAllowance;
    }

    public void setWorkNatureAllowance(Double workNatureAllowance) {
        this.workNatureAllowance = workNatureAllowance;
    }

    public Double getWorkNatureAllowance() {
        return workNatureAllowance;
    }

    public void setHousingAllowance(Double housingAllowance) {
        this.housingAllowance = housingAllowance;
    }

    public Double getHousingAllowance() {
        return housingAllowance;
    }

    public void setOtherAllowance(Double otherAllowance) {
        this.otherAllowance = otherAllowance;
    }

    public Double getOtherAllowance() {
        return otherAllowance;
    }

    public void setTotalMonthlySalary(Double totalMonthlySalary) {
        this.totalMonthlySalary = totalMonthlySalary;
    }

    public Double getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public void setHousingType(String housingType) {
        this.housingType = housingType;
    }

    public String getHousingType() {
        return housingType;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }
}
