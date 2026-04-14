package com.pearadmin.common.tools.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.aspose.cells.*;
import com.aspose.cells.Workbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcelTest {


  public static void main(String[] args) throws Exception {
    List<ExcelDataDto> list = new ArrayList<>();
    for(int i=0;i<10;i++){
      ExcelDataDto excelDataDto = new ExcelDataDto();
      if(i <5){
        excelDataDto.setUserName("用户名_");
      }else{
        excelDataDto.setUserName("用户名_"+i);
      }

      if(i>=5){
        excelDataDto.setEmail("邮箱_");
      }else{
        excelDataDto.setEmail("邮箱_"+i);
      }

      if(i>2 && i<6){
        excelDataDto.setPhone("电话_");
      }else{
        excelDataDto.setPhone("电话_"+i);
      }

      excelDataDto.setCreateTime(LocalDateTime.now());

      list.add(excelDataDto);
    }

    /*String[] headStr = {"序号", "时间", "照片", "姓名", "手机号码", "备注信息"};
    ExcelUtil.createExcel(list,headStr);*/

    // 存放路径
    String path = "D:/tmp/555.xls";
    // 文件名称
    String fileName = path;
    //数据
    // 创建合并区：将第2-6行的2-3列合并
    OnceAbsoluteMergeStrategy absoluteMergeStrategy = new OnceAbsoluteMergeStrategy(1, 5, 1, 2);
    EasyExcel.write(fileName, ExcelDataDto.class).registerWriteHandler(absoluteMergeStrategy).sheet("用户信息").doWrite(list);


    /*******************************************************/
    String filePaht = "D:/tmp/666.xls";

    ExcelWriterBuilder write = ExcelUtil.createWriterBuilder(filePaht).autoCloseStream(true);

    Set<Integer> set = new HashSet<>();
    set.add(0); // 合并第1列
    set.add(1); // 合并第2列
    set.add(2); // 合并第3列
    set.add(3); // 合并第4列
    write.registerWriteHandler(new OptimizedMergeCellStrategyHandler(true, true, 2, set)); // 启用列和行合并
    write.head(head1()).automaticMergeHead(true).sheet("模板").doWrite(list);


    /***************************************************************/
    // 定义输出文件路径
    String fileName2 = "D:/tmp/777.xls";

    // 需要合并的列
    int[] cols = {0, 1, 2, 3};
    // 从第二行后开始合并
    int row = 1;

    ExcelWriter excelWriter = EasyExcel.write(fileName2).autoCloseStream(true).build();

    // 创建第一个 Sheet（用户数据）
    WriteSheet userSheet = EasyExcel.writerSheet(0, "用户列表")
            .head(User.class)
            .registerWriteHandler(new ExcelMergeHandler(row,cols))
            .registerWriteHandler(ExcelUtil.createCellStyle())
            .build();
    List<User> userList = generateUserData();
    excelWriter.write(userList, userSheet);

    // 创建第二个 Sheet（商品数据）
    WriteSheet productSheet = EasyExcel.writerSheet(1, "商品列表")
            //.head(Product.class)
            .head(head1())
            .registerWriteHandler(new ExcelMergeHandler(row,cols))
            .registerWriteHandler(ExcelUtil.createCellStyle())
            .build();
    List<Product> productList = generateProductData();
    excelWriter.write(productList, productSheet);
    //excelWriter.write(list, productSheet);

    // 关闭资源（必须调用，否则文件不完整）
    excelWriter.finish();

    System.out.println("Excel 文件生成成功！");


  }

  private static List<List<String>> data1() {
    List<List<String>> data = new ArrayList<>();
    List<String> data1 = new ArrayList<>();
    data1.add("人员");
    data1.add("人员");
    data1.add("语文");
    data1.add("数值一");
    data1.add("数值二");

    List<String> data2 = new ArrayList<>();
    data2.add("人员");
    data2.add("人员1");
    data2.add("语文");
    data2.add("数值三");
    data2.add("数值四");

    data.add(data1);
    data.add(data2);
    return data;
  }

  private static List<List<String>> head() {
    List<List<String>> list = new ArrayList<>();
    List<String> head0 = new ArrayList<>();
    head0.add("模块");
    //head0.add("模块");
    List<String> head00 = new ArrayList<>();
    head00.add("模块");
    //head00.add("模块");
    List<String> head1 = new ArrayList<>();
    //head1.add("课程");
    head1.add("课程");
    List<String> head2 = new ArrayList<>();
    head2.add("完美世界");
    head2.add("石昊");
    List<String> head3 = new ArrayList<>();
    head3.add("完美世界");
    head3.add("火灵儿");

    list.add(head0);
    list.add(head00);
    list.add(head1);
    list.add(head2);
    list.add(head3);
    return list;
  }

  private static List<List<String>> head1() {
    List<List<String>> list = new ArrayList<>();
    List<String> head0 = new ArrayList<>();
    head0.add("模块");
    List<String> head1 = new ArrayList<>();
    head1.add("课程");
    List<String> head2 = new ArrayList<>();
    head2.add("完美世界");
    List<String> head3 = new ArrayList<>();
    head3.add("时间");

    list.add(head0);
    list.add(head1);
    list.add(head2);
    list.add(head3);
    return list;
  }

  // 生成模拟用户数据
  private static List<User> generateUserData() {
    List<User> list = new ArrayList<>();

    User user1 = new User();
    user1.setId(1);
    user1.setName("张三");
    user1.setAge(25);

    User user2 = new User();
    user2.setId(2);
    user2.setName("李四");
    user2.setAge(30);

    User user3 = new User();
    user3.setId(3);
    user3.setName("李四");
    user3.setAge(40);

    list.add(user1);
    list.add(user2);
    list.add(user3);
    return list;
  }

  // 生成模拟商品数据
  private static List<Product> generateProductData() {
    List<Product> list = new ArrayList<>();
    Product product1 = new Product();
    product1.setProductId("P001");
    product1.setProductName("笔记本电脑");
    product1.setPrice(5999.99);

    Product product2 = new Product();
    product2.setProductId("P001");
    product2.setProductName("笔记本电脑");
    product2.setPrice(4999.99);

    Product product3 = new Product();
    product3.setProductId("P002");
    product3.setProductName("智能手机");
    product3.setPrice(2999.99);

    Product product4 = new Product();
    product4.setProductId("P004");
    product4.setProductName("智能手机");
    product4.setPrice(2999.99);

    list.add(product1);
    list.add(product2);
    list.add(product3);
    list.add(product4);
    return list;
  }


  public static void creatExcel(){
    // 创建一个新的工作簿（Workbook）
    org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook(); // 对于 XLSX 文件
    // Workbook workbook = new HSSFWorkbook(); // 对于 XLS 文件
    Sheet sheet = workbook.createSheet("Example Sheet");

    // 创建一个行（Row）对象
    org.apache.poi.ss.usermodel.Row row = sheet.createRow(0); // 在第一行创建行对象

    // 创建一个单元格（Cell）对象，并设置值
    org.apache.poi.ss.usermodel.Cell cell = row.createCell(0); // 在第一行的第一列创建单元格对象
    cell.setCellValue("合并单元格示例");

    // 合并单元格，从第一行第一列到第一行第三列
    sheet.addMergedRegion(new CellRangeAddress(
            0, // first row (0-based)
            0, // last row  (0-based)
            0, // first column (0-based)
            2  // last column  (0-based)
    ));

    // 调整列宽以适应内容
    sheet.autoSizeColumn(0);
    sheet.autoSizeColumn(1); // 因为我们已经合并了这两列，所以只需要调整第一列即可
    sheet.autoSizeColumn(2); // 因为我们已经合并了这两列，所以只需要调整第一列即可

    // 将工作簿写入文件系统
    try (FileOutputStream outputStream = new FileOutputStream("D:\\tmp\\example.xlsx")) { // 对于 XLSX 文件
      // FileOutputStream outputStream = new FileOutputStream("example.xls"); // 对于 XLS 文件
      workbook.write(outputStream);
      workbook.close(); // 关闭工作簿释放资源，write方法会自动关闭Workbook，但最好手动关闭以确保资源被释放。
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
     * Excel导出为 PNG
     * @throws Exception
     */
    public static void excelToPng() throws Exception {
    // 加载 Excel 文件
      Workbook workbook = new Workbook("D:\\tmp\\11.xlsx");

      // 获取第一个工作表
      Worksheet worksheet = workbook.getWorksheets().get(0);

      // 设置图片选项
      ImageOrPrintOptions imgOptions = new ImageOrPrintOptions();
      imgOptions.setImageFormat(ImageFormat.getPng());
      //imgOptions.setHorizontalResolution(200);
      //imgOptions.setVerticalResolution(200);
      imgOptions.setCellAutoFit(true);
      imgOptions.setOnePagePerSheet(true); // 将整个工作表渲染为一张图片

      // 创建工作表渲染对象
      SheetRender sr = new SheetRender(worksheet, imgOptions);
      sr.toImage(0, "D:\\tmp\\output_1.png");

      System.out.println("Excel 文件已成功转换为图片。");


  }


  }

