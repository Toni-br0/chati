package com.pearadmin.common.tools.excel;

/**
 * 创建日期：2025-03-10
 **/


import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class OptimizedMergeCellStrategyHandler extends AbstractMergeStrategy {

    private final boolean alikeColumn;
    private final boolean alikeRow;
    private final int rowIndex;
    private final int rowIndexStart;
    private final Set<Integer> columns;
    private int currentRowIndex = 0;

    public OptimizedMergeCellStrategyHandler(boolean alikeColumn, boolean alikeRow, int rowIndex, Set<Integer> columns) {
        this(alikeColumn, alikeRow, rowIndex, columns, 0);
    }

    public OptimizedMergeCellStrategyHandler(boolean alikeColumn, boolean alikeRow, int rowIndex, Set<Integer> columns, int rowIndexStart) {
        this.alikeColumn = alikeColumn;
        this.alikeRow = alikeRow;
        this.rowIndex = rowIndex;
        this.columns = columns;
        this.rowIndexStart = rowIndexStart;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer integer) {
        int rowId = cell.getRowIndex();
        currentRowIndex = rowId;

        if (rowIndex > rowId) {
            return;
        }

        int columnId = cell.getColumnIndex();

        if (alikeColumn && columnId > 0) {
            mergeCells(sheet, cell, columnId - 1, columnId, 0);
        }

        if (alikeRow && rowId > rowIndexStart && columns.contains(columnId)) {
            mergeCells(sheet, cell, rowId - 1, rowId, 1);
        }
    }

    private void mergeCells(Sheet sheet, Cell cell, int start, int end, int direction) {
        String cellValue = getCellVal(cell);
        Cell referenceCell = direction == 0 ? cell.getRow().getCell(start) : sheet.getRow(start).getCell(cell.getColumnIndex());
        String refCellValue = getCellVal(referenceCell);

        if (Objects.equals(cellValue, refCellValue)) {
            CellRangeAddress rangeAddress = createRangeAddress(sheet, cell, start, end, direction);
            if (rangeAddress != null) {
                sheet.addMergedRegion(rangeAddress);
            }
        }
    }

    private CellRangeAddress createRangeAddress(Sheet sheet, Cell cell, int start, int end, int direction) {
        CellRangeAddress rangeAddress = direction == 0 ?
                new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), start, end) :
                new CellRangeAddress(start, end, cell.getColumnIndex(), cell.getColumnIndex());

        return findExistAddress(sheet, rangeAddress, getCellVal(cell));
    }

    private CellRangeAddress findExistAddress(Sheet sheet, CellRangeAddress rangeAddress, String currentVal) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (int i = mergedRegions.size() - 1; i >= 0; i--) {
            CellRangeAddress exist = mergedRegions.get(i);
            if (exist.intersects(rangeAddress)) {
                if (exist.getLastRow() < rangeAddress.getLastRow()) {
                    exist.setLastRow(rangeAddress.getLastRow());
                }
                if (exist.getLastColumn() < rangeAddress.getLastColumn()) {
                    exist.setLastColumn(rangeAddress.getLastColumn());
                }
                sheet.removeMergedRegion(i);
                return exist;
            }
        }
        return rangeAddress;
    }

    private String getCellVal(Cell cell) {
        try {
            return cell.getStringCellValue();
        } catch (Exception e) {
            // 使用日志框架代替 System.out.printf
            // Logger logger = LoggerFactory.getLogger(OptimizedMergeCellStrategyHandler.class);
            // logger.error("读取单元格内容失败:行{} 列{}", cell.getRowIndex() + 1, cell.getColumnIndex() + 1, e);
            log.info("读取单元格内容失败:行{} 列{} ", (cell.getRowIndex() + 1), (cell.getColumnIndex() + 1));
            return null;
        }
    }
}
