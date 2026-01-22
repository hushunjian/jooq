package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.ApmDetailTable;
import com.hushunjian.jooq.generator.tables.records.ApmDetailRecord;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
public class ApmDetailDao {


    private final ApmDetailTable apmDetailTable = ApmDetailTable.APM_DETAIL.as("apmDetail");

    @Resource
    private DSLContext dslContext;

    public int batchInsert(List<ApmDetailRecord> apmDetailRecords) {
        return dslContext.batchInsert(apmDetailRecords).execute().length;
    }

    public List<ApmDetailRecord> findAll() {
        return dslContext.selectFrom(apmDetailTable).fetchInto(ApmDetailRecord.class);
    }


    public List<ApmDetailRecord> findByApmId(String apmId) {
        return dslContext.selectFrom(apmDetailTable).where(apmDetailTable.APM_ID.eq(apmId)).fetchInto(ApmDetailRecord.class);
    }
}
