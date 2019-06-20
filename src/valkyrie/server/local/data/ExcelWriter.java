package valkyrie.server.local.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import server.data.EmployeeTimesheet;
import server.data.Job;
import valkyrie.server.local.data.config.ServerConfig;
import valkyrie.server.logging.messages.EmployeeLogMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Creates/updates excel document for each employee which contains their current and previous timesheet information.

public class ExcelWriter {
    private static final Logger logger = LogManager.getLogger(ExcelWriter.class.getName());

    private String fileName = "workbook.xls", sheetName = "Timesheet";

    private NPOIFSFileSystem fs = null;
    private ExecutorService excelExecutor;

    private CellStyle headerStyle, rightAlignStyle, dateCellStyle, timeCellStyle;

    private ExcelWriter() {
        excelExecutor = Executors.newSingleThreadExecutor();
    }

    public static ExcelWriter getInstance() {
        return ExcelWriter.SingletonHelper.INSTANCE;
    }

    private static void log(String log) {
        logger.info(log);
    }

    public void generateAllExcels(ArrayList<EmployeeTimesheet> timesheets) {
        logger.traceEntry();
        String path = ServerConfig.getInstance().getExcelPath();
        log("Excel Path: " + path);
        for (EmployeeTimesheet t : timesheets) {
            excelExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    logger.trace(new EmployeeLogMessage(t));
                    generateExcel(t, path);
                }
            });
        }
        logger.traceExit();
    }

    public void generateSingleExcel(EmployeeTimesheet timesheet) {
        logger.traceEntry();
        String path = ServerConfig.getInstance().getExcelPath();
        log("Excel Path: " + path);
        excelExecutor.execute(() -> {
            logger.trace(new EmployeeLogMessage(timesheet));
            generateExcel(timesheet, path);
        });
        logger.traceExit();
    }

    private void generateExcel(EmployeeTimesheet timesheet, String path) {
        if (timesheet == null) return;

        String name = timesheet.getFirstName() + " " + timesheet.getLastName();
        log("Creating excel: " + name);
        boolean isNewWorkbook = false;

        fileName =  name + ".xls";
        Workbook wb = null;
        Sheet sheet = null;
        File f = new File(path + "\\" + fileName);
        logger.trace(path + "\\" + fileName);
        logger.info("File exists: " + f.exists());
        if(f.exists()){
            try{
                logger.info("Opening workbook...");
                fs = new NPOIFSFileSystem(f);
                wb = new HSSFWorkbook(fs.getRoot(), true);
                sheet = wb.getSheet(sheetName);
                fs.close();
            }catch (IOException e){
                logger.error("Error opening excel file.", e);
                fileName = fileName + "_new";
            }
        } else {
            logger.info("Creating new file: " + path + "\\" + fileName);

            wb = new HSSFWorkbook();
            sheet = wb.createSheet(sheetName);
            isNewWorkbook = true;
        }
        // Cell Styles
        createCellStyles(wb);

        if(isNewWorkbook){
            Row nameRow = sheet.createRow(0);
            Cell cell = nameRow.createCell(0);
            cell.setCellValue(name);

            createHeaders(sheet);
        }


        int lastRow = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRow + 1);


        /*
            Add timesheet data to excel doc
          */

        // Date
        Cell cellDate = row.createCell(0);
        cellDate.setCellStyle(dateCellStyle);
        cellDate.setCellValue(timesheet.getDate());

        // Start Time
        Cell cellStartTime = row.createCell(1);
        cellStartTime.setCellStyle(timeCellStyle);
        if(timesheet.getPunchInTime() != null){
            cellStartTime.setCellValue(timesheet.getPunchInTime());
        } else {
            cellStartTime.setCellValue("");
        }

        // End Time
        Cell cellEndTime = row.createCell(2);
        cellEndTime.setCellStyle(timeCellStyle);
        if(timesheet.getPunchOutTime() != null){
            cellEndTime.setCellValue(timesheet.getPunchOutTime());
        } else {
            cellEndTime.setCellValue("");
        }

        //Total Hours
        Cell cellTh = row.createCell(3);
        if(timesheet.getPunchInTime() != null && timesheet.getPunchOutTime() != null){
            cellTh.setCellValue(getTimeDifference(timesheet.getPunchInTime(), timesheet.getPunchOutTime()));
        } else {
            cellTh.setCellValue("");
        }
        cellTh.setCellStyle(rightAlignStyle);

        // Jobs
        if(!timesheet.getJobs().isEmpty() && timesheet.getJobs() != null){
            int col = row.getLastCellNum();
            for (Job j : timesheet.getJobs()){
                Cell c1 = row.createCell(col);
                c1.setCellStyle(rightAlignStyle);
                c1.setCellValue(j.getJobNumber());

                Cell c2 = row.createCell(++col);
                c2.setCellStyle(timeCellStyle);
                if(j.getStartTime() != null && j.getEndTime() != null){
                    c2.setCellValue(getTimeDifference(j.getStartTime(), j.getEndTime()));
                }
                col++;
            }
        }

        for(int i = 0; i < sheet.getRow(2).getLastCellNum(); i++){
            sheet.autoSizeColumn(i);
        }

        try{
            OutputStream fileOut = new FileOutputStream(path + "\\" + fileName);
            wb.write(fileOut);
            fileOut.close();
            wb.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        File file = new File(path + "\\" + fileName);
        log("Excel file " + path + "\\" + fileName + " created.");
        log("Excel file " + path + "\\" + fileName + " exists: " + file.exists());
    }

    private static class SingletonHelper {
        private static final ExcelWriter INSTANCE = new ExcelWriter();
    }

    private void createHeaders(Sheet sheet){
        Row headerRow = sheet.createRow(2);
        Cell hcDate = headerRow.createCell(0);
        hcDate.setCellValue("Date");
        hcDate.setCellStyle(headerStyle);

        Cell hcStartTime = headerRow.createCell(1);
        hcStartTime.setCellValue("Start Time");
        hcStartTime.setCellStyle(headerStyle);

        Cell hcEndTime = headerRow.createCell(2);
        hcEndTime.setCellValue("End Time");
        hcEndTime.setCellStyle(headerStyle);

        Cell hcTh = headerRow.createCell(3);
        hcTh.setCellValue("Total Hours");
        hcTh.setCellStyle(headerStyle);

        for(int i = 0; i < 6; i++){
            Cell hcJobNumber = headerRow.createCell(headerRow.getLastCellNum());
            hcJobNumber.setCellValue("Job " + (i+1));
            hcJobNumber.setCellStyle(headerStyle);
            Cell hcJobTime = headerRow.createCell(headerRow.getLastCellNum());
            hcJobTime.setCellValue("Job " + (i+1) + " Time");
            hcJobTime.setCellStyle(headerStyle);
        }
    }

    private void createCellStyles(Workbook wb){
        CreationHelper createHelper = wb.getCreationHelper();
        headerStyle = wb.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        rightAlignStyle = wb.createCellStyle();
        rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);

        dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yyyy"));
        dateCellStyle.setAlignment(HorizontalAlignment.RIGHT);

        timeCellStyle = wb.createCellStyle();
        timeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("h:mm.ss AM/PM"));
        timeCellStyle.setAlignment(HorizontalAlignment.RIGHT);
    }

    private static String getTimeDifference(Date startDate, Date endDate){

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        //long elapsedDays = different / daysInMilli;
        //different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        return elapsedHours + ":" + elapsedMinutes + ":" + elapsedSeconds;

    }
}
