package com.hushunjian.jooq.helper;

import java.util.Map;

public enum ExcelAnalysisModel {

    /**
     * 列名模式
     */
    COLUMN_NAME {
        @Override
        public void putKeyValue(Map<String, String> result, Integer index, String head, String keyValue) {
            result.put(head, keyValue);
        }
    },

    /**
     * 列下标模式
     */
    COLUMN_INDEX {
        @Override
        public void putKeyValue(Map<String, String> result, Integer index, String head, String keyValue) {
            result.put(index + "", keyValue);
        }
    },

    /**
     * 列下标|列名模式
     */
    COLUMN_INDEX_NAME {
        @Override
        public void putKeyValue(Map<String, String> result, Integer index, String head, String keyValue) {
            result.put(String.format("%s|%s", index, head), keyValue);
        }
    },

    /**
     * 列名,重复的列名在加下标
     */
    COLUMN_NAME_REPEAT_INDEX {
        @Override
        public void putKeyValue(Map<String, String> result, Integer index, String head, String keyValue) {
            if (result.containsKey(head)) {
                // 已经包含这个列了,新进来的加下标前缀
                result.put(String.format("%s|%s", index, head), keyValue);
            } else {
                // 没有包含这个列,直接放进去
                result.put(head, keyValue);
            }
        }
    }

    ;

    public void putKeyValue(Map<String, String> result, Integer index, String head, String keyValue) {

    }
}
