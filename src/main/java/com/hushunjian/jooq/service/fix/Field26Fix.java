package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
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
public class Field26Fix implements XianShenBase {

    // ADR法
    public static final String ADR = "1e5e6c69-c161-11e8-a962-000c29399d7c";

    // 二分选项法
    public static final String TWO = "365533d2-c161-11e8-a962-000c29399d7c";

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告分类为“上市后境内报告”，并且评价模块中，报告者因果评价中不包含评价方法为”二分法“或者”直报系统方法“的评价记录时，
        // 对于公司因果评价，如果评价结果能与原结果值对应，则直接对应，保持原值。
        // 如果无法对应，有以下2种情况：
        // 如有报告者评价（本AE或其他AE），请均与报告者评价保持一致
        // 否则填写“无法评价”
        List<String> fixSql = Lists.newArrayList();
        // 查询评价
        List<EvaluationCausality> evaluationCausalities = Lists.newArrayList();
        // 分批查询数据
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryEvaluationCausality(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, evaluationCausalities));
        // 按照评价分组
        evaluationCausalities.stream().collect(Collectors.groupingBy(EvaluationCausality::getCausalityId)).forEach(((causalityId, causalities) -> {
            // 按照评价来源分组
            Map<String, List<EvaluationCausality>> sourceCausalitiesMap = causalities.stream().collect(Collectors.groupingBy(EvaluationCausality::getEvaluationSource));
            // 取直报或二分法的报告者评价
            List<EvaluationCausality> reporterCausalities = sourceCausalitiesMap.getOrDefault("报告者", Lists.newArrayList());
            // 移除掉不是直报或二分法的
            reporterCausalities.removeIf(causality -> !StringUtils.equalsAny(causality.getEvaluationMethodBreakdown(), ADR, TWO));
            if (CollectionUtils.isNotEmpty(reporterCausalities)) {
                // 判断是否有公司评价
                List<EvaluationCausality> companyCausalities = sourceCausalitiesMap.get("公司");
                if (CollectionUtils.isNotEmpty(companyCausalities)) {
                    // 不包含直报或二分法,从报告者评价里面取
                    EvaluationCausality firstReporterCausality = reporterCausalities.get(0);
                    // 有公司评价,判断是否包含
                    if (companyCausalities.stream().noneMatch(causality -> StringUtils.equalsAny(causality.getEvaluationMethodBreakdown(), ADR, TWO))) {
                        // 报告者的信息覆盖公司的
                        companyCausalities.forEach(causality -> fixSql.add(String.format("update tm_company_reporter set rough_classification = '%s', reporter_evaluation_dcitkey = '%s', reporter_evaluation = '%s' where id = '%s';", firstReporterCausality.getEvaluationMethod(), firstReporterCausality.getEvaluationMethodBreakdown(), firstReporterCausality.getEvaluationResult(), causality.getId())));
                    } else {
                        // 有二分法或者直报系统方法,在判断结果是否有值
                        causalities.stream().filter(causality ->
                                StringUtils.equalsAny(causality.getEvaluationMethodBreakdown(), ADR, TWO)
                                        && StringUtils.isBlank(causality.getEvaluationResult())).forEach(causality -> fixSql.add(String.format("update tm_company_reporter set reporter_evaluation = '%s' where id = '%s';", firstReporterCausality.getEvaluationResult(), causality.getId())));
                    }
                } else {
                    // 没有公司评价,复制数据
                    reporterCausalities.forEach(causality -> fixSql.add(String.format("INSERT INTO `tm_company_reporter`(`id`, `causality_item_id`, `causality_id`, `reporter_evaluation_dcitkey`, `rough_classification`, `reporter_evaluation`, `tenant_id`, `version`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`, `eu_source_of_assessment`, `data_source`, `evaluation_source`, `kt_result_of_assessment`, `wu_result_of_assessment`, `kr_method_of_assessment`) VALUES ('%s', NULL, '%s', '%s', '%s', '%s', 'af632df0-3999-4a35-9c2d-7269b4ecc6a0', 0, 'system', '2024-04-02 17:22:38.000000', NULL, NULL, 0, NULL, 0, '公司', NULL, NULL, NULL);", UUID.randomUUID(), causality.getCausalityId(), causality.getEvaluationMethodBreakdown(), causality.getEvaluationMethod(), causality.getEvaluationResult())));
                }
            } else {
                log.info("评价:[{}]没有报告者评价,请先优先处理报告者评价", causalityId);
            }
        }));
        return fixSql;
    }

    private void queryEvaluationCausality(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<EvaluationCausality> evaluationCausalities) {
        String querySql = String.format("select id, causality_id, rough_classification, reporter_evaluation_dcitkey, evaluation_source, reporter_evaluation from tm_company_reporter where causality_id in (select id from tm_causality where ReportId in ('%s') and is_deleted = 0) and is_deleted = 0 and evaluation_source in ('公司', '报告者');", String.join("','", reportIds));
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
            evaluationCausality.setEvaluationResult(row.get(5));
            evaluationCausalities.add(evaluationCausality);
        }
    }


    @Override
    public String fixField() {
        return "当报告分类为上市后境内报告，则必须使用二分法或直报系统方法填写公司因果评价|报告评价|公司因果评价";
    }

    @Override
    public int getOrder() {
        return 26;
    }
}
