package view.backing.worker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import model.AppModuleImpl;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.layout.RichPanelFormLayout;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.model.UploadedFile;

import view.backing.AbstractMBConfig;

import view.backing.common.CommonService;
import view.backing.common.FileInfoDto;
import view.backing.common.Template;
import view.backing.common.WorkerDto;

public class WorkerMB extends AbstractMBConfig {


    private WorkerService wService = new WorkerService();
    private InputStream uploadFileInputStream;
    private FileInfoDto fileInfoDto = new FileInfoDto();
    private List<WorkerDto> workerDtoList = new ArrayList();
    private List<String> wrongDataList = new ArrayList();
    private boolean displayTable = false;
    private RichPanelFormLayout panelForm;
    private RichInputFile richUploadFile;
    private RichPopup popup;
    private String errorMessage = "";
    public final static int NUMBER_OF_COLUMN = 23;
    private boolean displayStructureDataLink = false;
    // Required fields
    private RichInputText serialNumberText;

    private RichInputText nameText;
    private RichInputText nationalityCodeText;
    private RichInputText genderText;
    private RichInputText socialStatusText;
    private RichInputText spouseQidText;
    private RichInputDate birthDateText;
    private RichInputDate hiringDateTExt;
    private RichInputText jobNumberText;
    private RichInputText jobNameText;
    private RichInputText specializationText;
    private RichInputText basicSalaryText;
    private RichInputText totalMonthlySalaryText;
    private RichInputText qidText;


    /**
     * Uploading file
     * @param valueChangeEvent
     */
    
    public void uploadFileEventListener(ValueChangeEvent valueChangeEvent)  throws Exception{
        System.out.println("Call uploadFileListener...");
        BufferedReader br = null;
        setWorkerDtoList(new ArrayList());
        setWrongDataList(new ArrayList());

        // 1 : check file format
        UploadedFile uploadedFile = (UploadedFile) valueChangeEvent.getNewValue();
        if (!CommonService.isCsvFile(uploadedFile)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Check File Format",
                        uploadedFile.getFilename() + " is not CSV file !");
            setDisplayTable(false);
            setWorkerDtoList(null);
            throw new Exception(uploadedFile.getFilename() + " is not CSV file !");
        }

        try {
            System.out.println("employee template start process ...");
            fileInfoDto = CommonService.buildFileInfo(uploadedFile);
            List<WorkerDto> dtoList = buildData(br, uploadedFile);
            setWorkerDtoList(dtoList);

            getFileInfoDto().setErrorRecordsNumber(getWrongDataList().size());
            getFileInfoDto().setRightDataNumber(dtoList.size());

            // Save data in DB
            wService.saveWorker_Draft_AND_Error_Data(getWorkerDtoList(), getWrongDataList());

            //  Refresh table;
            CommonService.refreshTable("WorkerDraftView1Iterator");
            if (getWrongDataList().size() > 0) {
                setDisplayStructureDataLink(true);
            }

            setDisplayTable(true);
            AdfFacesContext.getCurrentInstance().addPartialTarget(getPanelForm());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
        } catch (IOException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {

                    br.close();
                } catch (IOException e) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    /*
    void uploadFileListener(ValueChangeEvent valueChangeEvent) throws Exception {
        System.out.println("Call uploadFileListener...");
        BufferedReader br = null;
        setWorkerDtoList(new ArrayList());
        setWrongDataList(new ArrayList());

        // 1 : check file format
        UploadedFile uploadedFile = (UploadedFile) valueChangeEvent.getNewValue();
        if (!CommonService.isCsvFile(uploadedFile)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Check File Format",
                        uploadedFile.getFilename() + " is not CSV file !");
            setDisplayTable(false);
            setWorkerDtoList(null);
            throw new Exception(uploadedFile.getFilename() + " is not CSV file !");
        }

        try {
            System.out.println("employee template start process ...");
            fileInfoDto = CommonService.buildFileInfo(uploadedFile);
            List<WorkerDto> dtoList = buildData(br, uploadedFile);
            setWorkerDtoList(dtoList);

            getFileInfoDto().setErrorRecordsNumber(getWrongDataList().size());
            getFileInfoDto().setRightDataNumber(dtoList.size());

            // Save data in DB
            wService.saveWorker_Draft_AND_Error_Data(getWorkerDtoList(), getWrongDataList());

            //  Refresh table;
            CommonService.refreshTable("WorkerDraftView1Iterator");
            if (getWrongDataList().size() > 0) {
                setDisplayStructureDataLink(true);
            }

            setDisplayTable(true);
            AdfFacesContext.getCurrentInstance().addPartialTarget(getPanelForm());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
        } catch (IOException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {

                    br.close();
                } catch (IOException e) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }*/

    /**
     * When click on save button
     * @return String
     */
    public String bSave_action() {
        System.out.println("Call  bSave_action ...");
        setRequiredField(false);
        BindingContainer bindings = getBindings();
        OperationBinding operationBinding = bindings.getOperationBinding("Commit");
        Object result = operationBinding.execute();
        if (!operationBinding.getErrors().isEmpty()) {
            return null;
        }
        showPopup();
        return null;
    }
    
    
    /**
     * Submit data to DB and make validation data
     * @return null
     */
    public String submit() {
        System.out.println("Call  submit_action ...");

        // Get data from table
        DCBindingContainer dcBindings = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
        DCIteratorBinding iterBind = (DCIteratorBinding) dcBindings.get("WorkerDraftView1Iterator");
        Row[] rows = iterBind.getAllRowsInRange();
        AppModuleImpl appModule = getConfig();
        // Validation data

        if ( isDataValidated(rows)) {

            try {
                wService.saveFinalWorkerAndDeleteDraftWorker(appModule, rows);
                showPopup();
            } catch (Exception e) {
                e.printStackTrace();
                CommonService.shoeInlineMessage(e.getMessage());
                rollback(appModule);
            }

        } else {
            // Set required fields to TRUE
            setRequiredField(true);
            CommonService.shoeInlineMessage("Please check required fields !");

        }


        return null;
    }


    private List<WorkerDto> buildData(BufferedReader br, UploadedFile uploadedFile) throws IOException, Exception,
                                                                                           ParseException {
        List<WorkerDto> tempList = new ArrayList();
        String line = "";
        String cvsSplitBy = ",";
        int iteration = 0;

        // Upload file
        uploadFileInputStream = uploadedFile.getInputStream();
        // Validate structure file
        br = new BufferedReader(new InputStreamReader(uploadFileInputStream));
        while ((line = br.readLine()) != null) {
            String[] empl = line.split(cvsSplitBy);
            if (iteration == 0 && checkCsvFileHeaderStructure(uploadedFile, empl)) {
                iteration++;
                continue;
            }
            // Check structure file
            if (empl != null && empl.length != 0) {
                if (empl.length == NUMBER_OF_COLUMN) {
                    WorkerDto newWorker = wService.createNewWorker(empl);
                    tempList.add(newWorker);
                    System.out.println("Worker [SerialNumber= " + newWorker.getSerialNumber() + " , name=" +
                                       newWorker.getName() + "]");

                } else {
                    // Add wrong data into errorDataList
                    String wrongString = CommonService.convertArrayToString(empl);
                    if (wrongString != null && wrongString.length() != 0) {
                        wrongDataList.add(wrongString);
                    }

                }
            }


        }
        return tempList;
    }

    /**
     * Check the header of csv file
     * @param employee
     * @return
     * @throws Exception
     */
    private boolean checkCsvFileHeaderStructure(UploadedFile uploadedFile, String[] worker) throws Exception {

        if (worker == null) {
            errorMessage = "Your file is empty !";
            handleTable();
            return false;
        }
        // Check all fields...
        if (worker[0].equalsIgnoreCase(Template.WORKER_SERIAL_NUMBER) &&
            worker[1].equalsIgnoreCase(Template.WORKER_QID) && worker[2].equalsIgnoreCase(Template.WORKER_NAME) &&
            worker[3].equalsIgnoreCase(Template.WORKER_NATIONALITY_CODE))
            return true;
        else {
            errorMessage = "Please check file structure !";
            showMessage(FacesMessage.SEVERITY_ERROR, "File structure", errorMessage);
            setWorkerDtoList(null);
            handleTable();
            throw new Exception(errorMessage);

        }

    }

    private void showMessage(FacesMessage.Severity severity, String message1, String message2) {
        FacesContext.getCurrentInstance()
            .addMessage(richUploadFile.getClientId(FacesContext.getCurrentInstance()),
                        new FacesMessage(severity, message1, message2));
        richUploadFile.resetValue();
        richUploadFile.setValid(false);
    }


    // The main validated data method
    public boolean isDataValidated(Row[] rows) {
        return isRequiredsFieldsValidated(rows) && isSpouseQidValidated(rows);
    }

    private boolean isSpouseQidValidated(Row[] rows) {
        for (Row row : rows) {
            Long spouseQid = (Long) row.getAttribute(Template.WORKER_SPOUSE_QID);
            String socilaStatus = (String) row.getAttribute(Template.WORKER_SOCIALS_STATUS);
            if (!StringUtils.isBlank(socilaStatus) && socilaStatus.equals("M") && spouseQid == null) {
                CommonService.shoeInlineMessage("In case the applicant is married, You have to enter Spouse's QID");
                return false;
            }
        }
        return true;

    }

    private boolean isRequiredsFieldsValidated(Row[] rows) {
        for (Row row : rows) {
            BigDecimal serialNumber = (BigDecimal) row.getAttribute(Template.WORKER_SERIAL_NUMBER);
            Long qid = (Long) row.getAttribute(Template.WORKER_QID);
            String name = (String) row.getAttribute(Template.WORKER_NAME);
            Integer nationalityCode = (Integer) row.getAttribute(Template.WORKER_NATIONALITY_CODE);
            String gender = (String) row.getAttribute(Template.WORKER_GENDER);
            String socialStatus = (String) row.getAttribute(Template.WORKER_SOCIALS_STATUS);

            Date birthDate = (Date) row.getAttribute(Template.WORKER_BIRTHDATE);
            Date hiringDate = (Date) row.getAttribute(Template.WORKER_HIRINGDATE);
            String jobNumber = (String) row.getAttribute(Template.WORKER_JOB_NUMBER);
            String jobName = (String) row.getAttribute(Template.WORKER_JOB_NAME);
            BigDecimal basicSalary = (BigDecimal) row.getAttribute(Template.WORKER_BASICSALARY);
            BigDecimal totalMonthlysalary = (BigDecimal) row.getAttribute(Template.WORKER_TOTAL_MONTHLY_SALARY);

            if (serialNumber == null || qid == null || StringUtils.isBlank(name) || nationalityCode == null ||
                StringUtils.isBlank(gender) || StringUtils.isBlank(socialStatus) || birthDate == null ||
                hiringDate == null || StringUtils.isBlank(jobNumber) || StringUtils.isBlank(jobName) ||
                basicSalary == null || totalMonthlysalary == null) {
                return false;
            }


        }


        return true;
    }

    private void setRequiredField(boolean value) {
        getSerialNumberText().setRequired(value);
        CommonService.refreshComponentUI( getSerialNumberText());
        getQidText().setReadOnly(value);
        CommonService.refreshComponentUI(getQidText() );
        getNameText().setRequired(value);
        CommonService.refreshComponentUI( getNameText() );
        getNationalityCodeText().setRequired(value);
        CommonService.refreshComponentUI(getNationalityCodeText() );
        getGenderText().setRequired(value);
        CommonService.refreshComponentUI(getGenderText() );
        getSocialStatusText().setRequired(value);
        CommonService.refreshComponentUI( getSocialStatusText());
        getBirthDateText().setRequired(value);
        CommonService.refreshComponentUI( getBirthDateText() );
        getHiringDateTExt().setRequired(value);
        CommonService.refreshComponentUI(getHiringDateTExt() );
        getJobNumberText().setRequired(value);
        CommonService.refreshComponentUI(getJobNumberText() );
        getJobNameText().setRequired(value);
        CommonService.refreshComponentUI(  getJobNameText());
        getBasicSalaryText().setRequired(value);
        CommonService.refreshComponentUI( getBasicSalaryText());
        getTotalMonthlySalaryText().setRequired(value);
        CommonService.refreshComponentUI( getTotalMonthlySalaryText());
    }

    public void showPopup() {
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.getPopup().setAutoDismissalTimeout(3);
        this.getPopup().show(hints);
    }

    private void handleTable() {
        setWorkerDtoList(new ArrayList());
        setDisplayTable(false);
    }
  /*  
    private void updateSpouseQidInputtext(boolean b) {
        getSpouseQidText().setRequired(b);
        CommonService.refreshComponentUI(getSpouseQidText());
    }
*/
    public void setFileInfoDto(FileInfoDto fileInfoDto) {
        this.fileInfoDto = fileInfoDto;
    }

    public FileInfoDto getFileInfoDto() {
        return fileInfoDto;
    }


    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }


    public void setDisplayTable(boolean displayTable) {
        this.displayTable = displayTable;
    }

    public boolean isDisplayTable() {
        return displayTable;
    }


    public void setWService(WorkerService wService) {
        this.wService = wService;
    }

    public WorkerService getWService() {
        return wService;
    }

    public void setPanelForm(RichPanelFormLayout panelForm) {
        this.panelForm = panelForm;
    }

    public RichPanelFormLayout getPanelForm() {
        return panelForm;
    }

    public void setRichUploadFile(RichInputFile richUploadFile) {
        this.richUploadFile = richUploadFile;
    }

    public RichInputFile getRichUploadFile() {
        return richUploadFile;
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void setWorkerDtoList(List<WorkerDto> workerDtoList) {
        this.workerDtoList = workerDtoList;
    }

    public List<WorkerDto> getWorkerDtoList() {
        return workerDtoList;
    }

    public void setWrongDataList(List<String> wrongDataList) {
        this.wrongDataList = wrongDataList;
    }

    public List<String> getWrongDataList() {
        return wrongDataList;
    }

    public void setUploadFileInputStream(InputStream uploadFileInputStream) {
        this.uploadFileInputStream = uploadFileInputStream;
    }

    public InputStream getUploadFileInputStream() {
        return uploadFileInputStream;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setDisplayStructureDataLink(boolean displayStructureDataLink) {
        this.displayStructureDataLink = displayStructureDataLink;
    }

    public boolean isDisplayStructureDataLink() {
        return displayStructureDataLink;
    }


    public void setNationalityCodeText(RichInputText nationalityCodeText) {
        this.nationalityCodeText = nationalityCodeText;
    }

    public RichInputText getNationalityCodeText() {
        return nationalityCodeText;
    }

    public void setGenderText(RichInputText genderText) {
        this.genderText = genderText;
    }

    public RichInputText getGenderText() {
        return genderText;
    }

    public void setSocialStatusText(RichInputText socialStatusText) {
        this.socialStatusText = socialStatusText;
    }

    public RichInputText getSocialStatusText() {
        return socialStatusText;
    }

    public void setSpouseQidText(RichInputText spouseQidText) {
        this.spouseQidText = spouseQidText;
    }

    public RichInputText getSpouseQidText() {
        return spouseQidText;
    }

    public void setBirthDateText(RichInputDate birthDateText) {
        this.birthDateText = birthDateText;
    }

    public RichInputDate getBirthDateText() {
        return birthDateText;
    }

    public void setHiringDateTExt(RichInputDate hiringDateTExt) {
        this.hiringDateTExt = hiringDateTExt;
    }

    public RichInputDate getHiringDateTExt() {
        return hiringDateTExt;
    }

    public void setJobNumberText(RichInputText jobNumberText) {
        this.jobNumberText = jobNumberText;
    }

    public RichInputText getJobNumberText() {
        return jobNumberText;
    }

    public void setJobNameText(RichInputText jobNameText) {
        this.jobNameText = jobNameText;
    }

    public RichInputText getJobNameText() {
        return jobNameText;
    }

    public void setSpecializationText(RichInputText specializationText) {
        this.specializationText = specializationText;
    }

    public RichInputText getSpecializationText() {
        return specializationText;
    }

    public void setBasicSalaryText(RichInputText basicSalaryText) {
        this.basicSalaryText = basicSalaryText;
    }

    public RichInputText getBasicSalaryText() {
        return basicSalaryText;
    }

    public void setTotalMonthlySalaryText(RichInputText totalMonthlySalaryText) {
        this.totalMonthlySalaryText = totalMonthlySalaryText;
    }

    public RichInputText getTotalMonthlySalaryText() {
        return totalMonthlySalaryText;
    }

    public void setSerialNumberText(RichInputText serialNumberText) {
        this.serialNumberText = serialNumberText;
    }

    public RichInputText getSerialNumberText() {
        return serialNumberText;
    }

    public void setNameText(RichInputText nameText) {
        this.nameText = nameText;
    }

    public RichInputText getNameText() {
        return nameText;
    }


    public void setQidText(RichInputText qidText) {
        this.qidText = qidText;
    }

    public RichInputText getQidText() {
        return qidText;
    }


    public String b2_action() {
        BindingContainer bindings = getBindings();
        OperationBinding operationBinding = bindings.getOperationBinding("Commit");
        Object result = operationBinding.execute();
        if (!operationBinding.getErrors().isEmpty()) {
            return null;
        }
        return null;
    }
}
