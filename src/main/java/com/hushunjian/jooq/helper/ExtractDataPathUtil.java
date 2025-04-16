package com.hushunjian.jooq.helper;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

public class ExtractDataPathUtil {

    @SneakyThrows
    @SuppressWarnings("all")
    private List<Object> extractDataPathValues(List<?> inputObjects, String dataPath) {
        // 临时集合
        List<Object> temp = Lists.newArrayList(inputObjects);
        // 结果集
        List<Object> result = Lists.newArrayList();
        // 循环路径
        for (String path : dataPath.split("\\.")) {
            // class
            String className = temp.get(0).getClass().getSimpleName();
            // 循环每个输入对象,取出当前路径下的数据
            for (Object inputItem : temp) {
                // 获取字段
                Field field = ReflectionUtils.findField(inputItem.getClass(), path);
                if (field == null) {
                    continue;
                }
                field.setAccessible(true);
                // 获取字段值
                Object fieldValue = field.get(inputItem);
                // 判断是否是list,如果是list在循环
                if (fieldValue != null) {
                    // list
                    if (field.getType().isAssignableFrom(List.class)) {
                        result.addAll(((List<Object>) fieldValue));
                    } else {
                        result.add(fieldValue);
                    }
                }
            }
            // 这里做一次移除
            result.removeIf(item -> StringUtils.equals(item.getClass().getSimpleName(), className));
            // 替换临时数据
            temp = Lists.newArrayList(result);
        }
        return result;
    }

}
