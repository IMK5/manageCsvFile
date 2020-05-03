package view.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.client.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.model.UploadedFile;


public class CommonService {


    public static boolean isCsvFile(UploadedFile uploadedFile) {
        return uploadedFile.getFilename().endsWith("csv");
    }

    /**
     * Build summary of uploaded file
     * @param uploadedFile
     * @return FileInfoDto
     */
    public static FileInfoDto buildFileInfo(UploadedFile uploadedFile) {
        FileInfoDto fileInfoDto = new FileInfoDto();
        fileInfoDto.setFileName(uploadedFile.getFilename());
        fileInfoDto.setFileType("CSV");
        fileInfoDto.setFileSize(String.valueOf(uploadedFile.getLength()));
        return fileInfoDto;
    }

    public static Date convertStringToDate(String sDate, String format) throws ParseException {
        //Instantiating the SimpleDateFormat class
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        //Parsing the given String to Date object
        Date date = formatter.parse(sDate);
        return date;
    }


    /**
     * Convert Array to String
     * @param array represents one line from CSV file
     * @return
     */
    public static String convertArrayToString(String[] array) {
        String result = "";
        if (array != null && array.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                // To avoid empty String
                if (!StringUtils.isBlank(s)) {
                    sb.append(s).append(",");
                }

            }
            if (!StringUtils.isBlank(sb)) {
                result = sb.deleteCharAt(sb.length() - 1).toString();
            }
        }

        return result;
    }

    public static ApplicationModule getConfig() {
        String amDef = "model.AppModule";
        String config = "AppModuleLocal";
        ApplicationModule am = Configuration.createRootApplicationModule(amDef, config);
        return am;
    }


    /**
     * refresh table
     * @param viewIterartor
     */
    public static void refreshTable(String viewIterartor) {
        DCIteratorBinding iter = (DCIteratorBinding) BindingContext.getCurrent()
                                                                   .getCurrentBindingsEntry()
                                                                   .get(viewIterartor);
        iter.getViewObject().executeQuery();
    }

    public static void shoeInlineMessage(String message) {
        FacesMessage msg = new FacesMessage(message);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        FacesContext fctx = FacesContext.getCurrentInstance();
        fctx.addMessage(null, msg);
    }

    /**
     *
     * @param vo; ViewObject
     * @param IdAttributeName : attribute name of Id in DB
     * @param idValue : value of Id
     */
    public static void deleteRowById(ViewObject vo, String IdAttributeName, String idValue) {
        vo.setWhereClause(IdAttributeName + "=" + idValue);
        vo.executeQuery();
        Row delRow = vo.first();
        delRow.remove();
    }

    public static void refreshComponentUI(UIComponent p1) {
        AdfFacesContext.getCurrentInstance().addPartialTarget(p1);
    }

}
