package io.akhil.printapp.controller;

import io.akhil.printapp.domain.PrintDoc;
import io.akhil.printapp.domain.PrintRequest;
import io.akhil.printapp.domain.PrintResponse;
import io.akhil.printapp.enums.ContentType;
import io.akhil.printapp.enums.PrintDocOrientation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterName;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.util.Base64;
import java.util.Locale;

@RestController
@Slf4j
public class PrintController {

    //@RequestMapping(value = "print/labels", method = RequestMethod.POST)
    @PostMapping("print/labels")
    public PrintResponse printLabels(@RequestBody PrintRequest printRequest) {
        PrintResponse printResponse = new PrintResponse();
        printResponse.setStatus(PrintResponse.Status.SUCCESS);
        printResponse.setStatusCode(PrintResponse.Status.SUCCESS.getValue());

        PrintRequest.validatePrintRequest(printRequest, printResponse);

        if(printResponse.getErrors().size()> 0) {
            printResponse.setStatus(PrintResponse.Status.FAILURE);
            printResponse.setStatusCode(PrintResponse.Status.FAILURE.getValue());
            return printResponse;
        }
        return print(printRequest, printResponse);
    }

    private PrintResponse print(PrintRequest printRequest, PrintResponse printResponse) {
        AttributeSet attrSet = new HashPrintServiceAttributeSet(new PrinterName(printRequest.getPrinterName().trim(), Locale.getDefault()));
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, attrSet);
        log.debug("PS1 size is `{}`.", printServices.length);

        if(printServices.length==0) {
            log.error("Printer not found with name `{}`.", printRequest.getPrinterName().trim());
            printResponse.addError("Printer not found with name `" +printRequest.getPrinterName().trim()+ "`.");
            printResponse.setStatus(PrintResponse.Status.FAILURE);
            printResponse.setStatusCode(PrintResponse.Status.FAILURE.getValue());
            return printResponse;
        }

        PrintService printService = printServices[0];
        log.debug("Printer found with printer name `{}`.", printService.getName());

        DocPrintJob job = printService.createPrintJob();
        job.addPrintJobListener(printJobAdapter());

        for(PrintDoc printDoc : printRequest.getDocuments()) {

            byte[] labelData = new byte[0];
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            DocFlavor[] supportedDocFlavors = printService.getSupportedDocFlavors();

            if(supportedDocFlavors.length==0) {
                log.error("");
                printResponse.addError("");
                printResponse.setStatus(PrintResponse.Status.FAILURE);
                printResponse.setStatusCode(PrintResponse.Status.FAILURE.getValue());
                return printResponse;
            }

            if(ContentType.RAW.name().equalsIgnoreCase(printDoc.getContentType().trim())) { //for ZPL, for raw data
                //byte[] labelData = Base64.getDecoder().decode(printDoc.getContent());
                labelData = Base64.getDecoder().decode(printDoc.getContent());
                for(DocFlavor docFlavor : supportedDocFlavors) {
                    if(docFlavor.getClass().isAssignableFrom(DocFlavor.BYTE_ARRAY.class)) {
                        flavor = docFlavor;
                        break;
                    }
                }
                //flavor = supportedDocFlavors[0];

                //flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            }
            else if(ContentType.PDF.name().equalsIgnoreCase(printDoc.getContentType().trim())) { //docment in the form of PDF, for images
                //byte[] labelData = Base64.getDecoder().decode(printDoc.getContent());
                labelData = Base64.getDecoder().decode(printDoc.getContent());
                //flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE; //TODO: this does not work, autosense does not seem working for this printer

                flavor = new DocFlavor("application/pdf", DocFlavor.BYTE_ARRAY.class.getName()); //TODO: need to test it to make sure it works
                flavor = new DocFlavor("application/octet-stream", DocFlavor.BYTE_ARRAY.class.getName()); //TODO: need to test it to make sure it works
            }

            DocAttributeSet docAttributeSet = new HashDocAttributeSet();
            if(printDoc.getOrientation()==null || printDoc.getOrientation().trim().isEmpty()) {
                docAttributeSet.add(OrientationRequested.LANDSCAPE);
            } else {
                if(PrintDocOrientation.LANDSCAPE.name().equalsIgnoreCase(printDoc.getOrientation().trim())) {
                    docAttributeSet.add(OrientationRequested.LANDSCAPE);
                } else if (PrintDocOrientation.PORTRAIT.name().equalsIgnoreCase(printDoc.getOrientation().trim())) {
                    docAttributeSet.add(OrientationRequested.PORTRAIT);
                }
            }

            Doc doc = new SimpleDoc(labelData, flavor, docAttributeSet);

            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            printRequestAttributeSet.add(new Copies(printDoc.getNumOfCopies()));
            printRequestAttributeSet.add(new JobName(printRequest.getJobName().trim(), Locale.getDefault()));
            //printRequestAttributeSet.addAll(docAttributeSet);

            try {
                job.print(doc, printRequestAttributeSet);
            } catch (PrintException e) {
                log.error("** Exception occurred while printing, with message `{}`.", e.getMessage(), e);
                e.printStackTrace();
                printResponse.addError("** Exception occurred while printing, with message `" +e.getMessage()+ "`.");
                printResponse.setStatus(PrintResponse.Status.FAILURE);
                printResponse.setStatusCode(PrintResponse.Status.FAILURE.getValue());
                return printResponse;
            }
        }

        return printResponse;
    }

    private PrintJobAdapter printJobAdapter() {
        return new PrintJobAdapter() {
            /**
             * Called to notify the client that data has been successfully transferred to the print service, and the client may free
             * local resources allocated for that data.  The client should not assume that the data has been completely printed after
             * receiving this event.
             * @param pje the event being notified
             */
            @Override
            public void printDataTransferCompleted(PrintJobEvent pje) {
                super.printDataTransferCompleted(pje);
                log.debug("`{}`", "printDataTransferCompleted");
            }

            /**
             * Called to notify the client that the job completed successfully.
             * @param pje the event being notified
             */
            @Override
            public void printJobCompleted(PrintJobEvent pje) {
                super.printJobCompleted(pje);
                log.debug("`{}`", "printJobCompleted");
            }

            /**
             * Called to notify the client that the job failed to complete successfully and will have to be resubmitted.
             * @param pje the event being notified
             */
            @Override
            public void printJobFailed(PrintJobEvent pje) {
                super.printJobFailed(pje);
                log.debug("`{}`", "printJobFailed");
            }

            /**
             * Called to notify the client that the job was canceled
             * by user or program.
             * @param pje the event being notified
             */
            @Override
            public void printJobCanceled(PrintJobEvent pje) {
                super.printJobCanceled(pje);
                log.debug("`{}`", "printJobCanceled");
            }

            /**
             * Called to notify the client that no more events will be delivered. One cause of this event being generated is if the job
             * has successfully completed, but the printing system is limited in capability and cannot verify this.
             * This event is required to be delivered if none of the other terminal events (completed/failed/canceled) are delivered.
             * @param pje the event being notified
             */
            @Override
            public void printJobNoMoreEvents(PrintJobEvent pje) {
                super.printJobNoMoreEvents(pje);
                log.debug("`{}`", "printJobNoMoreEvents");
            }

            /**
             * Called to notify the client that some possibly user rectifiable problem occurs (eg printer out of paper).
             * @param pje the event being notified
             */
            @Override
            public void printJobRequiresAttention(PrintJobEvent pje) {
                super.printJobRequiresAttention(pje);
                log.debug("`{}`", "printJobRequiresAttention");
            }
        };
    }

}
