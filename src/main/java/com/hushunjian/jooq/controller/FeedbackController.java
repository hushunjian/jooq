package com.hushunjian.jooq.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.FixData;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("feedback")
@RestController(value = "feedback")
public class FeedbackController {

    @Resource
    private RestTemplate restTemplate;

    @ApiOperation("查询错误的反馈数据")
    @PostMapping(value = "queryErrorFeedback")
    public void queryErrorFeedback(@RequestBody QueryDBReq req) {
        // 查询生产环境没有生成报告的反馈数据
        String queryRepeatSql = "select * from bot_feedback_task_detail where create_time > '2026-03-23' and report_id is null;";
        req.setSqlContent(queryRepeatSql);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 需要生成的
        Map<String, List<String>> needHandleIds = Maps.newHashMap();
        // 不需要生成的
        List<String> notNeedHandleIds = Lists.newArrayList();
        // 查询
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "code", "tenant_id"));
        // 循环判断
        rows.forEach(row -> {
            // 判断这个反馈吗今天是不是有新的导入
            String queryFeedBackCodeSql = String.format("select * from bot_feedback_task_detail where tenant_id = '%s' and code = '%s' and report_id is not null and report_id != '' and create_time > '2026-03-23';", row.get("tenant_id"), row.get("code"));
            // 查询
            req.setSqlContent(queryFeedBackCodeSql);
            // 获取成功的
            List<Map<String, String>> successCodeRows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "code", "tenant_id", "report_id"));
            if (CollectionUtils.isNotEmpty(successCodeRows)) {
                // 已经手动导入成功的
                notNeedHandleIds.add(row.get("id"));
            } else {
                // 需要重新执行的
                needHandleIds.computeIfAbsent(row.get("tenant_id"), k -> Lists.newArrayList()).add(row.get("id"));
            }
        });
        log.info("需要生成的:[{}]", needHandleIds);
        log.info("不需要生成的:[{}]", notNeedHandleIds);
    }

    @ApiOperation("查询错误的反馈数据2")
    @PostMapping(value = "queryErrorFeedback2")
    public void queryErrorFeedback2(@RequestBody QueryDBReq req) {
        String queryRepeatSql = "select id, report_id, task_id, code, tenant_id, report_no from bot_feedback_task_detail where id in (\"8a8d82a19cbd8f9b019d18a427d23c32\",\"8a8d82a19cbd8f9b019d18a427d23c33\",\"8a8d82a19cbd8f9b019d18a42d1c3c95\",\"8a8d82a19cbd8f9b019d18a432593cd5\",\"8a8d82a19cbd8f9b019d18a432593cd7\",\"8a8d82a19cbd8f9b019d18a432593cd8\",\"8a8d82a19cbd8f9b019d18a432593cd9\",\"8a8d82a19cbd8f9b019d18a432593cda\",\"8a8d82a19cbd8f9b019d18a442df3df5\",\"8a8d82a19cbd8f9b019d18a442df3df6\",\"8a8d82a19cbd8f9b019d18a44f943eb6\",\"8a8d82a19cbd8f9b019d18a459623f42\",\"8a8d82a19cbd8f9b019d18a459623f43\",\"8a8d82a19cbd8f9b019d18a459623f44\",\"8a8d82a19cbd8f9b019d18a459623f45\",\"8a8d82a19cbd8f9b019d18a459623f46\",\"8a8d82a19cbd8f9b019d18a459623f47\",\"8a8d82a19cbd8f9b019d18a459623f48\",\"8a8d82a19cbd8f9b019d18a459623f49\",\"8a8d82a19cbd8f9b019d18a459623f4a\",\"8a8d82a19cbd8f9b019d18a459623f4b\",\"8a8d82a19cbd8f9b019d18a459623f4c\",\"8a8d82a19cbd8f9b019d18a459623f4d\",\"8a8d82a19cbd8f9b019d18a459623f4e\",\"8a8d82a19cbd8f9b019d18a459623f4f\",\"8a8d82a19cbd8f9b019d18a459623f50\",\"8a8d82a19cbd8f9b019d18a459623f51\",\"8a8d82a19cbd8f9b019d18a459623f52\",\"8a8d82a19cbd8f9b019d18a459623f53\",\"8a8d82a19cbd8f9b019d18a459623f54\",\"8a8d82a19cbd8f9b019d18a459623f55\",\"8a8d82a19cbd8f9b019d18a487f64386\",\"8a8d82a19cbd8f9b019d18a487f64387\",\"8a8d82a19cbd8f9b019d18a487f64388\",\"8a8d82a19cbd8f9b019d18a4916f444f\",\"8a8d82a19cbd8f9b019d18a4916f4450\",\"8a8d82a19cbd8f9b019d18a49cfd44e4\",\"8a8d82a19cbd8f9b019d18a49cfd44e5\",\"8a8d82a19cbd8f9b019d18a49cfd44e6\",\"8a8d82a19cbd8f9b019d18a49cfd44e7\",\"8a8d82a19cbd8f9b019d18a49cfd44e8\",\"8a8d82a19cbd8f9b019d18a49cfd44e9\",\"8a8d82a19cbd8f9b019d18a49cfd44ea\",\"8a8d82a19cbd8f9b019d18a49cfd44eb\",\"8a8d82a19cbd8f9b019d18a49cfd44ec\",\"8a8d82a19cbd8f9b019d18a49cfd44ed\",\"8a8d82a19cbd8f9b019d18a49cfd44ee\",\"8a8d82a19cbd8f9b019d18a49cfd44ef\",\"8a8d82a19cbd8f9b019d18a4e68149c2\",\"8a8d82a19cbd8f9b019d18a4f05c4a08\",\"8a8d82a19cbd8f9b019d18a4f05c4a09\",\"8a8d82a19cbd8f9b019d18a4f05c4a0a\",\"8a8d82a19cbd8f9b019d18a4f05c4a0b\",\"8a8d82a19cbd8f9b019d18a4f05c4a0c\",\"8a8d82a19cbd8f9b019d18a4fddf4b0c\",\"8a8d82a19cbd8f9b019d18a4fddf4b0d\",\"8a8d82a19cbd8f9b019d18a505694b8c\",\"8a8d82a19cbd8f9b019d18a50f884bee\",\"8a8d82a19cbd8f9b019d18a50f884bef\",\"8a8d82a19cbd8f9b019d18a50f884bf0\",\"8a8d82a19cbd8f9b019d18a50f884bf1\",\"8a8d82a19cbd8f9b019d18a50f884bf2\",\"8a8d82a19cbd8f9b019d18a50f884bf3\",\"8a8d82a19cbd8f9b019d18a50f884bf4\",\"8a8d82a19cbd8f9b019d18a50f884bf5\",\"8a8d82a19cbd8f9b019d18a50f884bf6\",\"8a8d82a19cbd8f9b019d18a50f884bf7\",\"8a8d82a19cbd8f9b019d18a5274c4db9\",\"8a8d82a19cbd8f9b019d18a52f4e4df5\",\"8a8d82a19cbd8f9b019d18a52f4e4df6\",\"8a8d82a19cbd8f9b019d18a536ff4e53\",\"8a8d82a19cbd8f9b019d18a53e414e9d\",\"8a8d82a19cbd8f9b019d18a53e414e9e\",\"8a8d82a19cbd8f9b019d18a53e414e9f\",\"8a8d82a19cbd8f9b019d18a547724f49\",\"8a8d82a19cbd8f9b019d18a547724f4a\",\"8a8d82a19cbd8f9b019d18a54e5c4f9a\",\"8a8d82a19cbd8f9b019d18a552474fde\",\"8a8d82a19cbd8f9b019d18a559f95028\",\"8a8d82a19cbd8f9b019d18a559f95029\",\"8a8d82a19cbd8f9b019d18a561775099\",\"8a8d82a19cbd8f9b019d18a56177509a\",\"8a8d82a19cbd8f9b019d18a56c4850fe\",\"8a8d82a19cbd8f9b019d18a56c4850ff\",\"8a8d82a19cbd8f9b019d18a56c485100\",\"8a8d82a19cbd8f9b019d18a56c485101\",\"8a8d82a19cbd8f9b019d18a56c485102\",\"8a8d82a19cbd8f9b019d18a56c485103\",\"8a8d82a19cbd8f9b019d18a56c485104\",\"8a8d82a19cbd8f9b019d18a57d01523e\",\"8a8d82a19cbd8f9b019d18a57d02523f\",\"8a8d82a19cbd8f9b019d18a586b6529f\",\"8a8d82a19cbd8f9b019d18a58e9a52fa\")";
        req.setSqlContent(queryRepeatSql);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 查询
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id", "code", "tenant_id", "task_id"));
        // 一个任务一次
        Map<String, List<String>> taskIdsMap = Maps.newHashMap();
        //
        rows.forEach(row -> taskIdsMap.computeIfAbsent(row.get("task_id"), k -> Lists.newArrayList()).add(row.get("id")));
        System.out.println(taskIdsMap);
    }

    @ApiOperation("查询错误的反馈数据3")
    @PostMapping(value = "queryErrorFeedback3")
    public void queryErrorFeedback3(@RequestBody QueryDBReq req) {
        String queryRepeatSql = "select t1.id, t1.task_id, t1.report_id, t1.tenant_id, t1.report_no, t2.tenant_id from bot_feedback_task_detail t1 left join report_value t2 on t1.report_id = t2.id where t1.id in (\"8a8d82a19cbd8f9b019d18a427d23c32\",\"8a8d82a19cbd8f9b019d18a427d23c33\",\"8a8d82a19cbd8f9b019d18a42d1c3c95\",\"8a8d82a19cbd8f9b019d18a432593cd5\",\"8a8d82a19cbd8f9b019d18a432593cd7\",\"8a8d82a19cbd8f9b019d18a432593cd8\",\"8a8d82a19cbd8f9b019d18a432593cd9\",\"8a8d82a19cbd8f9b019d18a432593cda\",\"8a8d82a19cbd8f9b019d18a442df3df5\",\"8a8d82a19cbd8f9b019d18a442df3df6\",\"8a8d82a19cbd8f9b019d18a44f943eb6\",\"8a8d82a19cbd8f9b019d18a459623f42\",\"8a8d82a19cbd8f9b019d18a459623f43\",\"8a8d82a19cbd8f9b019d18a459623f44\",\"8a8d82a19cbd8f9b019d18a459623f45\",\"8a8d82a19cbd8f9b019d18a459623f46\",\"8a8d82a19cbd8f9b019d18a459623f47\",\"8a8d82a19cbd8f9b019d18a459623f48\",\"8a8d82a19cbd8f9b019d18a459623f49\",\"8a8d82a19cbd8f9b019d18a459623f4a\",\"8a8d82a19cbd8f9b019d18a459623f4b\",\"8a8d82a19cbd8f9b019d18a459623f4c\",\"8a8d82a19cbd8f9b019d18a459623f4d\",\"8a8d82a19cbd8f9b019d18a459623f4e\",\"8a8d82a19cbd8f9b019d18a459623f4f\",\"8a8d82a19cbd8f9b019d18a459623f50\",\"8a8d82a19cbd8f9b019d18a459623f51\",\"8a8d82a19cbd8f9b019d18a459623f52\",\"8a8d82a19cbd8f9b019d18a459623f53\",\"8a8d82a19cbd8f9b019d18a459623f54\",\"8a8d82a19cbd8f9b019d18a459623f55\",\"8a8d82a19cbd8f9b019d18a487f64386\",\"8a8d82a19cbd8f9b019d18a487f64387\",\"8a8d82a19cbd8f9b019d18a487f64388\",\"8a8d82a19cbd8f9b019d18a4916f444f\",\"8a8d82a19cbd8f9b019d18a4916f4450\",\"8a8d82a19cbd8f9b019d18a49cfd44e4\",\"8a8d82a19cbd8f9b019d18a49cfd44e5\",\"8a8d82a19cbd8f9b019d18a49cfd44e6\",\"8a8d82a19cbd8f9b019d18a49cfd44e7\",\"8a8d82a19cbd8f9b019d18a49cfd44e8\",\"8a8d82a19cbd8f9b019d18a49cfd44e9\",\"8a8d82a19cbd8f9b019d18a49cfd44ea\",\"8a8d82a19cbd8f9b019d18a49cfd44eb\",\"8a8d82a19cbd8f9b019d18a49cfd44ec\",\"8a8d82a19cbd8f9b019d18a49cfd44ed\",\"8a8d82a19cbd8f9b019d18a49cfd44ee\",\"8a8d82a19cbd8f9b019d18a49cfd44ef\",\"8a8d82a19cbd8f9b019d18a4e68149c2\",\"8a8d82a19cbd8f9b019d18a4f05c4a08\",\"8a8d82a19cbd8f9b019d18a4f05c4a09\",\"8a8d82a19cbd8f9b019d18a4f05c4a0a\",\"8a8d82a19cbd8f9b019d18a4f05c4a0b\",\"8a8d82a19cbd8f9b019d18a4f05c4a0c\",\"8a8d82a19cbd8f9b019d18a4fddf4b0c\",\"8a8d82a19cbd8f9b019d18a4fddf4b0d\",\"8a8d82a19cbd8f9b019d18a505694b8c\",\"8a8d82a19cbd8f9b019d18a50f884bee\",\"8a8d82a19cbd8f9b019d18a50f884bef\",\"8a8d82a19cbd8f9b019d18a50f884bf0\",\"8a8d82a19cbd8f9b019d18a50f884bf1\",\"8a8d82a19cbd8f9b019d18a50f884bf2\",\"8a8d82a19cbd8f9b019d18a50f884bf3\",\"8a8d82a19cbd8f9b019d18a50f884bf4\",\"8a8d82a19cbd8f9b019d18a50f884bf5\",\"8a8d82a19cbd8f9b019d18a50f884bf6\",\"8a8d82a19cbd8f9b019d18a50f884bf7\",\"8a8d82a19cbd8f9b019d18a5274c4db9\",\"8a8d82a19cbd8f9b019d18a52f4e4df5\",\"8a8d82a19cbd8f9b019d18a52f4e4df6\",\"8a8d82a19cbd8f9b019d18a536ff4e53\",\"8a8d82a19cbd8f9b019d18a53e414e9d\",\"8a8d82a19cbd8f9b019d18a53e414e9e\",\"8a8d82a19cbd8f9b019d18a53e414e9f\",\"8a8d82a19cbd8f9b019d18a547724f49\",\"8a8d82a19cbd8f9b019d18a547724f4a\",\"8a8d82a19cbd8f9b019d18a54e5c4f9a\",\"8a8d82a19cbd8f9b019d18a552474fde\",\"8a8d82a19cbd8f9b019d18a559f95028\",\"8a8d82a19cbd8f9b019d18a559f95029\",\"8a8d82a19cbd8f9b019d18a561775099\",\"8a8d82a19cbd8f9b019d18a56177509a\",\"8a8d82a19cbd8f9b019d18a56c4850fe\",\"8a8d82a19cbd8f9b019d18a56c4850ff\",\"8a8d82a19cbd8f9b019d18a56c485100\",\"8a8d82a19cbd8f9b019d18a56c485101\",\"8a8d82a19cbd8f9b019d18a56c485102\",\"8a8d82a19cbd8f9b019d18a56c485103\",\"8a8d82a19cbd8f9b019d18a56c485104\",\"8a8d82a19cbd8f9b019d18a57d01523e\",\"8a8d82a19cbd8f9b019d18a57d02523f\",\"8a8d82a19cbd8f9b019d18a586b6529f\",\"8a8d82a19cbd8f9b019d18a58e9a52fa\")";
        req.setSqlContent(queryRepeatSql);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 导出
        QueryDBHelper.exportExcel(QueryDBHelper.getRes(req, restTemplate).getData(), "生产错误报告信息");
    }


    @ApiOperation("查询错误的反馈数据4")
    @PostMapping(value = "queryErrorFeedback4")
    public void queryErrorFeedback4(@RequestBody QueryDBReq req) {
        String queryRepeatSql = "select id, safety_report_id, create_time from report_value where tenant_id = '8a81813e763fc30f017646131ddd51ce' and create_time > '2026-03-23' order by create_time";
        req.setSqlContent(queryRepeatSql);
        req.setLimitNum("0");
        req.setInstanceName("prod-mysql-pvcaps-ro");
        req.setDbName("pvs_report_all");
        // 导出
        //QueryDBHelper.exportExcel(QueryDBHelper.getRes(req, restTemplate).getData(), "错误租户数据");
        List<FixData> fixData = Lists.newArrayList();
        List<Map<String, String>> rows = QueryDBHelper.extractColumnValues(QueryDBHelper.getRes(req, restTemplate), Lists.newArrayList("id"));
        //
        rows.forEach(row -> {
            Map<String, String> columnValueMap  = Maps.newHashMap();
            columnValueMap.put("tenant_id", "1110");
            fixData.add(FixData.builder().id(row.get("id")).table("pvs_report_all.report_value").columnValueMap(columnValueMap).build());
        });
        System.out.println(fixData);
    }

}
