package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.*;
import com.hushunjian.jooq.generator.tables.records.*;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class ReportDao {

    private final ReportValueTable reportValueTable = ReportValueTable.REPORT_VALUE.as("reportValue");

    private final ReportProjectTable reportProjectTable = ReportProjectTable.REPORT_PROJECT.as("reportProject");

    private final DrugTable drugTable = DrugTable.DRUG.as("drug");

    private final ReportLanguageTable reportLanguageTable = ReportLanguageTable.REPORT_LANGUAGE.as("reportLanguage");

    private final ReportAuthTable reportAuthTable = ReportAuthTable.REPORT_AUTH.as("reportAuth");


    @Resource
    private DSLContext dslContext;

    public List<ReportLanguageRecord> findAllReportLanguage() {
        return dslContext.selectFrom(reportLanguageTable).fetchInto(ReportLanguageRecord.class);
    }

    public List<ReportValueRecord> findTenantReportValues(String tenantId, String locale) {
        return dslContext.selectFrom(reportValueTable).where(reportValueTable.TENANT_ID.eq(tenantId).and(reportValueTable.LOCALE.eq(locale))).fetchInto(ReportValueRecord.class);
    }

    public List<ReportProjectRecord> findReportProjectByReportId(String reportId) {
        return dslContext.selectFrom(reportProjectTable).where(reportProjectTable.REPORT_ID.eq(reportId)).fetchInto(ReportProjectRecord.class);
    }

    public List<DrugRecord> findDrugByReportId(String reportId) {
        return dslContext.selectFrom(drugTable).where(drugTable.REPORT_ID.eq(reportId)).fetchInto(DrugRecord.class);
    }

    public int batchReportAuthInsert(List<ReportAuthRecord> reportAuthRecords) {
        return dslContext.batchInsert(reportAuthRecords).execute().length;
    }
}
