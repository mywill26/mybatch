package com.mycx26.base.excel.imp.translator;

import com.mycx26.base.entity.EnumType;
import com.mycx26.base.entity.EnumValue;
import com.mycx26.base.enump.ColTypeEnum;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.service.EnumTypeService;
import com.mycx26.base.service.EnumValueService;
import com.mycx26.base.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by mycx26 on 2018/8/19.
 */
@Component
public class EnumTranslator {

    @Resource
    private EnumValueService enumValueService;

    @Resource
    private EnumTypeService enumTypeService;

    public void translate(List<String> values, Template template) {
        List<Integer> orders = template.getColTypeOrdersMap().get(ColTypeEnum.ENUM.getCode());
        String value;
        List<TemplateCol> cols = template.getCols();
        TemplateCol col;

        for (Integer order : orders) {
            value = values.get(order - 1);
            if (StringUtil.isBlank(value)) {
                continue;
            }

            col = cols.get(order - 1);
            translateEnum(value, col, values, cols);
        }
    }

    private void translateEnum(String value,
                               TemplateCol col,
                               List<String> values,
                               List<TemplateCol> cols) {
        EnumType et = enumTypeService.getByTypeCode(col.getEnumTypeCode());

        // cascade enum, from the beginning handle
        if (et.getCascadeEnum() && StringUtil.isNotBlank(et.getParentCode())) {
            return;
        }

        EnumValue ev = enumValueService.getRootByTypeCodeAndValueName(col.getEnumTypeCode(), value);
        values.set(col.getOrderNo() - 1, ev.getValueCode());

        // cascade root enum
        if (et.getCascadeEnum() && StringUtil.isBlank(et.getParentCode())) {
            cascadeTranslate(ev, col, values, cols);
        }
    }

    private void cascadeTranslate(EnumValue ev,
                                  TemplateCol col,
                                  List<String> values,
                                  List<TemplateCol> cols) {
        if (null == col.getChildOrderNo()) {
            return;
        }

        String cValue = values.get(col.getChildOrderNo() - 1);
        TemplateCol cCol = cols.get(col.getChildOrderNo() - 1);

        String cValueCode = ev.getNameEvMap().get(cValue).getValueCode();
        values.set(cCol.getOrderNo() - 1, cValueCode);

        ev = enumValueService.getByTypeAndValueCode(cCol.getEnumTypeCode(), cValueCode);

        cascadeTranslate(ev, cCol, values, cols);
    }
}
