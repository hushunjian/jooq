package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.DictionaryEntryTable;
import com.hushunjian.jooq.generator.tables.DictionaryTable;
import com.hushunjian.jooq.generator.tables.records.DictionaryEntryRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class DictionaryDao {


    private final DictionaryEntryTable dictionaryEntryTable = DictionaryEntryTable.DICTIONARY_ENTRY.as("dictionaryEntry");

    @Resource
    private DSLContext dslContext;


    public List<DictionaryEntryRecord> findSystem() {
        return dslContext.selectFrom(dictionaryEntryTable).where(dictionaryEntryTable.TENANT_ID.eq("system")).fetchInto(DictionaryEntryRecord.class);
    }

}
