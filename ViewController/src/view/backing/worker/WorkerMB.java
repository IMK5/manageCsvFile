package view.backing.worker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import model.AppModuleImpl;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.layout.RichPanelFormLayout;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;

import org.apache.myfaces.trinidad.model.UploadedFile;

import view.backing.AbstractMBConfig;
import view.backing.CommonService;

import view.common.FileInfoDto;
import view.common.Template;
import view.common.WorkerDto;

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

   
   
   
   
   
   /**
     * Uploading file 
     * @param valueChangeEvent
     */
    public void uploadFileListener(ValueChangeEvent valueChangeEvent) throws Exception {
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
   
    /**
     * When click on save button
     * @return String 
     */
    public String bSave_action() {
        System.out.println("Call  bSave_action ...");
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

        if(requiredsFieldsValidated()){

            try {
                wService.saveFinalWorkerAndDeleteDraftWorker(appModule, rows);
                showPopup();
            } catch (Exception e) {
                e.printStackTrace();
                CommonService.shoeInlineMessage(e.getMessage());
                rollback(appModule);
            }

        } else {
            setRequiredFieldsToTrue();

        }


        return null;
    }
    
    
    
    private List<WorkerDto>  buildData(BufferedReader br, UploadedFile uploadedFile) throws IOException, Exception,
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
            worker[1].equalsIgnoreCase(Template.WORKER_QID) &&
            worker[2].equalsIgnoreCase(Template.WORKER_NAME) &&
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
    public boolean isDataValidated(){
      boolean isValidated = false;
      

      
      return isValidated;
      }

    private boolean requiredsFieldsValidated() {
        //TODO
        return false;
    }
    
    private void setRequiredFieldsToTrue() {
    //TODO
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


   
}
