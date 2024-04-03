package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.Evaluation;
import com.hushunjian.jooq.res.EvaluationCausality;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field25Fix implements XianShenBase {

    // ADR法
    public static final String ADR = "1e5e6c69-c161-11e8-a962-000c29399d7c";

    // 二分选项法
    public static final String TWO = "365533d2-c161-11e8-a962-000c29399d7c";

    public static final Map<String, String> evaluationResultMap = Maps.newHashMap();

    static {
        evaluationResultMap.put("肯定有关", "215f6cca-c154-11e8-a962-000c29399d7c");
        evaluationResultMap.put("很可能有关", "215f1919-c154-11e8-a962-000c29399d7c");
        evaluationResultMap.put("可能有关", "215fd28c-c154-11e8-a962-000c29399d7c");
        evaluationResultMap.put("可能无关", "215fcfc3-c154-11e8-a962-000c29399d7c");
        evaluationResultMap.put("待评价", "215fbba5-c154-11e8-a962-000c29399d7c");
        evaluationResultMap.put("无法评价", "f08c863f-c2c6-11e8-a962-000c29399d7c");
    }

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告分类为“上市后境内报告”，且报告者因果评价中不包含评价方法为”二分法“或者”直报系统方法“的评价记录时，
        // 将评价方法批量处理为“直报系统方法”。
        // 评价结果：如果评价结果能与原结果值对应，则直接对应，保持原值。如果无法对应，则选择“无法评价”
        List<String> fixSql = Lists.newArrayList();
        // 查询评价数据
        List<Evaluation> evaluations = Lists.newArrayList();
        // 查询评价细分
        List<EvaluationCausality> evaluationCausalities = Lists.newArrayList();
        // 分批查询数据
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询评价
            queryEvaluation(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, evaluations);
            // 查询评价细分
            queryEvaluationCausality(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, evaluationCausalities);
        });
        // 评价按照报告分组
        Map<String, List<Evaluation>> reportEvaluationsMap = evaluations.stream().collect(Collectors.groupingBy(Evaluation::getReportId));
        // 评价细分按照评价分组
        Map<String, List<EvaluationCausality>> causalityMap = evaluationCausalities.stream().collect(Collectors.groupingBy(EvaluationCausality::getCausalityId));
        // 循环报告处理
        handleReportNos.forEach(handleReportNo -> {
            // 报告评价
            List<Evaluation> reportEvaluations = reportEvaluationsMap.get(handleReportNo.getId());
            if (CollectionUtils.isEmpty(reportEvaluations)) {
                log.info("报告:[{}]没有评价,请先计算评价", handleReportNo.getId());
            } else {
                // 有评价在取评价数据
                reportEvaluations.forEach(evaluation -> {
                    List<EvaluationCausality> causalities = causalityMap.get(evaluation.getId());
                    if (CollectionUtils.isEmpty(causalities)) {
                        // 没有评价细分数据,新增一个
                        fixSql.add(String.format("INSERT INTO `tm_company_reporter`(`id`, `causality_item_id`, `causality_id`, `reporter_evaluation_dcitkey`, `rough_classification`, `reporter_evaluation`, `tenant_id`, `version`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `eu_source_of_assessment`, `data_source`, `evaluation_source`, `kt_result_of_assessment`, `wu_result_of_assessment`, `kr_method_of_assessment`) VALUES ('%s', NULL, '%s', '1e5e6c69-c161-11e8-a962-000c29399d7c', '运算法则', 'f08c863f-c2c6-11e8-a962-000c29399d7c', 'af632df0-3999-4a35-9c2d-7269b4ecc6a0', 0, 'system', '2024-04-02 17:22:38.000000', NULL, NULL, 0, NULL, 0, '报告者', NULL, NULL, NULL);", UUID.randomUUID(), evaluation.getId()));
                    } else {
                        // 有评价数据,再判断是否符合条件
                        // 报告者评价里面没有二分法或者直报系统方法
                        if (causalities.stream().noneMatch(causality -> StringUtils.equalsAny(causality.getEvaluationMethodBreakdown(), ADR, TWO))) {
                            // 全部替换
                            causalities.forEach(causality ->
                                    fixSql.add(String.format("update tm_company_reporter set rough_classification = '运算法则', reporter_evaluation_dcitkey = '1e5e6c69-c161-11e8-a962-000c29399d7c', reporter_evaluation = '%s' where id = '%s';", evaluationResultMap.getOrDefault(causality.getEvaluationResult(), evaluationResultMap.get("无法评价")), causality.getId())));
                        } else {
                            // 有二分法或者直报系统方法,在判断结果是否有值
                            causalities.stream().filter(causality ->
                                    StringUtils.equalsAny(causality.getEvaluationMethodBreakdown(), ADR, TWO)
                                            && StringUtils.isBlank(causality.getEvaluationResult())).forEach(causality -> fixSql.add(String.format("update tm_company_reporter set reporter_evaluation = '%s' where id = '%s';", evaluationResultMap.get("无法评价"), causality.getId())));
                        }
                    }
                });
            }
        });
        return fixSql;
    }

    private void queryEvaluation(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<Evaluation> evaluations) {
        String querySql = String.format("select id, ReportId from tm_causality where ReportId in ('%s') and is_deleted = 0", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Evaluation evaluation = new Evaluation();
            evaluation.setId(row.get(0));
            evaluation.setReportId(row.get(1));
            evaluations.add(evaluation);
        }
    }

    private void queryEvaluationCausality(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<EvaluationCausality> evaluationCausalities) {
        String querySql = String.format("select t1.id, t1.causality_id, t1.rough_classification, t1.reporter_evaluation_dcitkey, t1.evaluation_source, t1.reporter_evaluation, t2.Name_chs from tm_company_reporter t1 left join Items t2 on t1.reporter_evaluation = t2.Unique_code where t1.causality_id in (select id from tm_causality where ReportId in ('%s') and is_deleted = 0) and t1.is_deleted = 0 and evaluation_source = '报告者';", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            EvaluationCausality evaluationCausality = new EvaluationCausality();
            evaluationCausality.setId(row.get(0));
            evaluationCausality.setCausalityId(row.get(1));
            evaluationCausality.setEvaluationMethod(row.get(2));
            evaluationCausality.setEvaluationMethodBreakdown(row.get(3));
            evaluationCausality.setEvaluationSource(row.get(4));
            evaluationCausality.setEvaluationResult(row.get(6));
            evaluationCausalities.add(evaluationCausality);
        }
    }

    @Override
    public String fixField() {
        return "当报告分类为上市后境内报告，则必须使用二分法或直报系统方法填写报告者因果评价|报告评价|报告者因果评价";
    }

    @Override
    public int getOrder() {
        return 25;
    }
}
