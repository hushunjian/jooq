package com.hushunjian.jooq.controller;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.CICDInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("cicd")
@RestController(value = "cicd")
public class CICDController {


    @ApiOperation("导出excel")
    @PostMapping(value = "exportExcel")
    public void exportExcel(@RequestBody List<CICDInfo> cicdInfos) {
        // 表头
        List<String> headers = Lists.newArrayList("应用名称", "应用类型", "代码仓", "服务名", "默认副本数", "副本最大CPU（核）", "副本最大内存（Mi）");
        // 数据
        List<List<String>> table = cicdInfos.stream().map(cicdInfo ->
                Lists.newArrayList(
                        cicdInfo.getAppName(),
                        cicdInfo.getAppType(),
                        cicdInfo.getGitFullName(),
                        cicdInfo.getServiceName(),
                        String.valueOf(cicdInfo.getReplicaCount()),
                        String.valueOf(cicdInfo.getResourceLimitCpu()),
                        String.valueOf(cicdInfo.getResourceLimitMemory())
                )).collect(Collectors.toList());
        QueryDBHelper.exportExcel2(headers, table, "资源清单");
    }
}
