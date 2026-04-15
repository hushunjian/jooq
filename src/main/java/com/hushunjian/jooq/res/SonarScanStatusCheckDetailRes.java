package com.hushunjian.jooq.res;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SonarScanStatusCheckDetailRes {

    /**
     * 分支名称
     */
    private String branchName;

    /**
     * 提交ID
     */
    private String commitId;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间 (通常为Unix时间戳的毫秒数)
     */
    private Long createTime;

    /**
     * 流水线历史ID
     */
    private String pipelineHistoryId;

    /**
     * 任务状态 (例如: "failed", "success", "running")
     */
    private String jobStatus;

    /**
     * 任务历史ID
     */
    private String jobHistoryId;

    /**
     * SonarQube 度量ID
     */
    private String sonarMeasureId;

    /**
     * SonarQube 分析结果URL
     */
    private String sonarUrl;

    /**
     * 告警状态
     */
    private String alertStatus;

    /**
     * 代码中的缺陷数量
     */
    private String bugs;

    /**
     * 代码中的安全漏洞数量
     */
    private String vulnerabilities;

    /**
     * 代码异味数量
     */
    private String codeSmells;

    /**
     * 代码复杂度
     */
    private String complexity;

    /**
     * 重复行密度 (%)
     */
    private String duplicatedLinesDensity;

    /**
     * 注释行密度 (%)
     */
    private String commentLinesDensity;
}
