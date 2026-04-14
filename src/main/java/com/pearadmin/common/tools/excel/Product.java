package com.pearadmin.common.tools.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 创建日期：2025-03-14
 **/

@Data
public class Product {
    @ExcelProperty("商品ID")
    private String productId;

    @ExcelProperty("商品名称")
    private String productName;

    @ExcelProperty("价格")
    private Double price;
}
