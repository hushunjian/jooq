package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.SchemaFieldTable;
import com.hushunjian.jooq.generator.tables.SchemaModuleTable;
import com.hushunjian.jooq.generator.tables.records.SchemaFieldRecord;
import com.hushunjian.jooq.generator.tables.records.SchemaModuleRecord;
import com.hushunjian.jooq.generator.tables.records.XianShenReportNoRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class SchemaDao {

    private final SchemaFieldTable schemaFieldTable = SchemaFieldTable.SCHEMA_FIELD.as("schemaField");

    private final SchemaModuleTable schemaModuleTable = SchemaModuleTable.SCHEMA_MODULE.as("schemaModule");

    @Resource
    private DSLContext dslContext;

    public List<SchemaFieldRecord> findAllSchemaFields() {
        return dslContext.selectFrom(schemaFieldTable).fetchInto(SchemaFieldRecord.class);
    }

    public List<SchemaModuleRecord> findAllSchemaModules() {
        return dslContext.selectFrom(schemaModuleTable).fetchInto(SchemaModuleRecord.class);
    }

    public void updateSchemaFields(List<SchemaFieldRecord> schemaFieldRecords) {
        // 先删除,在新增
        dslContext.deleteFrom(schemaFieldTable).execute();
        dslContext.batchInsert(schemaFieldRecords).execute();
    }

    public void updateSchemaModules(List<SchemaModuleRecord> schemaModuleRecords) {
        // 先删除,在新增
        dslContext.deleteFrom(schemaModuleTable).execute();
        dslContext.batchInsert(schemaModuleRecords).execute();
    }
}
