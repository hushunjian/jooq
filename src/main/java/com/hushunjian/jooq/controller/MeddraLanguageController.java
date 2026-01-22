package com.hushunjian.jooq.controller;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("meddraLanguage")
@RestController(value = "meddraLanguage")
public class MeddraLanguageController {

    @Resource
    private RestTemplate restTemplate;

    @ApiOperation("查询重复的详情信息")
    @PostMapping(value = "queryRepeatDetails")
    public void queryRepeatDetails(@RequestBody QueryDBReq req) {
        // 查询重复SQL
        String queryRepeatSql = "SELECT meddra_version, llt_code, pt_code, hlt_code, hlgt_code, soc_code, `language`, count(1) as 'repeat_num' FROM meddra_language_info_detail GROUP BY meddra_version, llt_code, pt_code, hlt_code, hlgt_code, soc_code, `language` having count(1) > 1 ORDER BY count(1) DESC, llt_code asc;";
        req.setSqlContent(queryRepeatSql);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 查询数据
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("meddra_version", "llt_code", "pt_code", "hlt_code", "hlgt_code", "soc_code", "language", "repeat_num"));
        // 循环每一行
        rows.forEach(row -> {
            // 查询出所有数据
            String queryRepeatDetailSql = String.format("SELECT * FROM meddra_language_info_detail where meddra_version = '%s' and llt_code = '%s' and pt_code = '%s' and hlt_code = '%s' and hlgt_code = '%s' and soc_code = '%s' and language = '%s';",
                    row.get("meddra_version"), row.get("llt_code"), row.get("pt_code"), row.get("hlt_code"), row.get("hlgt_code"), row.get("soc_code"), row.get("language"));
            req.setSqlContent(queryRepeatDetailSql);
            // 查询数据
            List<Map<String, String>> repeatRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "create_time"));
            if (repeatRows.size() > 1) {
                // 删除掉任意一个
                for (int i = 0; i < repeatRows.size(); i++) {
                    if (i != 0) {
                        // 删除掉
                        log.info("需要删除的数据id:[{}]", repeatRows.get(i).get("id"));
                    }
                }
            }
        });
    }

}
