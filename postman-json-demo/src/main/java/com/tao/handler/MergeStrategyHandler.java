package com.tao.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.util.List;

/**
 * @author Tao
 * <p>
 * 填充模板时新建的行会丢失上一行的合并效果，增加合并处理
 */
public class MergeStrategyHandler extends AbstractMergeStrategy {
	@Override
	protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
		if (relativeRowIndex == null || relativeRowIndex == 0) {
			// 模板填充的第一行自带合并，不需要处理
			return;
		}
		int rowIndex = cell.getRowIndex();
		int colIndex = cell.getColumnIndex();
		sheet = cell.getSheet();
		// 获取当前单元格的上方单元格
		Row preRow = sheet.getRow(rowIndex - 1);
		Cell preCell = preRow.getCell(colIndex);
		// 获取sheet的所有合并区域
		List<CellRangeAddress> list = sheet.getMergedRegions();
		for (CellRangeAddress cellRangeAddress : list) {
			// 判断上一个单元格，是否是在合并区域内
			if (cellRangeAddress.containsRow(preCell.getRowIndex()) && cellRangeAddress.containsColumn(preCell.getColumnIndex())) {
				int lastColIndex = cellRangeAddress.getLastColumn();
				int firstColIndex = cellRangeAddress.getFirstColumn();
				// 创建和上一个单元格一样的合并范围
				CellRangeAddress cra = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), firstColIndex, lastColIndex);
				sheet.addMergedRegion(cra);
				// 合并的增加的部分是没有边框的，需要重新设置
				RegionUtil.setBorderBottom(BorderStyle.THIN, cra, sheet);
				RegionUtil.setBorderLeft(BorderStyle.THIN, cra, sheet);
				RegionUtil.setBorderRight(BorderStyle.THIN, cra, sheet);
				RegionUtil.setBorderTop(BorderStyle.THIN, cra, sheet);
				return;
			}
		}
	}
}
