package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.records.ReporterRecord;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field5Fix implements XianShenBase {

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中的字段“全球唯一编号（C.1.8.1）“为空时，
        // 将首要报告者的所在国家/地区批量处理为“中国”
        // 如果报告没有首要报告者，则将第一个报告者处理为首要报告者，并赋值所在国家/地区批量处理为“中国”
        // 报告者信息规整好后，触发全球唯一编号的生成
        List<String> fixSql = Lists.newArrayList();
        // 报告者信息
        List<ReporterRecord> reporterRecords = Lists.newArrayList();
        // 查询报告者信息
        Lists.partition(handleReportNos, 200).forEach(partReportNos -> {
            // 查询报告者信息
            queryReporters(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, reporterRecords);
        });
        // 开始处理信息,按照报告分组
        Map<String, List<ReporterRecord>> reportReportersMap = reporterRecords.stream().collect(Collectors.groupingBy(ReporterRecord::getReportId));
        // 循环处理报告
        handleReportNos.forEach(handleReportNo -> {
            List<ReporterRecord> reportReporterRecords = reportReportersMap.get(handleReportNo.getId());
            if (CollectionUtils.isNotEmpty(reportReporterRecords)) {
                // 找到首要报告者
                ReporterRecord primaryReporter = reportReporterRecords.stream()
                        .filter(reporter -> StringUtils.equals(reporter.getPrimaryReporter(), "3464e6b0-358a-4ef5-911c-c2b7cd438ae0"))
                        .findFirst().orElse(reportReporterRecords.get(0));
                // 非首要报告者 || 报告者国家是空
                if (!StringUtils.equals(primaryReporter.getPrimaryReporter(), "3464e6b0-358a-4ef5-911c-c2b7cd438ae0") || StringUtils.isBlank(primaryReporter.getReporterCountry())) {
                    // 设置成首要报告者
                    primaryReporter.setPrimaryReporter("3464e6b0-358a-4ef5-911c-c2b7cd438ae0");
                    // 如果国家是空,设置成中国
                    if (StringUtils.isBlank(primaryReporter.getReporterCountry())) {
                        // 设置成中国
                        primaryReporter.setReporterCountry("COUNTRY_CN");
                    }
                    // 更新报告者信息
                    fixSql.add(String.format("update tm_reporter set PrimaryReporter = '%s', ReporterCountry = '%s' where id = '%s';", primaryReporter.getPrimaryReporter(), primaryReporter.getReporterCountry(), primaryReporter.getId()));
                }
                // 更新报告全球唯一编号,看了上面数据,都是中国
                fixSql.add(String.format("update report_value set worldwideuniquenumber = '%s' where id = '%s';", "CN-91320000134783346P-" + handleReportNo.getReportNo().split("-")[0], handleReportNo.getId()));
            }
        });
        return fixSql;
    }


    private void queryReporters(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<ReporterRecord> reporterRecords) {
        String queryReporterSql = String.format("select id, PrimaryReporter as 'primary_reporter', ReporterCountry as 'reporter_country', ReportId as 'report_id' from tm_reporter where ReportId in (%s) and is_deleted = 0", "'" + StringUtils.join(reportIds, "','") + "'");
        req.setSqlContent(queryReporterSql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            ReporterRecord reporterRecord = new ReporterRecord();
            reporterRecord.setId(row.get(0));
            reporterRecord.setPrimaryReporter(row.get(1));
            reporterRecord.setReporterCountry(row.get(2));
            reporterRecord.setReportId(row.get(3));
            reporterRecords.add(reporterRecord);
        }
    }

    @Override
    public String fixField() {
        return "请填写全球唯一编号|报告详情|全球唯一编号（C.1.8.1）";
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
