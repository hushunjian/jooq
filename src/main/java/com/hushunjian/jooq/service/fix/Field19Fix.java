package com.hushunjian.jooq.service.fix;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.QueryDBReq;
import com.hushunjian.jooq.res.D;
import com.hushunjian.jooq.res.Drug;
import com.hushunjian.jooq.res.XianShenReportNo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Field19Fix implements XianShenBase {

    public final static String RE_CHINESE = "[\u4E00-\u9FFF]";

    @Override
    public List<String> fixData(QueryDBReq req, List<XianShenReportNo> handleReportNos, RestTemplate restTemplate) {
        // 当报告中，产品模块的字段“商品名称”非中文字符时，
        // 将产品的通用名称赋值到商品名称
        List<String> fixSql = Lists.newArrayList();
        // 产品信息
        List<Drug> drugs = Lists.newArrayList();
        // 查询产品信息
        Lists.partition(handleReportNos, 200).forEach(partReportNos ->
                queryDrug(req, partReportNos.stream().map(XianShenReportNo::getId).collect(Collectors.toList()), restTemplate, drugs));
        // 判断商品名称是不是中文
        drugs.forEach(drug -> fixSql.add(String.format("update tm_drug set BrandName = GenericName where id = '%s';", drug.getId())));
        return fixSql;
    }

    private void queryDrug(QueryDBReq req, List<String> reportIds, RestTemplate restTemplate, List<Drug> drugs) {
        String querySql = String.format("SELECT id, ReportId, BrandName, GenericName FROM tm_drug WHERE ReportId in ('%s') and is_deleted = 0;", String.join("','", reportIds));
        req.setSqlContent(querySql);
        D res = QueryDBHelper.getRes(req, restTemplate);
        System.out.println("");
        for (List<String> row : res.getData().getRows()) {
            Drug drug = new Drug();
            drug.setId(row.get(0));
            drug.setReportId(row.get(1));
            drug.setBrandName(row.get(2));
            drug.setGenericName(row.get(3));
            if (!isContainChinese(drug.getBrandName())) {
                drugs.add(drug);
            }
        }
    }

    public static boolean isContainChinese(String value) {
        return isFind(RE_CHINESE, value);
    }

    public static boolean isFind(String regex, String content) {
        if (content == null) {
            //提供null的字符串为不匹配
            return false;
        }

        if (StringUtils.isEmpty(regex)) {
            //正则不存在则为全匹配
            return true;
        }
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(content);
        return matcher.find();
    }

    @Override
    public String fixField() {
        return "该字段只允许使用中文填写|产品信息|商品名称";
    }

    @Override
    public int getOrder() {
        return 19;
    }
}
