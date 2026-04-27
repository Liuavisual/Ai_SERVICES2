package com.delta.common.util;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    public static <T> void export(HttpServletResponse response, String fileName,
                                   LinkedHashMap<String, String> headers,
                                   List<T> dataList,
                                   RowMapper<T> rowMapper) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(fileName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        int colIdx = 0;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue(entry.getValue());
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> rowData = rowMapper.mapRow(dataList.get(i));
            colIdx = 0;
            for (String key : headers.keySet()) {
                Object val = rowData.get(key);
                Cell cell = row.createCell(colIdx++);
                setCellValue(cell, val);
            }
        }

        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(currentWidth + 1000, 15000));
        }

        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedName + ".xlsx; filename*=UTF-8''" + encodedName + ".xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public static List<Map<String, String>> importExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            return new ArrayList<>();
        }

        List<String> headerKeys = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            headerKeys.add(cell != null ? getCellStringValue(cell) : "col_" + i);
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            boolean allEmpty = true;
            Map<String, String> rowData = new LinkedHashMap<>();
            for (int j = 0; j < headerKeys.size(); j++) {
                Cell cell = row.getCell(j);
                String val = cell != null ? getCellStringValue(cell) : "";
                if (!val.isEmpty()) allEmpty = false;
                rowData.put(headerKeys.get(j), val);
            }
            if (!allEmpty) {
                result.add(rowData);
            }
        }

        workbook.close();
        return result;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static void setCellValue(Cell cell, Object val) {
        if (val == null) {
            cell.setCellValue("");
        } else if (val instanceof Number) {
            cell.setCellValue(((Number) val).doubleValue());
        } else if (val instanceof Boolean) {
            cell.setCellValue((Boolean) val);
        } else {
            cell.setCellValue(val.toString());
        }
    }

    private static String getCellStringValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    yield String.valueOf((long) d);
                }
                yield String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue().trim();
            default -> "";
        };
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        Map<String, Object> mapRow(T item);
    }
}
