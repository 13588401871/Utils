package com.ruoyi.common.utils;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author chenxd
 * @create 2019-07-09 11:48
 */
public class PoiExcelToHtmlUtil {
    private static Map<String, Object> map[];

    /**
     * 程序入口方法（读取指定位置的excel，将其转换成html形式的字符串，并保存成同名的html文件在相同的目录下，默认带样式）
     *
     * @return <table>...</table> 字符串
     */
    public static String excelWriteToHtml(String sourcePath) {
        File sourceFile = new File(sourcePath);
        String htmlFilePath;
        int suffixlength;
        File htmlFile;

        //截取文件名转为html
        suffixlength = sourcePath.substring(sourcePath.lastIndexOf(".")).length() - 1;
        htmlFilePath = sourcePath.substring(0, sourcePath.length() - suffixlength) + "html";


        try {
            InputStream fis = new FileInputStream(sourceFile);

            long start1 = System.currentTimeMillis();
            System.out.println("开始读取!");
            String excelHtml = PoiExcelToHtmlUtil.readExcelToHtml(fis, true);
            long end1 = System.currentTimeMillis();
            System.out.println("读取完成!用时" + (end1 - start1));
            long start2 = System.currentTimeMillis();
            System.out.println("开始写入");

            htmlFile = new File(htmlFilePath);
            if (!htmlFile.exists()) {
                htmlFile.createNewFile();
            }
            byte bytes[] = new byte[512];
            bytes = excelHtml.getBytes();
            int blength = bytes.length;
            FileOutputStream fos = new FileOutputStream(htmlFile);
            fos.write(bytes, 0, blength);
            fos.close();

            long end2 = System.currentTimeMillis();
            System.out.println("写入完成!用时" + (end2 - start2));
            System.out.println("共用时" + (end2 - start1));

            return htmlFilePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 程序入口方法（将指定路径的excel文件读取成字符串）
     *
     * @param filePath    文件的路径
     * @param isWithStyle 是否需要表格样式 包含 字体 颜色 边框 对齐方式
     * @return <table>...</table> 字符串
     */
    public static String readExcelToHtml(String filePath, boolean isWithStyle) {
        InputStream is = null;
        String htmlExcel = null;
        try {
            File sourcefile = new File(filePath);
            is = new FileInputStream(sourcefile);
            Workbook wb = WorkbookFactory.create(is);
            htmlExcel = readWorkbook(wb, isWithStyle);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlExcel;
    }

    /**
     * 程序入口方法（将指定路径的excel文件读取成字符串）
     *
     * @param is          excel转换成的输入流
     * @param isWithStyle 是否需要表格样式 包含 字体 颜色 边框 对齐方式
     * @return <table>...</table> 字符串
     */
    public static String readExcelToHtml(InputStream is, boolean isWithStyle) {
        String htmlExcel = null;
        try {
            Workbook wb = WorkbookFactory.create(is);
            htmlExcel = readWorkbook(wb, isWithStyle);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlExcel;
    }

    /**
     * 根据excel的版本分配不同的读取方法进行处理
     *
     * @param wb
     * @param isWithStyle
     * @return
     */
    private static String readWorkbook(Workbook wb, boolean isWithStyle) {
        String htmlExcel = "";
        if (wb instanceof XSSFWorkbook) {
            XSSFWorkbook xWb = (XSSFWorkbook) wb;
            htmlExcel = getExcelInfo(xWb, isWithStyle);
        } else if (wb instanceof HSSFWorkbook) {
            HSSFWorkbook hWb = (HSSFWorkbook) wb;
            htmlExcel = getExcelInfo(hWb, isWithStyle);
        }
        return htmlExcel;
    }

    /**
     * 读取excel成string
     *
     * @param wb
     * @param isWithStyle
     * @return
     */
    public static String getExcelInfo(Workbook wb, boolean isWithStyle) {

        StringBuffer sb = new StringBuffer();
        Sheet sheet = wb.getSheetAt(0);//获取第一个Sheet的内容
        // map等待存储excel图片
        Map<String, PictureData> sheetIndexPicMap = getSheetPictrues(0, sheet, wb);
        //临时保存位置，正式环境根据部署环境存放其他位置
        try {
            if (sheetIndexPicMap != null)
                printImg(sheetIndexPicMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //读取excel拼装html
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head>");
        sb.append("<meta charset=\"utf-8\">");
        sb.append("<meta name=\"viewport\" content=\"width=device-width\">");
        sb.append("<title>");
        sb.append("table</title>");
        sb.append("</head>");
        sb.append("<body>");

        int lastRowNum = sheet.getLastRowNum();
        map = getRowSpanColSpanMap(sheet);
        sb.append("<table style='border-collapse:collapse;width:100%;'>");
        Row row = null;
        Cell cell = null;

        //数据库读值填回值(此处忽略公式类型单元格)
        for (int rowNum = sheet.getFirstRowNum(); rowNum <= lastRowNum; rowNum++) {
            row = sheet.getRow(rowNum);
            int lastColNum = PoiExcelToHtmlUtil.getColsOfTable(sheet)[0];
            int rowHeight = PoiExcelToHtmlUtil.getColsOfTable(sheet)[1];
            for (int colNum = 0; colNum < lastColNum; colNum++) {
                cell = row.getCell(colNum);

                String stringValue = getCellValueIgnoreFormula(cell);

                //数据库读值填回值
                if (stringValue.contains("key")) {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    String url = "jdbc:mysql://127.0.0.1:3306/exceltest?characterEncoding=utf8&useSSL=false&serverTimezone=GMT";
                    Connection con = null;
                    try {
                        con = DriverManager.getConnection(url, "root", "root");
                        Statement statement = con.createStatement();

                        ResultSet resultSet2 = statement.executeQuery("select * from test");
                        while (resultSet2.next()) {
                            stringValue = resultSet2.getString("row");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            con.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cell.setCellValue(Integer.valueOf(stringValue));
                }

            }
        }


        for (int rowNum = sheet.getFirstRowNum(); rowNum <= lastRowNum; rowNum++) {
//            if(rowNum > 1000) break;
            row = sheet.getRow(rowNum);

            int lastColNum = PoiExcelToHtmlUtil.getColsOfTable(sheet)[0];
            int rowHeight = PoiExcelToHtmlUtil.getColsOfTable(sheet)[1];

            if (null != row) {
                lastColNum = row.getLastCellNum();
                rowHeight = row.getHeight();
            }

            if (null == row) {
                sb.append("<tr><td >  </td></tr>");
                continue;
            } else if (row.getZeroHeight()) {
                continue;
                //针对jxl的隐藏行（此类隐藏行只是把高度设置为0，单getZeroHeight无法识别）
            } else if (0 == rowHeight) {
                continue;
            }
            sb.append("<tr>");

            for (int colNum = 0; colNum < lastColNum; colNum++) {
                if (sheet.isColumnHidden(colNum)) continue;
                String imageRowNum = "0_" + rowNum + "_" + colNum;
                String imageHtml = "";
                cell = row.getCell(colNum);
                if ((sheetIndexPicMap != null && !sheetIndexPicMap.containsKey(imageRowNum) || sheetIndexPicMap == null) && cell == null) {    //特殊情况 空白的单元格会返回null+//判断该单元格是否包含图片，为空时也可能包含图片
                    sb.append("<td>  </td>");
                    continue;
                }
                if (sheetIndexPicMap != null && sheetIndexPicMap.containsKey(imageRowNum)) {
                    //待修改路径
                    String imagePath = "D:\\pic" + imageRowNum + ".jpeg";

                    imageHtml = "<img src='" + imagePath + "' style='height:" + rowHeight / 20 + "px;'>";
                }
                String stringValue = getCellValue(wb, cell);

                if (map[0].containsKey(rowNum + "," + colNum)) {
                    String pointString = (String) map[0].get(rowNum + "," + colNum);
                    int bottomeRow = Integer.valueOf(pointString.split(",")[0]);
                    int bottomeCol = Integer.valueOf(pointString.split(",")[1]);
                    int rowSpan = bottomeRow - rowNum + 1;
                    int colSpan = bottomeCol - colNum + 1;
                    if (map[2].containsKey(rowNum + "," + colNum)) {
                        rowSpan = rowSpan - (Integer) map[2].get(rowNum + "," + colNum);
                    }
                    sb.append("<td rowspan= '" + rowSpan + "' colspan= '" + colSpan + "' ");
                    if (map.length > 3 && map[3].containsKey(rowNum + "," + colNum)) {
                        //此类数据首行被隐藏，value为空，需使用其他方式获取值
                        stringValue = getMergedRegionValue(sheet, rowNum, colNum);
                    }
                } else if (map[1].containsKey(rowNum + "," + colNum)) {
                    map[1].remove(rowNum + "," + colNum);
                    continue;
                } else {
                    sb.append("<td ");
                }

                //判断是否需要样式
                if (isWithStyle) {
                    dealExcelStyle(wb, sheet, cell, sb);//处理单元格样式
                }

                sb.append(">");
                if (sheetIndexPicMap != null && sheetIndexPicMap.containsKey(imageRowNum)) sb.append(imageHtml);
                if (stringValue == null || "".equals(stringValue.trim())) {
                    sb.append("   ");
                } else {
                    // 将ascii码为160的空格转换为html下的空格（ ）
                    sb.append(stringValue.replace(String.valueOf((char) 160), " "));
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
            continue;
        }

        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 分析excel表格，记录合并单元格相关的参数，用于之后html页面元素的合并操作
     *
     * @param sheet
     * @return
     */
    private static Map<String, Object>[] getRowSpanColSpanMap(Sheet sheet) {
        Map<String, String> map0 = new HashMap<String, String>();    //保存合并单元格的对应起始和截止单元格
        Map<String, String> map1 = new HashMap<String, String>();    //保存被合并的那些单元格
        Map<String, Integer> map2 = new HashMap<String, Integer>();    //记录被隐藏的单元格个数
        Map<String, String> map3 = new HashMap<String, String>();    //记录合并了单元格，但是合并的首行被隐藏的情况
        int mergedNum = sheet.getNumMergedRegions();
        CellRangeAddress range = null;
        Row row = null;
        for (int i = 0; i < mergedNum; i++) {
            range = sheet.getMergedRegion(i);
            int topRow = range.getFirstRow();
            int topCol = range.getFirstColumn();
            int bottomRow = range.getLastRow();
            int bottomCol = range.getLastColumn();
            /**
             * 此类数据为合并了单元格的数据
             * 1.处理隐藏（只处理行隐藏，列隐藏poi已经处理）
             */
            if (topRow != bottomRow) {
                int zeroRoleNum = 0;
                int tempRow = topRow;
                for (int j = topRow; j <= bottomRow; j++) {
                    row = sheet.getRow(j);
                    if (row.getZeroHeight() || row.getHeight() == 0) {
                        if (j == tempRow) {
                            //首行就进行隐藏，将rowTop向后移
                            tempRow++;
                            continue;//由于top下移，后面计算rowSpan时会扣除移走的列，所以不必增加zeroRoleNum;
                        }
                        zeroRoleNum++;
                    }
                }
                if (tempRow != topRow) {
                    map3.put(tempRow + "," + topCol, topRow + "," + topCol);
                    topRow = tempRow;
                }
                if (zeroRoleNum != 0) map2.put(topRow + "," + topCol, zeroRoleNum);
            }
            map0.put(topRow + "," + topCol, bottomRow + "," + bottomCol);
            int tempRow = topRow;
            while (tempRow <= bottomRow) {
                int tempCol = topCol;
                while (tempCol <= bottomCol) {
                    map1.put(tempRow + "," + tempCol, topRow + "," + topCol);
                    tempCol++;
                }
                tempRow++;
            }
            map1.remove(topRow + "," + topCol);
        }
        Map[] map = {map0, map1, map2, map3};
//        System.err.println(map0);
        return map;
    }


    /**
     * 获取合并单元格的值
     *
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static String getMergedRegionValue(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();

            if (row >= firstRow && row <= lastRow) {

                if (column >= firstColumn && column <= lastColumn) {
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);

                    return getCellValue(fCell);
                }
            }
        }
        return null;
    }

    /**
     * 获取表格单元格Cell内容(忽略公式类型)
     *
     * @param cell
     * @return
     */
    private static String getCellValueIgnoreFormula(Cell cell) {
        String result = new String();
        switch (cell.getCellType()) {
            // 数字类型
            case Cell.CELL_TYPE_NUMERIC:
                result = numCellValue(cell);
                break;
            //公式类型
            case Cell.CELL_TYPE_FORMULA:
//                try {
//                    result = String.valueOf(cell.getNumericCellValue());
//                } catch (IllegalStateException e) {
//                    result = cell.getRichStringCellValue().toString();
//                }
                break;
            // String类型
            case Cell.CELL_TYPE_STRING:
                result = cell.getRichStringCellValue().toString();
                break;
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }


    /**
     * 获取表格单元格Cell内容
     *
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell) {
        return getCellValue(null, cell);
    }

    /**
     * 获取表格单元格Cell内容
     *
     * @param cell
     * @param wb
     * @return
     */
    private static String getCellValue(Workbook wb, Cell cell) {
        String result = new String();
        switch (cell.getCellType()) {
            // 数字类型
            case Cell.CELL_TYPE_NUMERIC:
                result = numCellValue(cell);
                break;
            //公式类型
            case Cell.CELL_TYPE_FORMULA:
                try {
                    if (wb == null) {
                        break;
                    }
                    //强制运算
                    wb.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(cell);
                    result = String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    result = cell.getRichStringCellValue().toString();
                }
                break;
            // String类型
            case Cell.CELL_TYPE_STRING:
                result = cell.getRichStringCellValue().toString();
                break;
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    /**
     * @return java.lang.String
     * @Author zeus
     * @Description 处理 数字格式单元格
     * @Date 8:47 2019/5/10
     * @Param [cell]
     **/
    private static String numCellValue(Cell cell) {
        String result;
        // 处理日期格式、时间格式
        if (HSSFDateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat sdf = null;
            if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm")) {
                sdf = new SimpleDateFormat("HH:mm");
            } else {
                // 日期
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            }
            Date date = cell.getDateCellValue();
            result = sdf.format(date);
        } else if (cell.getCellStyle().getDataFormat() == 58) {
            // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            double value = cell.getNumericCellValue();
            Date date = org.apache.poi.ss.usermodel.DateUtil
                    .getJavaDate(value);
            result = sdf.format(date);
        } else {
            double value = cell.getNumericCellValue();
            CellStyle style = cell.getCellStyle();
            DecimalFormat format = new DecimalFormat();
            String temp = style.getDataFormatString();
            // 单元格设置成常规
            if (temp.equals("General")) {
                format.applyPattern("#");
            }
            result = format.format(value);
        }
        return result;
    }

    /**
     * 处理表格样式
     *
     * @param wb
     * @param sheet
     * @param cell
     * @param sb
     */
    private static void dealExcelStyle(Workbook wb, Sheet sheet, Cell cell, StringBuffer sb) {
        CellStyle cellStyle = cell.getCellStyle();
        if (cellStyle != null) {
            HorizontalAlignment alignment = cellStyle.getAlignmentEnum();
            sb.append("align='" + convertAlignToHtml(alignment) + "' ");//单元格内容的水平对齐方式
            VerticalAlignment verticalAlignment = cellStyle.getVerticalAlignmentEnum();
            sb.append("valign='" + convertVerticalAlignToHtml(verticalAlignment) + "' ");//单元格中内容的垂直排列方式

            if (wb instanceof XSSFWorkbook) {

                XSSFFont xf = ((XSSFCellStyle) cellStyle).getFont();
                short boldWeight = xf.getFontHeight();
                sb.append("style='");
                sb.append("font-weight:" + boldWeight + ";"); // 字体加粗
                sb.append("font-size: " + xf.getFontHeight() / 2 + "%;"); // 字体大小

                int topRow = cell.getRowIndex(), topColumn = cell.getColumnIndex();
                if (map[0].containsKey(topRow + "," + topColumn)) {//该单元格为合并单元格，宽度需要获取所有单元格宽度后合并
                    String value = (String) map[0].get(topRow + "," + topColumn);
                    String[] ary = value.split(",");
                    int bottomColumn = Integer.parseInt(ary[1]);
                    if (topColumn != bottomColumn) {//合并列，需要计算相应宽度
                        int columnWidth = 0;
                        for (int i = topColumn; i <= bottomColumn; i++) {
                            columnWidth += sheet.getColumnWidth(i);
                        }
                        sb.append("width:" + columnWidth / 256 * xf.getFontHeight() / 20 + "pt;");
                    } else {
                        int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
                        sb.append("width:" + columnWidth / 256 * xf.getFontHeight() / 20 + "pt;");
                    }
                } else {
                    int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
                    sb.append("width:" + columnWidth / 256 * xf.getFontHeight() / 20 + "pt;");
                }

                XSSFColor xc = xf.getXSSFColor();
                if (xc != null && !"".equals(xc.toString())) {
                    sb.append("color:#" + xc.getARGBHex().substring(2) + ";"); // 字体颜色
                }

                XSSFColor bgColor = (XSSFColor) cellStyle.getFillForegroundColorColor();
                if (bgColor != null && !"".equals(bgColor.toString())) {
                    sb.append("background-color:#" + bgColor.getARGBHex().substring(2) + ";"); // 背景颜色
                }
                sb.append("border:solid #000000 1px;");
                //                sb.append(getBorderStyle(0,cellStyle.getBorderTop(), ((XSSFCellStyle) cellStyle).getTopBorderXSSFColor()));
                //                sb.append(getBorderStyle(1,cellStyle.getBorderRight(), ((XSSFCellStyle) cellStyle).getRightBorderXSSFColor()));
                //                sb.append(getBorderStyle(2,cellStyle.getBorderBottom(), ((XSSFCellStyle) cellStyle).getBottomBorderXSSFColor()));
                //                sb.append(getBorderStyle(3,cellStyle.getBorderLeft(), ((XSSFCellStyle) cellStyle).getLeftBorderXSSFColor()));
            } else if (wb instanceof HSSFWorkbook) {
                HSSFFont hf = ((HSSFCellStyle) cellStyle).getFont(wb);
                short boldWeight = hf.getFontHeight();
                short fontColor = hf.getColor();
                sb.append("style='");

                HSSFPalette palette = ((HSSFWorkbook) wb).getCustomPalette(); // 类HSSFPalette用于求的颜色的国际标准形式
                HSSFColor hc = palette.getColor(fontColor);
                sb.append("font-weight:" + boldWeight + ";"); // 字体加粗
                sb.append("font-size: " + hf.getFontHeight() / 2 + "%;"); // 字体大小
                String fontColorStr = convertToStardColor(hc);
                if (fontColorStr != null && !"".equals(fontColorStr.trim())) {
                    sb.append("color:" + fontColorStr + ";"); // 字体颜色
                }

                int topRow = cell.getRowIndex(), topColumn = cell.getColumnIndex();
                if (map[0].containsKey(topRow + "," + topColumn)) {//该单元格为合并单元格，宽度需要获取所有单元格宽度后合并
                    String value = (String) map[0].get(topRow + "," + topColumn);
                    String[] ary = value.split(",");
                    int bottomColumn = Integer.parseInt(ary[1]);
                    if (topColumn != bottomColumn) {//合并列，需要计算相应宽度
                        int columnWidth = 0;
                        for (int i = topColumn; i <= bottomColumn; i++) {
                            columnWidth += sheet.getColumnWidth(i);
                        }
                        sb.append("width:" + columnWidth / 256 * hf.getFontHeight() / 20 + "pt;");
                    } else {
                        int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
                        sb.append("width:" + columnWidth / 256 * hf.getFontHeight() / 20 + "pt;");
                    }
                } else {
                    int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
                    sb.append("width:" + columnWidth / 256 * hf.getFontHeight() / 20 + "pt;");
                }

                short bgColor = cellStyle.getFillForegroundColor();
                hc = palette.getColor(bgColor);
                String bgColorStr = convertToStardColor(hc);
                if (bgColorStr != null && !"".equals(bgColorStr.trim())) {
                    sb.append("background-color:" + bgColorStr + ";");        // 背景颜色
                }
                sb.append("border:solid #000000 1px;");
            }
            sb.append("' ");
        }
    }

    /**
     * 单元格内容的水平对齐方式
     *
     * @param alignment
     * @return
     */
    private static String convertAlignToHtml(HorizontalAlignment alignment) {
        String align = "left";
        switch (alignment) {
            case LEFT:
                align = "left";
                break;
            case CENTER:
                align = "center";
                break;
            case RIGHT:
                align = "right";
                break;
            default:
                break;
        }
        return align;
    }

    /**
     * 单元格中内容的垂直排列方式
     *
     * @param verticalAlignment
     * @return
     */
    private static String convertVerticalAlignToHtml(VerticalAlignment verticalAlignment) {
        String valign = "middle";
        switch (verticalAlignment) {
            case BOTTOM:
                valign = "bottom";
                break;
            case CENTER:
                valign = "center";
                break;
            case TOP:
                valign = "top";
                break;
            default:
                break;
        }
        return valign;
    }

    private static String convertToStardColor(HSSFColor hc) {
        StringBuffer sb = new StringBuffer("");
        if (hc != null) {
            if (HSSFColor.AUTOMATIC.index == hc.getIndex()) {
                return null;
            }
            sb.append("#");
            for (int i = 0; i < hc.getTriplet().length; i++) {
                sb.append(fillWithZero(Integer.toHexString(hc.getTriplet()[i])));
            }
        }
        return sb.toString();
    }

    private static String fillWithZero(String str) {
        if (str != null && str.length() < 2) {
            return "0" + str;
        }
        return str;
    }

    static String[] bordesr = {"border-top:", "border-right:", "border-bottom:", "border-left:"};
    static String[] borderStyles = {"solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid", "solid", "solid", "solid", "solid"};

    @SuppressWarnings("unused")
    private static String getBorderStyle(HSSFPalette palette, int b, short s, short t) {
        if (s == 0) return bordesr[b] + borderStyles[s] + "#d0d7e5 1px;";
        String borderColorStr = convertToStardColor(palette.getColor(t));
        borderColorStr = borderColorStr == null || borderColorStr.length() < 1 ? "#000000" : borderColorStr;
        return bordesr[b] + borderStyles[s] + borderColorStr + " 1px;";
    }

    @SuppressWarnings("unused")
    private static String getBorderStyle(int b, short s, XSSFColor xc) {
        if (s == 0) return bordesr[b] + borderStyles[s] + "#d0d7e5 1px;";
        if (xc != null && !"".equals(xc)) {
            String borderColorStr = xc.getARGBHex();//t.getARGBHex();
            borderColorStr = borderColorStr == null || borderColorStr.length() < 1 ? "#000000" : borderColorStr.substring(2);
            return bordesr[b] + borderStyles[s] + borderColorStr + " 1px;";
        }
        return "";
    }

    @SuppressWarnings("unused")
    private static void writeFile(String content, String path) {
        OutputStream os = null;
        BufferedWriter bw = null;
        try {
            File file = new File(path);
            os = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(os, "GBK"));
            bw.write(content);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (null != bw)
                    bw.close();
                if (null != os)
                    os.close();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }


    /**
     * 获取Excel图片公共方法
     *
     * @param sheetNum 当前sheet编号
     * @param sheet    当前sheet对象
     * @param workbook 工作簿对象
     * @return Map key:图片单元格索引（0_1_1）String，value:图片流PictureData
     */
    public static Map<String, PictureData> getSheetPictrues(int sheetNum, Sheet sheet, Workbook workbook) {
        if (workbook instanceof HSSFWorkbook) {
            return getSheetPictrues03(sheetNum, (HSSFSheet) sheet, (HSSFWorkbook) workbook);
        } else if (workbook instanceof XSSFWorkbook) {
            return getSheetPictrues07(sheetNum, (XSSFSheet) sheet, (XSSFWorkbook) workbook);
        } else {
            return null;
        }
    }

    /**
     * 获取Excel2003图片
     *
     * @param sheetNum 当前sheet编号
     * @param sheet    当前sheet对象
     * @param workbook 工作簿对象
     * @return Map key:图片单元格索引（0_1_1）String，value:图片流PictureData
     * @throws IOException
     */
    private static Map<String, PictureData> getSheetPictrues03(int sheetNum,
                                                               HSSFSheet sheet, HSSFWorkbook workbook) {

        Map<String, PictureData> sheetIndexPicMap = new HashMap<String, PictureData>();
        List<HSSFPictureData> pictures = workbook.getAllPictures();
        if (pictures.size() != 0) {
            for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                shape.getLineWidth();
                if (shape instanceof HSSFPicture) {
                    HSSFPicture pic = (HSSFPicture) shape;
                    int pictureIndex = pic.getPictureIndex() - 1;
                    HSSFPictureData picData = pictures.get(pictureIndex);
                    String picIndex = String.valueOf(sheetNum) + "_"
                            + String.valueOf(anchor.getRow1()) + "_"
                            + String.valueOf(anchor.getCol1());
                    sheetIndexPicMap.put(picIndex, picData);
                }
            }
            return sheetIndexPicMap;
        } else {
            return null;
        }
    }

    /**
     * 获取Excel2007图片
     *
     * @param sheetNum 当前sheet编号
     * @param sheet    当前sheet对象
     * @param workbook 工作簿对象
     * @return Map key:图片单元格索引（0_1_1）String，value:图片流PictureData
     */
    private static Map<String, PictureData> getSheetPictrues07(int sheetNum,
                                                               XSSFSheet sheet, XSSFWorkbook workbook) {
        Map<String, PictureData> sheetIndexPicMap = new HashMap<String, PictureData>();

        for (POIXMLDocumentPart dr : sheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) dr;
                List<XSSFShape> shapes = drawing.getShapes();
                for (XSSFShape shape : shapes) {
                    XSSFPicture pic = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = pic.getPreferredSize();
                    CTMarker ctMarker = anchor.getFrom();
                    String picIndex = String.valueOf(sheetNum) + "_"
                            + ctMarker.getRow() + "_" + ctMarker.getCol();
                    sheetIndexPicMap.put(picIndex, pic.getPictureData());
                }
            }
        }

        return sheetIndexPicMap;
    }

    public static void printImg(List<Map<String, PictureData>> sheetList) throws IOException {
        for (Map<String, PictureData> map : sheetList) {
            printImg(map);
        }
    }

    public static void printImg(Map<String, PictureData> map) throws IOException {
        Object key[] = map.keySet().toArray();
        for (int i = 0; i < map.size(); i++) {
            // 获取图片流
            PictureData pic = map.get(key[i]);
            // 获取图片索引
            String picName = key[i].toString();
            // 获取图片格式
            String ext = pic.suggestFileExtension();

            byte[] data = pic.getData();

            FileOutputStream out = new FileOutputStream("D:\\pic" + picName + "." + ext);
            out.write(data);
            out.flush();
            out.close();
        }
    }

    private static int[] getColsOfTable(Sheet sheet) {

        int[] data = {0, 0};
        for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i++) {
            if (null != sheet.getRow(i)) {
                data[0] = sheet.getRow(i).getLastCellNum();
                data[1] = sheet.getRow(i).getHeight();
            } else
                continue;
        }
        return data;
    }

    public static void main(String[] args) {

    }
}
