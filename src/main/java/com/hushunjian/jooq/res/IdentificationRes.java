package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IdentificationRes {

    @ApiModelProperty(value = "批量标识符id")
    private String batchId;

    @ApiModelProperty(value = "批量发送者")
    private String batchSender;

    @ApiModelProperty(value = "批量接收者")
    private String batchReceiver;

    private String company;

    @ApiModelProperty(value = "ICSR标识符id")
    private String icsrId;

    @ApiModelProperty(value = "ICSR发送者")
    private String icsrSender;

    @ApiModelProperty(value = "ICSR接收者")
    private String icsrReceiver;

    public String batchSenderAndReceiver() {
        return String.format("%s/%s", batchSender, batchReceiver);
    }
}
