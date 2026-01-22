package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.ReportCommonQueryTable;
import com.hushunjian.jooq.generator.tables.records.ReportCommonQueryRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class ReportCommonQueryDao {


    private final ReportCommonQueryTable reportCommonQueryTable = ReportCommonQueryTable.REPORT_COMMON_QUERY.as("reportCommonQuery");


    @Resource
    private DSLContext dslContext;

    public int batchInsert(List<ReportCommonQueryRecord> reportCommonQueryRecords) {
        return dslContext.batchInsert(reportCommonQueryRecords).execute().length;
    }

    public int batchUpdate(List<ReportCommonQueryRecord> reportCommonQueryRecords) {
        return dslContext.batchUpdate(reportCommonQueryRecords).execute().length;
    }

    public List<ReportCommonQueryRecord> getPage(int pageNo, int pageSize) {
        return dslContext.selectFrom(reportCommonQueryTable).orderBy(reportCommonQueryTable.CREATE_TIME.asc()).limit(pageNo * pageSize, pageSize).fetchInto(ReportCommonQueryRecord.class);
    }

    public int count() {
        return dslContext.fetchCount(reportCommonQueryTable);
    }
}
