package com.hushunjian.jooq.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class V4V5FieldConfig {

    private String v4FieldPath;

    private String v4FieldName;

    private String v4ItemClassId;

    private String v5FieldPath;

    private String v5FieldName;

    private String v5ItemClassId;

    private String v5ModuleName;
}
