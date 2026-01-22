package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.LineListingMappingTable;
import com.hushunjian.jooq.generator.tables.records.LineListingMappingRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class LineListingDao {


    private final LineListingMappingTable lineListingMappingTable = LineListingMappingTable.LINE_LISTING_MAPPING.as("lineListingMapping");

    @Resource
    private DSLContext dslContext;

    public List<LineListingMappingRecord> findAll() {
        return dslContext.selectFrom(lineListingMappingTable).fetchInto(LineListingMappingRecord.class);
    }

    public void batchUpdate(List<LineListingMappingRecord> lineListingMappingRecords) {
        dslContext.batchUpdate(lineListingMappingRecords).execute();
    }
}
