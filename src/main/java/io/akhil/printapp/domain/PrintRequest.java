package io.akhil.printapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.akhil.printapp.enums.ContentType;
import io.akhil.printapp.enums.PrintDocOrientation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintRequest implements Serializable {

    private String jobName;
    private String printerName;
    private String printerIp;
    private String printerPort;

    private List<PrintDoc> documents;

    public List<PrintDoc> getDocuments() {
        if(documents==null) {
            return new ArrayList<>();
        }
        return documents;
    }

    public static void validatePrintRequest(PrintRequest printRequest, PrintResponse printResponse) {
        if(printRequest==null) {
            log.error("`{}`", "Please provide data for printing.");
            printResponse.addError("Please provide data for printing.");
        }
        if(printRequest.getJobName()==null || printRequest.getJobName().trim().isEmpty()) {
            log.error("`{}`", "Please provide Job Name.");
            printResponse.addError("Please provide Job Name.");
        }
        if(printRequest.getPrinterName()==null || printRequest.getPrinterName().trim().isEmpty()) {
            log.error("`{}`", "Please provide Printer Name.");
            printResponse.addError("Please provide Printer Name.");
        }
        if(printRequest.getDocuments()==null || printRequest.getDocuments().size()==0) {
            log.error("`{}`", "Please document/data for the printing.");
            printResponse.addError("Please document/data for the printing.");
        }
        printRequest.getDocuments().forEach(printDoc -> {
            if(printDoc.getContentType()==null || ContentType.forValue(printDoc.getContentType().trim())==null) {
                log.error("`{}` is not a valid Content Type. Valid Content Types are `{}`.",
                        printDoc.getContentType(), Arrays.toString(ContentType.values()));
                printResponse.addError("`" +printDoc.getContentType()+ "` is not a valid Content Type. " +
                        "Valid Content Types are `" +Arrays.toString(ContentType.values())+ "`.");
            }
//            if(printDoc.getFileType()==null || FileType.forValue(printDoc.getFileType().trim())==null) {
//                log.error("`{}` is not a valid File Type. Valid File Types are `{}`.",
//                        printDoc.getFileType(), Arrays.toString(FileType.values()));
//                printResponse.addError("`" +printDoc.getFileType()+ "` is not a valid File Type. " +
//                        "Valid File Types are `" +Arrays.toString(FileType.values())+ "`.");
//            }
            if(printDoc.getOrientation()!=null && !printDoc.getOrientation().trim().isEmpty()
                    && !PrintDocOrientation.LANDSCAPE.name().equalsIgnoreCase(printDoc.getOrientation().trim())
                    && !PrintDocOrientation.PORTRAIT.name().equalsIgnoreCase(printDoc.getOrientation().trim()))
            {
                log.error("`{}` is not a valid document orientation type.", printDoc.getOrientation().trim());
                printResponse.addError("`" +printDoc.getOrientation().trim()+ "` is not a valid document orientation type.");
            }
            if(printDoc.getContent()==null || printDoc.getContent().trim().isEmpty()) {
                log.error("`{}`", "Please provide document content to be printed.");
                printResponse.addError("Please provide document content to be printed.");
            }
        });
    }

}
