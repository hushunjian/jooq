package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.RawdataFolderTable;
import com.hushunjian.jooq.generator.tables.RawdataTable;
import com.hushunjian.jooq.generator.tables.records.RawdataFolderRecord;
import com.hushunjian.jooq.generator.tables.records.RawdataRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class RawDao {


    private final RawdataTable rawdataTable = RawdataTable.RAWDATA.as("raw");


    private final RawdataFolderTable rawdataFolderTable = RawdataFolderTable.RAWDATA_FOLDER.as("raw_folder");

    @Resource
    private DSLContext dslContext;


    public List<RawdataRecord> findAllRawData() {
        return dslContext.selectFrom(rawdataTable).fetchInto(RawdataRecord.class);
    }

    public List<RawdataFolderRecord> findAllRawDataFolder() {
        return dslContext.selectFrom(rawdataFolderTable).fetchInto(RawdataFolderRecord.class);
    }

    public void batchUpdateRawdataInfos(List<RawdataRecord> rawdataRecords) {
        dslContext.batchUpdate(rawdataRecords).execute();
    }

    public void batchUpdateRawdataFolders(List<RawdataFolderRecord> rawdataFolderRecords) {
        dslContext.batchUpdate(rawdataFolderRecords).execute();
    }

    public void updateRawdataFolderOutLine(String folderId, String outLine) {
        // 方式1
        dslContext.update(rawdataFolderTable).set(rawdataFolderTable.OUT_LINE, outLine).where(rawdataFolderTable.ID.eq(folderId)).execute();
    }
}
