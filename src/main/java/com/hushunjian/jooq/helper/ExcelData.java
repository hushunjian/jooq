package com.hushunjian.jooq.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelData {

    @ApiModelProperty(value = "表头")
    private Map<String, Map<Integer, String>> sheetHeadMap;

    @ApiModelProperty(value = "表格行数据")
    private Map<String, List<Map<String, String>>> sheetRowsMap;

    public Map<String, List<String>> getSheetHeadValues() {
        Map<String, List<String>> result = Maps.newLinkedHashMap();
        sheetHeadMap.forEach((sheetName, sheetHeads) -> result.put(sheetName, Lists.newArrayList(sheetHeads.values())));
        return result;
    }

    public Integer getRowSize(String sheetName) {
        if (StringUtils.isBlank(sheetName)) {
            // 取所有sheet
            int size = 0;
            for (Map.Entry<String, List<Map<String, String>>> entry : sheetRowsMap.entrySet()) {
                size = size + entry.getValue().size();
            }
            return size;
        } else {
            // 取指定sheet
            return CollectionUtils.size(sheetRowsMap.get(sheetName));
        }
    }
}
