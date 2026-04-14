package com.pearadmin.common.tools.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.pearadmin.common.constant.SystemConstant;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Excel 工具
 * */
public class ExcelUtil {

    /**
     * 读 Excel
     *
     * @param request 请求对象
     * @param clazz 对象
     * */
    public static <T> List<T> read(HttpServletRequest request, Class clazz) {
        try {
            return EasyExcel.read(request.getInputStream()).head(clazz).sheet().doReadSync();
        } catch (IOException io) {
            io.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 读 Excel
     *
     * @param filename 文件路径
     * @param clazz 对象
     * */
    public static <T> List<T> read(String filename, Class clazz) {
        return EasyExcel.read(filename).head(clazz).sheet().doReadSync();
    }

    /**
     * 写 Excel
     *
     * @param response 响应对象
     * @param clazz 对象
     * */
    public static void write(HttpServletResponse response, Class clazz, List list) {
        try{
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding(SystemConstant.UTF8);
            EasyExcel.write(response.getOutputStream(), clazz).sheet("默认").doWrite(list);
        }catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * 写 Excel
     *
     * @param filename 文件名称
     * @param clazz 对象
     * */
    public static void write(String filename, Class clazz, List<T> list) {
        EasyExcel.write(filename, clazz).sheet("默认").doWrite(list);
    }



    /**
     * 创建ExcelWriterBuilder对象
     */
    public static ExcelWriterBuilder createWriterBuilder(String filePath) {
        ExcelWriterBuilder write = EasyExcel.write(filePath);
        write.registerWriteHandler(createCellStyle());
        return write;

    }

    /**
     * 创建 CellStyle 对象
     * @return
     */
    public static HorizontalCellStyleStrategy createCellStyle(){
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 背景设置为白色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
        //contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        // 背景绿色
        //contentWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        //设置 自动换行
        contentWriteCellStyle.setWrapped(true);

        //设置 垂直居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置边框线
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);   // 设置单元格上边框为细线
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);// 设置单元格下边框为粗线
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);	 // 设置单元格左边框为中线
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN); // 设置单元格右边框为中虚线
        //设置 水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            /*WriteFont contentWriteFont = new WriteFont();
            // 字体大小
            contentWriteFont.setFontHeightInPoints((short)20);
            contentWriteCellStyle.setWriteFont(contentWriteFont);*/
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

        return horizontalCellStyleStrategy;
    }

    /**
     * 内容居中样式
     * @param wb
     * @return
     */
    private static CellStyle getContentCenterStyle(XSSFWorkbook wb){
        CellStyle contentCenterStyle = wb.createCellStyle();
        contentCenterStyle.setBorderBottom(BorderStyle.THIN);
        contentCenterStyle.setBorderLeft(BorderStyle.THIN);
        contentCenterStyle.setBorderRight(BorderStyle.THIN);
        contentCenterStyle.setBorderTop(BorderStyle.THIN);
        contentCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        contentCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentCenterStyle.setFont(getFontStyle(wb));
        return contentCenterStyle;
    }

    /**
     * 内容左靠样式
     * @param wb
     * @return
     */
    private static CellStyle getContentLeftStyle(XSSFWorkbook wb){
        CellStyle contentCenterStyle = wb.createCellStyle();
        contentCenterStyle.setBorderBottom(BorderStyle.THIN);
        contentCenterStyle.setBorderLeft(BorderStyle.THIN);
        contentCenterStyle.setBorderRight(BorderStyle.THIN);
        contentCenterStyle.setBorderTop(BorderStyle.THIN);
        contentCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentCenterStyle.setFont(getFontStyle(wb));
        return contentCenterStyle;
    }

    /**
     * 标题居中样式
     * @param wb
     * @return
     */
    private static CellStyle getHeadStyle(XSSFWorkbook wb){
        CellStyle headStyle = wb.createCellStyle();
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setFont(getBoldFontStyle(wb));
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return headStyle;
    }

    /**
     * 普通文字样式
     * @param wb
     * @return
     */
    private static Font getFontStyle(XSSFWorkbook wb){
        Font contentFont = wb.createFont();
        contentFont.setFontName("仿宋");
        contentFont.setFontHeightInPoints((short) 12);
        return contentFont;
    }

    /**
     * 加粗文字样式
     * @param wb
     * @return
     */
    private static Font getBoldFontStyle(XSSFWorkbook wb){
        Font contentFont = wb.createFont();
        contentFont.setFontName("仿宋");
        contentFont.setFontHeightInPoints((short) 12);
        contentFont.setBold(true);
        return contentFont;
    }


}