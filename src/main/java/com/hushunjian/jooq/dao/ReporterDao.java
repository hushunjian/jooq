package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.ReporterTable;
import com.hushunjian.jooq.generator.tables.records.ReporterRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class ReporterDao {


    private final ReporterTable reporterTable = ReporterTable.REPORTER.as("reporter");


    @Resource
    private DSLContext dslContext;

    public int batchInsert(List<ReporterRecord> reporterRecords) {
        return dslContext.batchInsert(reporterRecords).execute().length;
    }
}
