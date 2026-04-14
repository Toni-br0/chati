package com.pearadmin.common.tools.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建日期：2025-03-07
 **/

@Data
public class ExcelDataDto {


    @ExcelIgnore
    private String userId;

    @ColumnWidth(20)
    @ExcelProperty("用户名")
    private String userName;

    @ColumnWidth(20)
    @ExcelProperty("邮箱")
    private String email;

    @ColumnWidth(20)
    @ExcelProperty("电话")
    private String phone;

    @ColumnWidth(20)
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd hh:MM:ss")
    private LocalDateTime createTime;

}
