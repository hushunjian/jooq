package com.hushunjian.jooq.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.res.FixData;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("fixData")
@RestController(value = "fixData")
public class FixDataController {


    @ApiOperation("fixData")
    @PostMapping(value = "fixData")
    public void fixData(@RequestBody List<String> ids) {
        List<FixData> fixDataInfos = Lists.newArrayList();
        ids.forEach(id -> {
            Map<String, String> columnValueMap = Maps.newHashMap();
            columnValueMap.put("task_status", "esafety_task_status_completed");
            columnValueMap.put("gvp_task_status", "finished");
            columnValueMap.put("process_account_id", "8a8181f076287e3001766b478ced2551");
            columnValueMap.put("process_account_name", "");
            columnValueMap.put("process_user_id", "");
            columnValueMap.put("process_user_name", "");
            columnValueMap.put("create_user_id", "");
            columnValueMap.put("create_user_name", "");
            columnValueMap.put("tenant_id", "");
            fixDataInfos.add(FixData.builder()
                    .id(id)
                    .table("gvp_workbench.task_info")
                    .columnValueMap(columnValueMap)
                    .build());
        });
        System.out.println(JSON.toJSONString(fixDataInfos));
    }
}
