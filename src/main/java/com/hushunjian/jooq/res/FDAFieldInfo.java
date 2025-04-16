package com.hushunjian.jooq.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FDAFieldInfo {

    private String filedSource;

    private String fieldNo;

    private String maxlength;

    private String dateType;

    private Set<String> nullFlavorItems;

    private List<String> dbNullFlavorItems;

    private String dbMaxLength;

    private String dbType;

    private Boolean nullFlavorMatch;

    private Boolean maxLengthMatch;


    private String i18nKey;

    private String fieldKey;

    private String table;

    private String columnName;

    private String itemClassId;
}
