package com.hushunjian.jooq.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class AdvanceSearchDTO {

    @ApiModelProperty("检索条件")
    private CommonSearchDTO commonSearchDTO;

}
