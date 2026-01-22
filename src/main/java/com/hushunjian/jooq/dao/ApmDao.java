package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.ApmTable;
import com.hushunjian.jooq.generator.tables.records.ApmRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class ApmDao {


    private final ApmTable apmTable = ApmTable.APM.as("apm");

    @Resource
    private DSLContext dslContext;

    public int batchInsert(List<ApmRecord> apmRecords) {
        return dslContext.batchInsert(apmRecords).execute().length;
    }

    public List<ApmRecord> findAll() {
        return dslContext.selectFrom(apmTable).fetchInto(ApmRecord.class);
    }

    public void batchUpdate(List<ApmRecord> apmRecords) {
        dslContext.batchUpdate(apmRecords).execute();
    }
}
