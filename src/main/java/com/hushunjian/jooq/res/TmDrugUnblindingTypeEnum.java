package com.hushunjian.jooq.res;

/**
 * @author shuxiaolong
 * @time 2018/8/30 17:04
 * @description 报告用药类型
 */
public enum TmDrugUnblindingTypeEnum {
    normal(0,"常规用药"),
    unblinding(1,"盲态用药"),
    /**
     * 代表此药品原来是盲态用药，后因其它原因，药品变为正常用药
     */
    unblindingChannel(3,"盲态取消用药")
    ;

    private Integer type;
    private String name;

    TmDrugUnblindingTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
