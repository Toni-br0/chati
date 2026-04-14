package com.pearadmin.common.tools.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 创建日期：2025-03-14
 **/

@Data
public class User {
    @ExcelProperty("用户ID")
    private Integer id;

    @ExcelProperty("用户名")
    private String name;

    @ExcelProperty("年龄")
    private Integer age;
}
