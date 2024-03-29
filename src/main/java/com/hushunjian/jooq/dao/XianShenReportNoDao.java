package com.hushunjian.jooq.dao;


import com.hushunjian.jooq.generator.tables.XianShenReportNoTable;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
public class XianShenReportNoDao {


    private final XianShenReportNoTable xianShenReportNoTable = XianShenReportNoTable.XIAN_SHEN_REPORT_NO.as("xianShenReportNo");


    @Resource
    private DSLContext dslContext;

    public List<XianShenReportNo> findByReportNos(Collection<String> reportNos) {
        return dslContext.selectFrom(xianShenReportNoTable).where(xianShenReportNoTable.REPORT_NO.in(reportNos)).fetchInto(XianShenReportNo.class);
    }
}
