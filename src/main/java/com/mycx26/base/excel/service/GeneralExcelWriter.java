package com.mycx26.base.excel.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.mycx26.base.util.StringUtil;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mycx26 on 2019/11/1.
 */
public class GeneralExcelWriter {

    public static final String SHEET_NAME = "Sheet1";

    /**
     * Include file name.
     */
    private ExcelWriter excelWriter;

    private WriteSheet writeSheet;

    public GeneralExcelWriter(String filePath, List<String> heads) {
        if (StringUtil.isBlank(filePath)) {
            throw new RuntimeException("File path is blank");
        }
        if (null == heads || heads.isEmpty()) {
            throw new RuntimeException("File labels is blank");
        }

        // 临时解决阿里巴巴 EasyExcel(2.1.0-beta4) 当head只有一列时，数据列第一列不会写入excel
//        heads.add("错误信息");
        excelWriter = EasyExcel.write(filePath).head(initHeads(heads))
                .registerWriteHandler(getStrategy()).build();
        writeSheet = EasyExcel.writerSheet(SHEET_NAME).build();
    }

    public static List<List<String>> initHeads(List<String> heads) {
        List<List<String>> lists = new ArrayList<>(heads.size());

        heads.forEach(e -> {
            List<String> head0 = new ArrayList<>(1);
            head0.add(e);
            lists.add(head0);
        });
        return lists;
    }

    public static HorizontalCellStyleStrategy getStrategy() {
        WriteCellStyle cellStyle = new WriteCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());

        WriteFont writeFont = new WriteFont();
        writeFont.setFontHeightInPoints((short) 10);
        writeFont.setBold(true);

        cellStyle.setWriteFont(writeFont);

        return new HorizontalCellStyleStrategy(cellStyle, new WriteCellStyle());
    }

    public void writeRows(List<List<String>> rows) {
        excelWriter.write(rows, writeSheet);
        if (rows.get(rows.size() - 1).isEmpty()) {
            excelWriter.finish();
        }
    }
}
