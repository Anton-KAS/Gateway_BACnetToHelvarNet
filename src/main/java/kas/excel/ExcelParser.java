package kas.excel;

import java.io.*;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;

public class ExcelParser {
    private final Logger logger;
    private final String fileName;
    private final String filePath;

    public ExcelParser() {
        this.logger = Logger.getLogger(ExcelParser.class);
        this.fileName = "configPoints.xlsx";
        this.filePath = System.getProperty("user.dir") + "\\" + fileName;
    }

    public ExcelParser(String dirPath, String fileName) {
        this.logger = Logger.getLogger(ExcelParser.class);
        this.fileName = fileName;
        this.filePath = dirPath + fileName;
    }

    public JSONObject parseXlsxToJson() {
        try {
            logger.info("Start parseXlsxToJson");

            File file = new File(filePath);

            logger.info("Excel file " + filePath + " exist: " + file.exists());
            if (!file.exists()) {
                logger.error("Excel file " + filePath + " not exist");
                return null;
            }
            logger.info("Start parse excel file " + filePath);
            FileInputStream inputStream = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            JSONObject jsonData = new JSONObject();

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                XSSFSheet sheet = wb.getSheetAt(i);

                // Пропускаем информационные страницы в Excel
                if (sheet.getSheetName().equals(DefaultSheets.DESCRIPTION.toString()) |
                        sheet.getSheetName().equals(DefaultSheets.PATTERN.toString()) |
                        sheet.getSheetName().equals(DefaultSheets.SETTINGS.toString())) {
                    continue;
                }

                JSONObject jsonController = new JSONObject();
                JSONObject jsonPoints = new JSONObject();

                String[] mainHeaders = DefaultHeader.getMainHeaders();
                int headRowIndex = -1;
                int[] headColIndexes = new int[mainHeaders.length];
                for (Row row : sheet) {
                    JSONObject jsonPoint = new JSONObject();
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        int cellColumnIndex = cell.getColumnIndex();
                        int cellRowIndex = cell.getRowIndex();

                        Map<String, Integer> headers = DefaultHeader.getMainHeadersMap();
                        Map<Integer, String> defColName = DefaultHeader.getDefColNameMap();
                        String cellValue;

                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue().trim();
                                if (cellIterator.hasNext() && headRowIndex == -1) {
                                    // Поиск параметров для контроллера Helvar
                                    findControllerParameter(cellIterator, cellValue, jsonController);
                                }

                                for (String header : headers.keySet()) {
                                    if (headRowIndex == -1 | cellRowIndex == headRowIndex) {
                                        // Поиск номера столбца по названию
                                        if (cellValue.equals(header)) {
                                            headColIndexes[headers.get(header)] = cellColumnIndex;
                                            if (headRowIndex == -1) headRowIndex = cellRowIndex;
                                        }
                                    } else if (cellRowIndex > headRowIndex) {
                                        // Поиск данных в таблице
                                        searchTableBody(headColIndexes, cellColumnIndex, headers, header, cellValue, jsonPoint);
                                    }
                                }
                                break;

                            case NUMERIC:
                                double doubleValue = cell.getNumericCellValue();
                                cellValue = String.valueOf((int) doubleValue);
                                // Поисх данных по точкам
                                if (headRowIndex != -1 && cell.getRowIndex() > headRowIndex) {
                                    for (String header : headers.keySet()) {
                                        // Поиск данных в таблице
                                        searchTableBody(headColIndexes, cellColumnIndex, headers, header, cellValue, jsonPoint);
                                    }
                                }
                                break;
                            default:
                        }
                    }

                    if (headRowIndex != -1 && row.getRowNum() > headRowIndex && jsonPoint.get(DefaultHeader.HELVAR_GROUP.toString()) != null) {
                        jsonPoints.put(jsonPoint.get(DefaultHeader.HELVAR_GROUP.toString()).toString(), jsonPoint);
                    }
                }
                jsonController.put("Points", jsonPoints);
                jsonData.put(jsonController.get(DefaultHeader.IP_CONTROLLER.toString()).toString(), jsonController);
            }
            logger.info("END parseXlsxToJson");
            return jsonData;

        } catch (IOException e) {
            logger.error(e.toString());
            return null;
        }

    }

    private static void findControllerParameter(Iterator<Cell> cellIterator, String cellValue, JSONObject jsonController) {
        String parameterNameToAdd;
        boolean finalDataIsInt = false;
        if (cellValue.equals(DefaultHeader.IP_CONTROLLER.getHeader())) {
            parameterNameToAdd = DefaultHeader.IP_CONTROLLER.toString();
        } else if (cellValue.equals(DefaultHeader.PORT_CONTROLLER.getHeader())) {
            parameterNameToAdd = DefaultHeader.PORT_CONTROLLER.toString();
            finalDataIsInt = true;
        } else if (cellValue.equals(DefaultHeader.LIGHT_PANEL.getHeader())) {
            parameterNameToAdd = DefaultHeader.LIGHT_PANEL.toString();
        } else if (cellValue.equals(DefaultHeader.CONTROLLER_REGISTER.getHeader())) {
            parameterNameToAdd = DefaultHeader.CONTROLLER_REGISTER.toString();
            finalDataIsInt = true;
        } else return;

        Cell cell = cellIterator.next();

        String nextStringValue = null;
        int nextIntValue = -1;

        switch (cell.getCellType()) {
            case STRING:
                nextStringValue = cell.getStringCellValue();
                break;
            case NUMERIC:
                nextIntValue = (int) cell.getNumericCellValue();
                break;
        }

        if (cellValue.equals(DefaultHeader.IP_CONTROLLER.getHeader()) & !checkIP(nextStringValue)) return;

        if (finalDataIsInt) {
            if (nextStringValue != null && nextStringValue.length() > 0) {
                nextIntValue = Integer.parseInt(nextStringValue);
            }
            jsonController.put(parameterNameToAdd, nextIntValue);
        } else {
            if (nextStringValue == null && nextIntValue != -1) {
                nextStringValue = String.valueOf(nextIntValue);
            }
            jsonController.put(parameterNameToAdd, nextStringValue);
        }

        // Добавим поле со статусом контроллера (по умолчанию "Не в сети")
        if (cellValue.equals(DefaultHeader.IP_CONTROLLER.getHeader())) jsonController.put("STATUS", false);
    }

    private static void searchTableBody(int[] headColIndexes, int cellColumnIndex,
                             Map<String, Integer> headers, String header, String cellValue,
                             JSONObject jsonPoint) {
        Map<Integer, String> defColName = DefaultHeader.getDefColNameMap();
        if (headColIndexes[headers.get(header)] == cellColumnIndex) {
            Object putValue;
            if (headers.get(header) == 0 | headers.get(header) == 4) {
                putValue = Integer.parseInt(cellValue);
            } else if (headers.get(header) == 3) {
                putValue = Boolean.parseBoolean(cellValue);
            } else {
                putValue = cellValue;
            }
            jsonPoint.put(defColName.get(headers.get(header)), putValue);
        }
    }

    // TODO: Подумать, может выделить в отдельный класс
    public static boolean checkIP(String ip) {
        if (ip == null) return false;

        List<String> ipParts = List.of(ip.trim().split("\\."));
        if (ipParts.size() != 4) return false;

        String[] stringPatterns = {"0{0,2}[1-9]", "0?[1-9][0-9]", "1[0-9]{1,2}", "2[0-4][0-9]", "25[0-4]"};
        Pattern[] patterns = new Pattern[stringPatterns.length];

        for (int n = 0; n < stringPatterns.length; n++) {
            patterns[n] = Pattern.compile(stringPatterns[n]);
        }
        int n = 0;
        for (String part : ipParts) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(part).matches()) {
                    n++;
                    break;
                }
            }
        }
        return n == 4;
    }
}
