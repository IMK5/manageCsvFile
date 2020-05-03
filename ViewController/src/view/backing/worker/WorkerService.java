package view.backing.worker;

import java.sql.Timestamp;

import java.text.ParseException;

import java.util.Date;
import java.util.List;

import model.AppModuleImpl;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.client.Configuration;

import org.apache.commons.lang3.StringUtils;

import view.backing.common.CommonService;
import view.backing.common.Template;
import view.backing.common.WorkerDto;


public class WorkerService {

    private String dateFormat = "dd-MM-yy";

    /**
     * Update DB after validation data
     * @param appModule
     * @param rows : from WorkerDraft
     */
    public void saveFinalWorkerAndDeleteDraftWorker(AppModuleImpl appModule, Row[] rows) throws ParseException {
        System.out.println("Call WorkerService.saveFinalWorkerAndDeleteDraftWorker ...");

        ViewObject draftWorkerVO = appModule.getWorkerDraftView1();
        ViewObject finalWorkerVO = appModule.getWorkerView1();
        try {
            for (Row row : rows) {
                //Step1 : save Employee in DB
                createWorkerVO(finalWorkerVO, row);

                // Step2: delete draftEmp row from DB
                String draftWorkerId = row.getAttribute(Template.WORKER_ID).toString();
                CommonService.deleteRowById(draftWorkerVO, Template.WORKER_ID, draftWorkerId);
            }
            // Step3: commit transaction
            appModule.getTransaction().commit();
            CommonService.refreshTable("WorkerDraftView1Iterator");

        } finally {
            Configuration.releaseRootApplicationModule(appModule, true);
        }


    }

    private void createWorkerVO(ViewObject vo, Row draftRow) throws ParseException {
        Row row = vo.createRow();
        //row.setAttribute("EmployeeId", draftRow.getAttribute("EmployeeId"));
        row.setAttribute(Template.WORKER_SERIAL_NUMBER, draftRow.getAttribute(Template.WORKER_SERIAL_NUMBER));
        row.setAttribute(Template.WORKER_QID, draftRow.getAttribute(Template.WORKER_QID));
        row.setAttribute(Template.WORKER_NAME, draftRow.getAttribute(Template.WORKER_NAME));
        row.setAttribute(Template.WORKER_NATIONALITY_CODE, draftRow.getAttribute(Template.WORKER_NATIONALITY_CODE));
        row.setAttribute(Template.WORKER_GENDER, draftRow.getAttribute(Template.WORKER_GENDER));
        row.setAttribute(Template.WORKER_SOCIALS_STATUS, draftRow.getAttribute(Template.WORKER_SOCIALS_STATUS));
        row.setAttribute(Template.WORKER_SPOUSE_QID, draftRow.getAttribute(Template.WORKER_SPOUSE_QID));
        java.sql.Timestamp bd = (Timestamp) draftRow.getAttribute(Template.WORKER_BIRTHDATE);
        System.out.println("timestimp " + bd);
        if (bd != null) {
            // String dateString = new SimpleDateFormat(dateFormat).format(bd);
            //  System.out.println("format date :  " + dateString);
            row.setAttribute(Template.WORKER_BIRTHDATE, bd);
        }
        java.sql.Timestamp hd = (Timestamp) draftRow.getAttribute(Template.WORKER_HIRINGDATE);
        if (hd != null) {
            // String dateString = new SimpleDateFormat( dateFormat).format(bd);
            row.setAttribute(Template.WORKER_HIRINGDATE, hd);
        }

        row.setAttribute(Template.WORKER_JOB_NUMBER, draftRow.getAttribute(Template.WORKER_JOB_NUMBER));
        row.setAttribute(Template.WORKER_JOB_NAME, draftRow.getAttribute(Template.WORKER_JOB_NAME));
        row.setAttribute(Template.WORKER_CONTRACT_TYPE, draftRow.getAttribute(Template.WORKER_CONTRACT_TYPE));
        row.setAttribute(Template.WORKER_EDUCATIONAL_LEVEL, draftRow.getAttribute(Template.WORKER_EDUCATIONAL_LEVEL));
        row.setAttribute(Template.WORKER_SPECIALIZATION, draftRow.getAttribute(Template.WORKER_SPECIALIZATION));
        row.setAttribute(Template.WORKER_BASICSALARY, draftRow.getAttribute(Template.WORKER_BASICSALARY));
        row.setAttribute(Template.WORKER_SOCIALAL_LOWANCE, draftRow.getAttribute(Template.WORKER_SOCIALAL_LOWANCE));
        row.setAttribute(Template.WORKER_TRANSPORT_ALLOWANCE,
                         draftRow.getAttribute(Template.WORKER_TRANSPORT_ALLOWANCE));
        row.setAttribute(Template.WORKER_WORK_NATURE_ALLOWANCE,
                         draftRow.getAttribute(Template.WORKER_WORK_NATURE_ALLOWANCE));
        row.setAttribute(Template.WORKER_HOUSING_ALLOWANCE, draftRow.getAttribute(Template.WORKER_HOUSING_ALLOWANCE));
        row.setAttribute(Template.WORKER_OTHER_ALLOWANCE, draftRow.getAttribute(Template.WORKER_OTHER_ALLOWANCE));
        row.setAttribute(Template.WORKER_TOTAL_MONTHLY_SALARY,
                         draftRow.getAttribute(Template.WORKER_TOTAL_MONTHLY_SALARY));
        row.setAttribute(Template.WORKER_HOUSING_TYPE, draftRow.getAttribute(Template.WORKER_HOUSING_TYPE));
        row.setAttribute(Template.WORKER_ADDITIONAL_NOTES, draftRow.getAttribute(Template.WORKER_ADDITIONAL_NOTES));

        vo.insertRow(row);
       // vo.executeQuery();

    }

    /**
     * Create new WorkerDto
     * @param empl splitted data fron csv file
     * @return WorkerDto
     * @throws ParseException
     */
    public WorkerDto createNewWorker(String[] empl) throws ParseException {
        WorkerDto dto = new WorkerDto();
        if (!StringUtils.isBlank(empl[0])) {
            dto.setSerialNumber(Long.parseLong(empl[0]));
        }
        if (!StringUtils.isBlank(empl[1])) {
            dto.setQId(Long.parseLong(empl[1]));
        }
        dto.setName(empl[2]);
        dto.setNationalityCode(empl[3]);
        dto.setGender(empl[4]);
        dto.setSocialStatus(empl[5]);
        if (!StringUtils.isBlank(empl[6])) {
            dto.setSpouseQid(Long.parseLong(empl[6]));
        }
        if (!StringUtils.isBlank(empl[7])) {
            Date date = CommonService.convertStringToDate(empl[7], "dd-MMM-yy");
            dto.setBirthDate(date);
        }
        if (!StringUtils.isBlank(empl[8])) {
            Date date = CommonService.convertStringToDate(empl[8], "dd-MMM-yy");
            dto.setHiringDate(date);
        }
        dto.setJobNumber(empl[9]);
        dto.setJobName(empl[10]);
        dto.setContractType(empl[11]);
        dto.setEducationalLevel(empl[12]);
        dto.setSpecialization(empl[13]);
        if (!StringUtils.isBlank(empl[14])) {
            dto.setBasicSalary(Double.parseDouble(empl[14]));
        }
        if (!StringUtils.isBlank(empl[15])) {
            dto.setSocialAllowance(Double.parseDouble(empl[15]));
        }
        if (!StringUtils.isBlank(empl[16])) {
            dto.setTransportAllowance(Double.parseDouble(empl[16]));
        }
        if (!StringUtils.isBlank(empl[17])) {
            dto.setWorkNatureAllowance(Double.parseDouble(empl[17]));
        }
        if (!StringUtils.isBlank(empl[18])) {
            dto.setHousingAllowance(Double.parseDouble(empl[18]));
        }
        if (!StringUtils.isBlank(empl[19])) {
            dto.setOtherAllowance(Double.parseDouble(empl[19]));
        }
        if (!StringUtils.isBlank(empl[20])) {
            dto.setTotalMonthlySalary(Double.parseDouble(empl[20]));
        }
        dto.setHousingType(empl[21]);
        dto.setAdditionalNotes(empl[22]);

        return dto;
    }

    void saveWorker_Draft_AND_Error_Data(List<WorkerDto> dtoList, List<String> wrongDataList) {
        System.out.println("call saveWorker_Draft_AND_Error_Data ..");
        ApplicationModule am = CommonService.getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getWorkerDraftView1();
            ViewObject ve = service.getDataErrorsView1();
            // Save WorkerDraft in DB
            if (dtoList != null && !dtoList.isEmpty()) {
                for (WorkerDto dto : dtoList) {
                    createWorkerRow(vo, dto);
                }
            }
            // Save Erro data in DB
            if (wrongDataList != null && !wrongDataList.isEmpty()) {
                for (String wrongData : wrongDataList) {
                    System.out.println("error data  : " + wrongData);
                    Row row = ve.createRow();
                    row.setAttribute("Data", wrongData);
                    ve.insertRow(row);
                }
            }
            // Commit transaction
            vo.executeQuery();
            ve.executeQuery();
            service.getTransaction().commit();


        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }

    }

    private void createWorkerRow(ViewObject vo, WorkerDto dto) {
        Row row = vo.createRow();
        row.setAttribute(Template.WORKER_SERIAL_NUMBER, dto.getSerialNumber());
        row.setAttribute(Template.WORKER_QID, dto.getQId());
        row.setAttribute(Template.WORKER_NAME, dto.getName());
        row.setAttribute(Template.WORKER_NATIONALITY_CODE, dto.getNationalityCode());
        row.setAttribute(Template.WORKER_GENDER, dto.getGender());
        row.setAttribute(Template.WORKER_SOCIALS_STATUS, dto.getSocialStatus());
        row.setAttribute(Template.WORKER_SPOUSE_QID, dto.getSpouseQid());
        row.setAttribute(Template.WORKER_BIRTHDATE, dto.getBirthDate());
        row.setAttribute(Template.WORKER_HIRINGDATE, dto.getHiringDate());
        row.setAttribute(Template.WORKER_JOB_NUMBER, dto.getJobNumber());
        row.setAttribute(Template.WORKER_JOB_NAME, dto.getJobName());
        row.setAttribute(Template.WORKER_CONTRACT_TYPE, dto.getContractType());
        row.setAttribute(Template.WORKER_EDUCATIONAL_LEVEL, dto.getEducationalLevel());
        row.setAttribute(Template.WORKER_SPECIALIZATION, dto.getSpecialization());
        row.setAttribute(Template.WORKER_BASICSALARY, dto.getBasicSalary());
        row.setAttribute(Template.WORKER_SOCIALAL_LOWANCE, dto.getSocialAllowance());
        row.setAttribute(Template.WORKER_TRANSPORT_ALLOWANCE, dto.getTransportAllowance());
        row.setAttribute(Template.WORKER_WORK_NATURE_ALLOWANCE, dto.getWorkNatureAllowance());
        row.setAttribute(Template.WORKER_HOUSING_ALLOWANCE, dto.getHousingAllowance());
        row.setAttribute(Template.WORKER_OTHER_ALLOWANCE, dto.getOtherAllowance());
        row.setAttribute(Template.WORKER_TOTAL_MONTHLY_SALARY, dto.getTotalMonthlySalary());
        row.setAttribute(Template.WORKER_HOUSING_TYPE, dto.getHousingType());
        row.setAttribute(Template.WORKER_ADDITIONAL_NOTES, dto.getAdditionalNotes());
        vo.insertRow(row);

    }


}
