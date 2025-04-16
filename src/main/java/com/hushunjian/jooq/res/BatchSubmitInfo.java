package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BatchSubmitInfo {

    @ApiModelProperty(value = "批量导出信息")
    private List<BatchExportInfo> autoSubmitReqList;
}
