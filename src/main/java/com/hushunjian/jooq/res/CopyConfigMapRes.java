package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyConfigMapRes {

    @ApiModelProperty(value = "文件path")
    private String filePath;

    @ApiModelProperty(value = "关联的配置")
    private Map<String, Set<String>> relatedConfigKeysMap;

    @ApiModelProperty(value = "发生变更的配置")
    private List<ChangeConfigInfoRes> changeConfigInfos;
}
