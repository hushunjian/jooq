package com.hushunjian.jooq.controller;

import com.google.common.collect.Lists;
import com.hushunjian.jooq.dao.RawDao;
import com.hushunjian.jooq.generator.tables.records.RawdataFolderRecord;
import com.hushunjian.jooq.generator.tables.records.RawdataRecord;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.FolderTree;
import com.hushunjian.jooq.req.SqlExInfo;
import com.hushunjian.jooq.util.OutLineUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("raw")
@RestController(value = "raw")
public class RawController {

    @Resource
    private RawDao rawDao;


    @ApiOperation("processFolderTree")
    @PostMapping(value = "processFolderTree")
    public void processFolderTree(@RequestBody FolderTree tree) {
        tree.setOutLine("0004");
        // 递归子级
        processTreeOutLine(tree);
        System.out.println();
        // 查询
        FolderTree outputTree = getTree();
        System.out.println();
    }

    private FolderTree getTree() {
        // 查询所有
        List<RawdataFolderRecord> all = rawDao.findAllRawDataFolder();
        // 按照parentId分组
        Map<String, List<RawdataFolderRecord>> parentChildrenMap = all.stream().collect(Collectors.groupingBy(RawdataFolderRecord::getParentId));
        // 获取根节点
        List<RawdataFolderRecord> roots = parentChildrenMap.get("");
        if (CollectionUtils.isEmpty(roots)) {
            return null;
        }
        List<FolderTree> result = Lists.newArrayList();
        // 排序
        sort(roots).forEach(root -> {
            // 根节点
            FolderTree folder = new FolderTree();
            BeanUtils.copyProperties(root, folder);
            // 子节点
            buildTree(folder, parentChildrenMap);
            result.add(folder);
        });
        if (result.size() != 1) {
            // 当前版本不允许存在多个根节点?
            throw new RuntimeException();
        }
        return result.get(0);
    }

    private void buildTree(FolderTree node, Map<String, List<RawdataFolderRecord>> parentChildrenMap) {
        if (parentChildrenMap.containsKey(node.getId())) {
            List<FolderTree> children = Lists.newArrayList();
            // 递归处理子节点 倒序
            sort(parentChildrenMap.get(node.getId())).forEach(folder -> {
                FolderTree child = new FolderTree();
                BeanUtils.copyProperties(folder, child);
                buildTree(child, parentChildrenMap);
                children.add(child);
            });
            node.setChilds(children);
        }
    }

    private List<RawdataFolderRecord> sort(List<RawdataFolderRecord> folders) {
        // 默认按照创建时间倒序
        folders.sort(Comparator.comparing(RawdataFolderRecord::getOutLine).reversed());
        return folders;
    }

    private void processTreeOutLine(FolderTree treeNode) {
        if (CollectionUtils.isNotEmpty(treeNode.getChilds())) {
            String preOutLine = "";
            // 倒序
            Collections.reverse(treeNode.getChilds());
            for (FolderTree child : treeNode.getChilds()) {
                preOutLine = OutLineUtil.next(treeNode.getOutLine(), preOutLine, OutLineUtil.STEP, OutLineUtil.DIGIT);
                child.setOutLine(preOutLine);
                // 更新记录
                rawDao.updateRawdataFolderOutLine(child.getId(), preOutLine);
                // 在递归子级
                processTreeOutLine(child);
            }
        }
    }

    @ApiOperation("test")
    @GetMapping(value = "test")
    public void test() {
        // 查询原始资料文件夹
        List<RawdataFolderRecord> folders = rawDao.findAllRawDataFolder();
        // 查询原始资料
        List<RawdataRecord> files = rawDao.findAllRawData();
        // 文件随机给一个租户id和文件夹id
        files.forEach(file -> {
            // 随机文件夹下标
            int index = RandomUtils.nextInt(0, folders.size());
            // 获取文件夹id
            RawdataFolderRecord folder = folders.get(index);
            // 修改文件的信息
            file.setFolderId(folder.getId());
            file.setTenantId(folder.getTenantId());
        });
        // 更新文件信息
        rawDao.batchUpdateRawdataInfos(files);
    }

    @ApiOperation("genExcel")
    @PostMapping(value = "genExcel")
    public void genExcel(@RequestBody List<SqlExInfo> sqlExInfos) {
        List<List<String>> table = Lists.newArrayList();
        // 表格
        sqlExInfos.forEach(sqlExInfo -> table.add(Lists.newArrayList(sqlExInfo.getSql_id().toString(), sqlExInfo.getSql_a(), sqlExInfo.getSql_b(), sqlExInfo.getA_execution_ms().toString(), sqlExInfo.getB_execution_ms().toString())));
        // 表头
        List<String> header = Lists.newArrayList("sql_id", "sql_a", "sql_b", "a_execution_ms", "b_execution_ms");
        // 导出
        QueryDBHelper.exportExcel2(header, table, "执行结果");
    }

    @ApiOperation("维护outLine")
    @GetMapping("/maintainOutLine")
    public void maintainOutLine() {
        // 查询文件夹数据
        List<RawdataFolderRecord> folders = rawDao.findAllRawDataFolder();
        // 排序处理器
        Comparator<RawdataFolderRecord> comparator = Comparator.comparing(RawdataFolderRecord::getOutLine, Comparator.nullsLast(String::compareTo)).thenComparing(RawdataFolderRecord::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo));
        // 处理文件夹数据
        maintainOutLine(folders, comparator);
        // 更新
        rawDao.batchUpdateRawdataFolders(folders);
    }

    public void maintainOutLine(List<RawdataFolderRecord> nodes, Comparator<RawdataFolderRecord> comparator) {
        // 按照parentId分组
        Map<String, List<RawdataFolderRecord>> parentChildrenMap = nodes.stream().collect(Collectors.groupingBy(RawdataFolderRecord::getParentId));
        // parentId是空的作为root
        List<RawdataFolderRecord> roots = parentChildrenMap.getOrDefault("root", parentChildrenMap.get("")).stream().filter(f -> 0 == f.getIsDeleted()).collect(Collectors.toList());
        // 从root开始递归
        String preOutLine = "";
        // 做一次排序
        roots.sort(comparator);
        for (RawdataFolderRecord root : roots) {
            root.setParentId("");
            preOutLine = OutLineUtil.next("", preOutLine, OutLineUtil.STEP, OutLineUtil.DIGIT);
            root.setOutLine(preOutLine);
            maintainChildrenOutLine(root, parentChildrenMap, comparator);
        }

    }

    private void maintainChildrenOutLine(RawdataFolderRecord parent, Map<String, List<RawdataFolderRecord>> parentChildrenMap, Comparator<RawdataFolderRecord> comparator) {
        List<RawdataFolderRecord> children = parentChildrenMap.get(parent.getId());
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        // 做一次排序
        children.sort(comparator);
        String preOutLine = "";
        for (RawdataFolderRecord child : children) {
            preOutLine = OutLineUtil.next(parent.getOutLine(), preOutLine, OutLineUtil.STEP, OutLineUtil.DIGIT);
            child.setOutLine(preOutLine);
            maintainChildrenOutLine(child, parentChildrenMap, comparator);
        }
    }
}
