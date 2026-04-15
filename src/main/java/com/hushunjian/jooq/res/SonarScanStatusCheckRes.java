package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SonarScanStatusCheckRes {

    @ApiModelProperty(value = "sonar扫描详情")
    private List<SonarScanStatusCheckDetailRes> list;
}
