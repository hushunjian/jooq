package com.hushunjian.jooq.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hushunjian.jooq.helper.QueryDBHelper;
import com.hushunjian.jooq.req.*;
import com.hushunjian.jooq.res.*;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("configMap")
@RestController(value = "configMap")
public class ConfigMapController {

    private static final String targetFileName = "aws-singapore-prod";

    private static final String namespace = "aws_singapore_prod";

    private static final String redis_host = "master.prod-redis-ccp.do3ekt.apse1.cache.amazonaws.com";

    private static final String redis_password = "FSZDVBXFGBSRRT456FDV354WED";

    /**
     * 美西UAT不需要
     * 新加坡生产需要
     */
    private static final String redis_url = "rediss://FSZDVBXFGBSRRT456FDV354WED@master.prod-redis-ccp.do3ekt.apse1.cache.amazonaws.com:6379";

    /**
     * 美西UAT需要服务后缀
     * 新加坡生产不需要
     */
    private static final Boolean removeNameSpace = true;

    private static final String mongo_url = "mongodb://ztuser:ztuserTM@aws-singapore-prod-mongo-global01.taimei.com:20001,aws-singapore-prod-mongo-global02.taimei.com:20001,aws-singapore-prod-mongo-global03.taimei.com:20001";

    private static final String mongo_rs_info = "replicaSet=prod_trailer";

    private static final String rabbit_host = "aws-singapore-prod-mq-2.taimei.com";

    private static final String rabbit_username = "pv";

    private static final String rabbit_password = "PV_adminuser@123";

    private static final String v4_datasource_username = "usr_pv";

    private static final String v4_datasource_password = "P__^^12IixYTxnb4";

    private static final String v5_datasource_username = "usr_pvcaps";

    private static final String v5_datasource_password = "Uyxo__^^q231Opnx";

    private static final String zkAddress = "aws-singapore-prod-zk-001.taimei.com:2181";

    private static final String elasticsearch_uris = "http://aws-singapore-prod-es7.taimei.com";

    private static final String kafka_bootstrap_servers = "10.136.135.129:9092,10.136.130.65:9092,10.136.138.164:9092";

    private static final String data_source_url = "aws-singapore-prod-db-itaimei.taimei.com:3310";

    private static final String topic_prefix = "PROD__";

    private static final String oppugnBaseUrl = "https://ap.trialos.com/";

    private static final String as2MdnUrl = "https://ap-edi.trialos.com/as2/mdn";

    private static final String env_prefix = "ap.trialos.com";

    private static final Map<String, String> mongo_replace_info_map = Maps.newLinkedHashMap();

    private static final Map<String, String> datasource_replace_info_map = Maps.newHashMap();

    private static final Map<String, String> service_appid_map = Maps.newLinkedHashMap();

    private static final Map<String, String> PROJECT_V_INFO_MAP = Maps.newLinkedHashMap();

    private static final String git_branch = "release/singapore";

    private static final String PROD_PROPERTIES = "prod.properties";

    private static final String CONFIG_DIR = "config";

    @Resource
    private RestTemplate restTemplate;

    static {
        mongo_replace_info_map.put("mongodb://pvuser:pvuserTM@prod-pvmongo-001.taimei.com:20001,prod-pvmongo-002.taimei.com:20001", mongo_url);
        mongo_replace_info_map.put("mongodb://traceuser:traceTM@prod-pvmongo-001.taimei.com:20001,prod-pvmongo-002.taimei.com:20001,prod-pvmongo-003.taimei.com:20001", mongo_url);
        mongo_replace_info_map.put("mongodb://traceuser:traceTM@prod-mongodb3-001.taimei.com:20001,prod-mongodb3-002.taimei.com:20001,prod-mongodb3-003.taimei.com:20001", mongo_url);
        mongo_replace_info_map.put("mongodb://traceuser:traceTM@10.0.67.8:20001,10.0.67.9:20001,10.0.67.10:20001", mongo_url);


        mongo_replace_info_map.put("replicaSet=prod_pv_rs", mongo_rs_info);
        mongo_replace_info_map.put("replicaSet=prodrs", mongo_rs_info);

        datasource_replace_info_map.put("prod-polarm-pvcaps001-sh2e.taimei.com:3306", data_source_url);
        datasource_replace_info_map.put("prod-polarm-pv001-sh2e.taimei.com:3306", data_source_url);
        datasource_replace_info_map.put("prod-bimysql-001.taimei.com:3310/app_report", String.format("%s/pv", data_source_url));
        datasource_replace_info_map.put("10.0.66.50:3310/app_report", String.format("%s/pv", data_source_url));
        datasource_replace_info_map.put("prod-polarm-bimysql-001-sh2e.taimei.com:3306/app_report", String.format("%s/pv", data_source_url));


        /*PROJECT_V_INFO_MAP.put("as2", "v4");
        service_appid_map.put("edi-web", "2c9f84fc8235923201823a45a87902e7");
        service_appid_map.put("edi-service", "2c9f84fc82359232018239bbe94f0260");

        PROJECT_V_INFO_MAP.put("esae-ms", "v4");
        service_appid_map.put("esae-admin", "2c9f8446823e0fb8018244147c6805fe");
        service_appid_map.put("esae-ms-service", "2c9f8446823e0fb801824405e64805ef");
        service_appid_map.put("esae-ms-web", "2c9f8446823e0fb80182436abedb040c");

        PROJECT_V_INFO_MAP.put("esae-sponsor", "v4");
        service_appid_map.put("esae-sponsor-web", "2c9f8446823e0fb80182435194cf03b8");

        PROJECT_V_INFO_MAP.put("esae-template", "v4");
        service_appid_map.put("esae-template-service", "2c9f8446823e0fb80182439c511704a3");

        PROJECT_V_INFO_MAP.put("faq", "v4");
        service_appid_map.put("pv-faq-web", "2c9f83d28232fb5d018233025db70004");

        PROJECT_V_INFO_MAP.put("pv-meddra", "v4");
        service_appid_map.put("pv-dictionary", "2c9f830e823429a80182347baef200ac");

        PROJECT_V_INFO_MAP.put("pv-summary_report", "v4");
        service_appid_map.put("pv-sur", "2c9f84fc823592320182384fac140048");

        PROJECT_V_INFO_MAP.put("caps", "v5");
        service_appid_map.put("caps-operation", "2c9f816180b6238b0180b781f22f0082");
        service_appid_map.put("caps", "2c9f816180b6238b0180b76fb6da005d");*/


        PROJECT_V_INFO_MAP.put("report", "v5");
        service_appid_map.put("pvs-report-convert", "2c9f823f926b43070192982d2df34363");
        service_appid_map.put("pvs-report-pipeline", "2c9180837e4d8c11017e4d99f63d0007");
        service_appid_map.put("pvs-report", "2c9180837e4d8c11017e4d8c11940000");

        /*PROJECT_V_INFO_MAP.put("pvs-track", "v5");
        service_appid_map.put("pvs-track-service", "2c9180837f43faa3017f498d5b480172");

        PROJECT_V_INFO_MAP.put("gvp", "v5");
        service_appid_map.put("gvp-api", "2c9f816180b6238b0180b77d9a6a0073");

        PROJECT_V_INFO_MAP.put("gvp-workbench", "v5");
        service_appid_map.put("gvp-workbench", "2c9180837f43faa3017f71a943950417");

        PROJECT_V_INFO_MAP.put("pvs-middle-data", "v5");
        service_appid_map.put("pvs-middle-data-api", "2c9180837e4d8c11017e4dd3f0360042");*/
    }

    private static final Path WORKSPACE_ROOT = Paths.get("D:/project").toAbsolutePath().normalize();

    private static Path resolveProjectRoot(String projectName) {
        if (StringUtils.isBlank(projectName)) {
            throw new IllegalArgumentException("projectName 不能为空");
        }
        Path root = WORKSPACE_ROOT;
        Path projectDir = root.resolve(projectName.trim()).normalize();
        if (!projectDir.startsWith(root)) {
            throw new IllegalArgumentException("非法 projectName: " + projectName);
        }
        return projectDir;
    }

    @GetMapping("copyProdProperties")
    @ApiOperation("多项目复制 prod.properties，仅替换 REPLACE_MAP_INFO 中的键")
    public List<CopyConfigMapRes> copyProdProperties() throws IOException {
        List<CopyConfigMapRes> result = Lists.newArrayList();
        // 循环配置生成
        for (Map.Entry<String, String> entry : PROJECT_V_INFO_MAP.entrySet()) {
            String projectName = entry.getKey();
            Path base = resolveProjectRoot(projectName);
            if (!Files.isDirectory(base)) {
                log.warn("项目目录不存在，跳过: {}", base.toAbsolutePath());
                continue;
            }
            final List<Path> sources = Lists.newArrayList();
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
                    Path parent = file.getParent();
                    if (parent != null && CONFIG_DIR.equals(parent.getFileName().toString()) && PROD_PROPERTIES.equals(file.getFileName().toString())) {
                        sources.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            if (sources.isEmpty()) {
                log.warn("未在 [{}] {} 下找到 config/{}", projectName, base.toAbsolutePath(), PROD_PROPERTIES);
                continue;
            }
            for (Path source : sources) {
                // 复制文件
                Path targetFile = copyFile(source, targetFileName);
                // 文件路径
                String filePath = targetFile.toAbsolutePath().toString();
                log.info("开始处理文件:[{}]配置替换", filePath);
                try {
                    result.add(rewritePropertiesPreservingLines(targetFile, entry.getValue()));
                } catch (IOException e) {
                    log.error("无法处理文件: {}, {}", targetFile.toAbsolutePath(), e.getMessage(), e);
                }
                //Files.deleteIfExists(targetFile);
                System.out.println();
            }
        }
        return result;
    }

    private CopyConfigMapRes rewritePropertiesPreservingLines(Path targetFile, String v) throws IOException {
        // 相关的所有配置
        Map<String, Set<String>> relateAllKeysMap = Maps.newHashMap();
        // 发生变更的所有配置
        List<ChangeConfigInfoRes> changeConfigInfos = Lists.newArrayList();
        List<String> lines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>(lines.size());
        for (String line : lines) {
            String key = getKey(line);
            if (StringUtils.isNotBlank(key) && StringUtils.contains(key, "redis") && StringUtils.contains(key, "host") && StringUtils.isNotBlank(redis_url)) {
                // 生成ssl方式的url
                String newValue = String.format("%s = %s", key.replace("host", "url"), redis_url);
                // 追加url
                out.add(newValue);
                // 追加ssl = true
                out.add(String.format("%s = true", key.replace("host", "ssl")));
                // 标记变更
                changeConfigInfos.add(new ChangeConfigInfoRes("", newValue));
            }
            String newValue = replaceLineInfo(v, line, relateAllKeysMap);
            // 质疑回复地址
            if (StringUtils.equals(key, "oppugn.baseUrl")) {
                newValue = String.format("oppugn.baseUrl = %s", oppugnBaseUrl);
            }
            // as2 mdn url
            if (StringUtils.equals(key, "as2.mdn.url")) {
                newValue = String.format("as2.mdn.url = %s", as2MdnUrl);
            }
            // UAT环境,PROD环境topic前缀处理
            if (StringUtils.contains(key, "topic")) {
                newValue = newValue.replace("PROD__", topic_prefix);
            }
            // 处理服务后缀
            if (BooleanUtils.isTrue(removeNameSpace) && StringUtils.endsWithAny(newValue, ".middle", ".fleming", ".bigdata")) {
                newValue = newValue.substring(0, newValue.lastIndexOf("."));
            }
            // 需要移除得配置
            if (!StringUtils.contains(newValue, "need-remove")) {
                out.add(newValue);
            }
            // 标记变更
            if (!StringUtils.equals(line, newValue)) {
                changeConfigInfos.add(new ChangeConfigInfoRes(line, newValue));
            }
        }
        Path tmp = Files.createTempFile(targetFile.getParent(), targetFile.getFileName() + ".", ".tmp");
        try {
            Files.write(tmp, out, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
        return CopyConfigMapRes.builder()
                .filePath(targetFile.toAbsolutePath().toString())
                .relatedConfigKeysMap(relateAllKeysMap)
                .changeConfigInfos(changeConfigInfos)
                .build();
    }

    @SneakyThrows
    private Path copyFile(Path source, String fileName) {
        Path target = source.getParent().resolve(fileName + ".properties");
        // 复制文件
        return Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private String replaceNewView(String key, String oldValue, String v, Map<String, Set<String>> relateAllKeysMap) {
        // 特殊处理的key,MQ拆分租户,必须要有MQ(pvs.new.version.report.queue.A,pvs.new.report.queue.A)
        if (StringUtils.equalsAny(key, "pvs.create.report.tenant.split", "pvs.new.version.create.report.tenant.split")) {
            return "A";
        }
        if (StringUtils.contains(key, "redis")) {
            relateAllKeysMap.computeIfAbsent("redis", value -> Sets.newHashSet()).add(key);
            // redis替换
            if (StringUtils.contains(key, "host")) {
                // 地址
                return redis_host;
            } else if (StringUtils.contains(key, "password")) {
                // 密码
                return redis_password;
            }
        } else if (StringUtils.contains(key, "mongo")) {
            relateAllKeysMap.computeIfAbsent("mongo", value -> Sets.newHashSet()).add(key);
            // mongo处理
            if (StringUtils.contains(key, "uri")) {
                // 地址
                return replaceOldLineInfo(oldValue, mongo_replace_info_map);
            }
        } else if (StringUtils.contains(key, "rabbit")) {
            relateAllKeysMap.computeIfAbsent("rabbit", value -> Sets.newHashSet()).add(key);
            // rabbit处理
            if (StringUtils.contains(key, "host")) {
                if (!StringUtils.contains(key, "virtual")) {
                    // 地址
                    return rabbit_host;
                }
            } else if (StringUtils.contains(key, "username")) {
                // 用户名
                return rabbit_username;
            } else if (StringUtils.contains(key, "password")) {
                // 密码
                return rabbit_password;
            }
        } else if (StringUtils.contains(key, "datasource")) {
            relateAllKeysMap.computeIfAbsent("datasource", value -> Sets.newHashSet()).add(key);
            // 数据库
            if (StringUtils.contains(key, "url")) {
                if (!StringUtils.contains(key, "pattern")) {
                    // 地址
                    return replaceOldLineInfo(oldValue, datasource_replace_info_map);
                }
            } else if (StringUtils.contains(key, "username")) {
                // 用户名
                if (!StringUtils.contains(key, "login")) {
                    return v4DataSource(v, key) ? v4_datasource_username : v5_datasource_username;
                }
            } else if (StringUtils.contains(key, "password")) {
                // 密码
                if (!StringUtils.contains(key, "login")) {
                    return v4DataSource(v, key) ? v4_datasource_password : v5_datasource_password;
                }
            }
        } else if (StringUtils.contains(key, "zkAddress")) {
            relateAllKeysMap.computeIfAbsent("zkAddress", value -> Sets.newHashSet()).add(key);
            // zookeeper
            return zkAddress;
        } else if (StringUtils.contains(key, "elasticsearch") || StringUtils.contains(key, ".es.")) {
            relateAllKeysMap.computeIfAbsent("elasticsearch", value -> Sets.newHashSet()).add(key);
            // elasticsearch
            if (StringUtils.contains(key, "uris") || StringUtils.contains(key, ".clientAddress") || StringUtils.contains(key, ".address")) {
                return elasticsearch_uris;
            } else if (StringUtils.containsAny(key, "username", "password", ".es.security.")) {
                // ES不要密码,删除掉用户,密码相关配置
                return "need-remove";
            }
        } else if (StringUtils.contains(key, "kafka")) {
            relateAllKeysMap.computeIfAbsent("kafka", value -> Sets.newHashSet()).add(key);
            // kafka
            if (StringUtils.contains(key, "bootstrap-servers")) {
                return kafka_bootstrap_servers;
            }
        }
        return "";
    }

    private boolean v4DataSource(String v, String key) {
        if (StringUtils.contains(v, "v4")) {
            return !StringUtils.contains(key, "pvs_report_all");
        } else {
            return false;
        }
    }

    private String getKey(String line) {
        int eq = line.indexOf('=');
        if (eq < 0) {
            return "";
        }
        return line.substring(0, eq).trim();
    }

    private String replaceLineInfo(String v, String line, Map<String, Set<String>> relateAllKeysMap) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
            return line;
        }
        int eq = line.indexOf('=');
        if (eq < 0) {
            return line;
        }
        String key = line.substring(0, eq).trim();
        // 替换新值
        String newValue = replaceNewView(key, line.substring(eq + 1), v, relateAllKeysMap);
        if (StringUtils.isBlank(newValue)) {
            // 新值是空,返回原来的
            return line;
        }
        return line.substring(0, eq + 1) + newValue;
    }

    private String replaceOldLineInfo(String oldLineInfo, Map<String, String> replaceValueInfoMap) {
        String result = oldLineInfo;
        for (Map.Entry<String, String> entry : replaceValueInfoMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        // 判断一次
        if (StringUtils.equals(oldLineInfo, result)) {
            log.info("旧值:[{}]未成功替换", oldLineInfo);
            throw new RuntimeException("未替换成功");
        }
        return result;
    }


    @ApiOperation("复制发布环境配置")
    @PostMapping(value = "copyEnvConfig")
    public void copyEnvConfig(@RequestBody ConfigMapReq req) {
        // 源空间
        String sourceNamespace = "cttq_local";
        // 目标空间
        List<String> targetNamespaces = Lists.newArrayList(namespace);
        // 循环处理
        service_appid_map.forEach((serviceName, appId) -> {
            log.info("复制发布环境配置:服务名[{}],源:[{}]->目标:[{}]", serviceName, sourceNamespace, targetNamespaces);
            QueryDBHelper.copyEnvConfig(req.getCookie(), restTemplate, CopyEnvConfigReq.builder().appId(appId).sourceNamespace(sourceNamespace).targetNamespaces(targetNamespaces).build());
        });
    }

    @ApiOperation("推送configMap配置")
    @PostMapping(value = "pushConfigMap")
    public void pushConfigMap(@RequestBody ConfigMapReq req) {
        // 循环推送配置
        service_appid_map.forEach((serviceName, appId) -> {
            log.info("服务:[{}]推送configMap配置", serviceName);
            QueryDBHelper.pushConfigMap(req.getCookie(), restTemplate, PushConfigMapReq.builder().appId(appId).branchName(git_branch).configurationVersion("default").namespace(namespace).build());
        });
    }

    @ApiOperation("sonar扫描")
    @PostMapping(value = "sonarScan")
    public void sonarScan(@RequestBody ConfigMapReq req) {
        // 循环进行sonar扫描
        service_appid_map.forEach((serviceName, appId) -> {
            log.info("服务:[{}]进行sonar扫描", serviceName);
            QueryDBHelper.sonarScan(req.getCookie(), restTemplate, appId, git_branch);
        });
    }

    @ApiOperation("sonar扫描状态检查")
    @PostMapping(value = "sonarScanStatusCheck")
    public void sonarScanStatusCheck(@RequestBody ConfigMapReq req) {
        // 循环进行sonar扫描状态检查
        service_appid_map.forEach((serviceName, appId) -> {
            log.info("服务:[{}]进行sonar扫描状态检查", serviceName);
            Boolean success = QueryDBHelper.sonarScanStatusCheck(req.getCookie(), restTemplate, SonarScanStatusCheckReq.builder().appId(appId).branchName(git_branch).pageNum(1).pageSize(1).build());
            if (BooleanUtils.isNotTrue(success)) {
                log.error("服务:[{}]分支:[{}]扫描未通过", serviceName, git_branch);
            } else {
                log.info("服务:[{}]分支:[{}]扫描通过", serviceName, git_branch);
            }
            System.out.println();
        });
    }


    @ApiOperation("服务发布")
    @PostMapping(value = "deployService")
    public void deployService(@RequestBody ConfigMapReq req) {
        // 循环进行服务发布
        service_appid_map.forEach((serviceName, appId) -> {
            log.info("服务:[{}]进行发布", serviceName);
            QueryDBHelper.deployService(req.getCookie(), restTemplate, ServiceDeployReq.builder()
                    .appId(appId)
                    .branchName(git_branch)
                    .deployIssueKey("PVS5-22639")
                    .configurationVersion("default")
                    .deploymentVersion("default")
                    .jdkVersion("1.8")
                    .namespace(namespace)
                    .noCache(0)
                    .triggerType("BUILD_NEW")
                    .build());
        });
    }

    @ApiOperation("服务发布状态检查")
    @PostMapping(value = "deployStatusCheck")
    public void deployStatusCheck(@RequestBody ConfigMapReq req) {
        // 循环进行服务发布
        service_appid_map.forEach((serviceName, appId) -> {
            log.info("检查服务:[{}]发布状态", serviceName);
            Boolean success = QueryDBHelper.deployStatusCheck(req.getCookie(), restTemplate, ServiceDeployStatusCheckReq.builder()
                    .appId(appId)
                    .branchName(git_branch)
                    .namespaces(Lists.newArrayList(namespace))
                    .pageNum(1)
                    .pageSize(10)
                    .build());
            if (BooleanUtils.isNotTrue(success)) {
                log.error("服务:[{}]分支:[{}],环境:[{}]发布失败", serviceName, git_branch, namespace);
            } else {
                log.info("服务:[{}]分支:[{}],环境:[{}]发布成功", serviceName, git_branch, namespace);
            }
        });
    }

    @ApiOperation("downloadTemplateFile")
    @PostMapping(value = "downloadTemplateFile")
    public void downloadTemplateFile(@RequestBody QueryProdScheduleTaskReq req) {
        // 查询生产环境的模板
        List<TemplateFileRes> templateFiles = QueryDBHelper.getProdTemplateFile(restTemplate, req);
        // 导出目录
        String baseFolderPath = "D:\\download\\jooq\\Desktop\\template_file";
        // 循环下载到本地
        templateFiles.forEach(templateFile -> {
            // 点导出
            FileMetaInfo fileMetaInfo = QueryDBHelper.exportProdTemplateFile(restTemplate, req, templateFile.getId());
            // 导出到本地
            QueryDBHelper.download(fileMetaInfo.getFileId(), fileMetaInfo.getOriginFileName(), restTemplate, baseFolderPath, req.getDownloadFileCookie());
            // 在上传文件到目标服务
            FileMetaInfo uploadFileInfo = QueryDBHelper.uploadFile(String.format("%s\\%s", baseFolderPath, fileMetaInfo.getOriginFileName()), restTemplate, req.getUploadFileCookie(), env_prefix);
            // 更新记录
            boolean success = QueryDBHelper.templateFileOverlayImport(restTemplate, req.getUploadFileCookie(), uploadFileInfo.getFileId(), env_prefix, removeNameSpace);
            log.info("模板id:[{}]文件更新状态:[{}]", templateFile.getId(), success);
        });
    }


    @ApiOperation("copyProdScheduleTask")
    @PostMapping(value = "copyProdScheduleTask")
    public void copyProdScheduleTask(@RequestBody QueryProdScheduleTaskReq req) {
        // 查询生产的定时任务
        List<ProdScheduleTaskDetail> prodScheduleTaskDetails = QueryDBHelper.queryProdScheduleTask(restTemplate, req);
        // 往指定环境循环新增
        prodScheduleTaskDetails.forEach(prodScheduleTaskDetail -> {
            // 是否需要修改.pv后缀
            if (removeNameSpace) {
                prodScheduleTaskDetail.setCallbackUrl(prodScheduleTaskDetail.getCallbackUrl().replace(".pv/", "/"));
            }
            Boolean success = QueryDBHelper.addScheduleTask(restTemplate, req.getAddTaskEnvCookie(), prodScheduleTaskDetail, env_prefix);
            log.info("定时任务:[{}]添加状态:[{}]", prodScheduleTaskDetail.getJobCode(), success);
        });
    }


    @ApiOperation("mqInfo")
    @PostMapping(value = "mqInfo")
    public void mqInfo(@RequestBody MQInfo req) throws IOException {
        Map<String, List<QueueInfo>> businessQueueInfosMap = Maps.newLinkedHashMap();
        // 新加坡MQ信息
        MQInfo xinjiapoMqInfo = readMq();
        // 收集已存在
        List<String> xinjiapoQueueNames = xinjiapoMqInfo.getQueues().stream().map(QueueInfo::getName).collect(Collectors.toList());
        // 追加不要得
        addIgnoreQueues(xinjiapoQueueNames);
        req.getQueues().stream().filter(queue -> !xinjiapoQueueNames.contains(queue.getName())).sorted(Comparator.comparing(QueueInfo::getName))
                .forEach(queue -> businessQueueInfosMap.computeIfAbsent(mqType(queue.getName()), v -> Lists.newArrayList()).add(queue));
        List<String> queueNames = businessQueueInfosMap.get("pv").stream().map(QueueInfo::getName).collect(Collectors.toList());
        System.out.println("队列信息");
        // 交换机绑定信息队列信息
        Map<String, Map<String, BindingInfo>> exchangeBindingQueueMap = Maps.newLinkedHashMap();
        // 绑定信息
        List<BindingInfo> bindingInfos = Lists.newArrayList();
        // 循环绑定信息,找到对应的绑定信息
        req.getBindings().forEach(binding -> {
            if (queueNames.contains(binding.getDestination())) {
                exchangeBindingQueueMap.computeIfAbsent(binding.getSource(), v -> Maps.newLinkedHashMap()).put(binding.getDestination(), binding);
                bindingInfos.add(binding);
            }
        });
        System.out.println("交换机绑定关系");
        // 再找出所有的交换机信息
        List<ExchangeInfo> exchangeInfos = req.getExchanges().stream().filter(exchangeInfo -> exchangeBindingQueueMap.containsKey(exchangeInfo.getName())).collect(Collectors.toList());
        System.out.println("交换机信息");
        // 返回MQ信息
        MQInfo result = MQInfo.builder()
                .queues(businessQueueInfosMap.get("pv"))
                .exchanges(exchangeInfos)
                .bindings(bindingInfos)
                .build();
        System.out.println();
    }

    private void addIgnoreQueues(List<String> xinjiapoQueueNames) {
        xinjiapoQueueNames.add("''_clinicalDataQueue");
        xinjiapoQueueNames.add("MILESTONE_MESSAGE_QUEUE");
        xinjiapoQueueNames.add("MONI_BASIC_CONFIG_CHANGE_QUEUE");
        xinjiapoQueueNames.add("SASQueueName_Unicode");
        xinjiapoQueueNames.add("SIGN_PERSON_CERT_CALLBACK_QUEUE");
        xinjiapoQueueNames.add("TASK-QUEUE");
        xinjiapoQueueNames.add("TEST");
        xinjiapoQueueNames.add("TestDirectQueue");
        xinjiapoQueueNames.add("WW_RG_CUP001");
        xinjiapoQueueNames.add("a");
        xinjiapoQueueNames.add("aaa");
        xinjiapoQueueNames.add("add_project_ww");
        xinjiapoQueueNames.add("affiliate_approval_queue");
        xinjiapoQueueNames.add("aiiiii");
        xinjiapoQueueNames.add("asset.project.update.queue");
        xinjiapoQueueNames.add("authChangeQueue");
        xinjiapoQueueNames.add("auth_hcc");
        xinjiapoQueueNames.add("b");
        xinjiapoQueueNames.add("bb");
        xinjiapoQueueNames.add("bbb");
        xinjiapoQueueNames.add("bizQueue");
        xinjiapoQueueNames.add("compress_zip_queue");
        xinjiapoQueueNames.add("countryArea001");
        xinjiapoQueueNames.add("cru_ms_form_value_queue");
        xinjiapoQueueNames.add("ctTest");
        xinjiapoQueueNames.add("consumer-sc");
        xinjiapoQueueNames.add("davinci_tenant_auth_queue");
        xinjiapoQueueNames.add("dead_sur_queue");
        xinjiapoQueueNames.add("enterpriseSynQueue");
        xinjiapoQueueNames.add("eproResearchersInvitationQueue");
        xinjiapoQueueNames.add("esClientLogQueue");
        xinjiapoQueueNames.add("financeSchedulePlanUpdateQueue");
        xinjiapoQueueNames.add("flewFlowQueue");
        xinjiapoQueueNames.add("kf_analyze_queue");
        xinjiapoQueueNames.add("node.queue.ruansu-SFE-SALE-DATA-APPEAL-APP");
        xinjiapoQueueNames.add("oldInstitutionStatusQueue");
        xinjiapoQueueNames.add("open001.fqs");
        xinjiapoQueueNames.add("open002.fqs");
        xinjiapoQueueNames.add("original.material.batch.download.queue");
        xinjiapoQueueNames.add("pv.original.material.batch.download.queue");
        xinjiapoQueueNames.add("planner_milestone_bus_exchange_planner_queue");
        xinjiapoQueueNames.add("process.queue.ruansu-SFE-SALE-DATA-APPEAL-APP");
        xinjiapoQueueNames.add("sss");
    }

    public MQInfo readMq() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/rabbit_rabbitmq-2_2026-3-30.json")) {
            if (inputStream == null) {
                throw new IOException("File not found");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            // 将输入流转换成MQInfo对象
            return objectMapper.readValue(inputStream, MQInfo.class);
        }
    }

    private String mqType(String queueName) {
        if (StringUtils.contains(queueName, "fs")) {
            return "fs";
        } else if (StringUtils.contains(queueName, "edc") || StringUtils.contains(queueName, "EDC")) {
            return "edc";
        } else if (StringUtils.contains(queueName, "boExport")) {
            return "boExport";
        } else if (StringUtils.contains(queueName, "dataMigration")) {
            return "dataMigration";
        } else if (StringUtils.contains(queueName, "ejc.trail")) {
            return "ejc.trail";
        } else if (StringUtils.contains(queueName, "drug_register_")) {
            return "drug_register_";
        } else if (StringUtils.contains(queueName, "e-sms.")) {
            return "e-sms.";
        } else if (StringUtils.contains(queueName, "dcr_")) {
            return "dcr_";
        } else if (StringUtils.contains(queueName, "clinicalData")) {
            return "clinicalData";
        } else if (StringUtils.contains(queueName, "etmf")) {
            return "etmf";
        } else if (StringUtils.contains(queueName, "yield")) {
            return "yield";
        } else if (StringUtils.contains(queueName, "site")) {
            return "site";
        } else if (StringUtils.contains(queueName, "ssu")) {
            return "ssu";
        } else if (StringUtils.contains(queueName, "ccp") && !StringUtils.contains(queueName, "esae-ms")) {
            return "ccp";
        } else if (StringUtils.contains(queueName, "trial") || StringUtils.contains(queueName, "Trial") || StringUtils.contains(queueName, "trail")) {
            return "trial";
        } else if (StringUtils.contains(queueName, "evigi") || StringUtils.contains(queueName, "eVigi") || StringUtils.contains(queueName, "eivig") || StringUtils.contains(queueName, "EVIGI")) {
            return "evigi";
        } else if (StringUtils.contains(queueName, "csp") && !StringUtils.startsWith(queueName, "pvs")) {
            return "csp";
        } else if (StringUtils.contains(queueName, "amq")) {
            return "amq";
        } else if (StringUtils.contains(queueName, "ApiResult")) {
            return "ApiResult";
        } else if (StringUtils.contains(queueName, "BI")) {
            return "BI";
        } else if (StringUtils.contains(queueName, "CTMS") || StringUtils.contains(queueName, "ctms")) {
            return "CTMS";
        } else if (StringUtils.contains(queueName, "aiD")) {
            return "aiD";
        } else if (StringUtils.contains(queueName, "autoTest")) {
            return "autoTest";
        } else if (StringUtils.contains(queueName, "Dm")) {
            return "Dm";
        } else if (StringUtils.contains(queueName, "Doc")) {
            return "Doc";
        } else if (StringUtils.contains(queueName, "ELEARNING_SERVICE_PDF")) {
            return "ELEARNING_SERVICE_PDF";
        } else if (StringUtils.contains(queueName, "JobQueue")) {
            return "JobQueue";
        } else if (StringUtils.contains(queueName, "MDS")) {
            return "MDS";
        } else if (StringUtils.contains(queueName, "aiqc")) {
            return "aiqc";
        } else if (StringUtils.contains(queueName, "batchUpdate")) {
            return "batchUpdate";
        } else if (StringUtils.contains(queueName, "clean_")) {
            return "clean_";
        } else if (StringUtils.contains(queueName, "coding") && !StringUtils.startsWith(queueName, "pvs.batch")) {
            return "coding";
        } else if (StringUtils.contains(queueName, "NotificationRecord")) {
            return "QueueQrx";
        } else if (StringUtils.contains(queueName, "SFE_")) {
            return "SFE_";
        } else if (StringUtils.contains(queueName, "common_delay")) {
            return "common_delay";
        } else if (StringUtils.contains(queueName, "test")) {
            return "test";
        } else if (StringUtils.contains(queueName, "dct")) {
            return "dct";
        } else if (StringUtils.contains(queueName, "deepcrm")) {
            return "deepcrm";
        } else if (StringUtils.contains(queueName, "demo")) {
            return "demo";
        } else if (StringUtils.contains(queueName, "eArchive") || StringUtils.contains(queueName, "eArchvie")) {
            return "eArchive";
        } else if (StringUtils.contains(queueName, "ecollege")) {
            return "ecollege";
        } else if (StringUtils.contains(queueName, "exportDraft")) {
            return "exportDraft";
        } else if (StringUtils.contains(queueName, "exportCoding")) {
            return "exportCoding";
        } else if (StringUtils.contains(queueName, "evisit")) {
            return "evisit";
        } else if (StringUtils.contains(queueName, "exportJobRabbitMqName")) {
            return "exportJobRabbitMqName";
        } else if (StringUtils.contains(queueName, "exportLightQueue")) {
            return "exportLightQueue";
        } else if (StringUtils.contains(queueName, "exportResponse")) {
            return "exportResponse";
        } else if (StringUtils.contains(queueName, "chuang")) {
            return "chuang";
        } else if (StringUtils.contains(queueName, "extra_")) {
            return "extra_";
        } else if (StringUtils.startsWithAny(queueName, "0", "1", "2", "3", "6", "7", "8")) {
            return "number";
        } else if (StringUtils.contains(queueName, "hzh")) {
            return "hzh";
        } else if (StringUtils.contains(queueName, "adapter")) {
            return "adapter";
        } else if (StringUtils.contains(queueName, "ecdisc")) {
            return "ecdisc";
        } else if (StringUtils.contains(queueName, "exportM")) {
            return "exportM";
        } else if (StringUtils.contains(queueName, "exportN")) {
            return "exportN";
        } else if (StringUtils.contains(queueName, "exportSubject")) {
            return "exportSubject";
        } else if (StringUtils.contains(queueName, "exportVersion")) {
            return "exportVersion";
        } else if (StringUtils.contains(queueName, "exportWeight")) {
            return "exportWeight";
        } else if (StringUtils.contains(queueName, "fanout")) {
            return "fanout";
        } else if (StringUtils.contains(queueName, "formValue")) {
            return "formValue";
        } else if (StringUtils.contains(queueName, "iflow")) {
            return "iflow";
        } else if (StringUtils.contains(queueName, "iit-")) {
            return "iit-";
        } else if (StringUtils.contains(queueName, "irms_")) {
            return "irms_";
        } else if (StringUtils.contains(queueName, "labRefresh")) {
            return "labRefresh";
        } else if (StringUtils.contains(queueName, "master_")) {
            return "master_";
        } else if (StringUtils.contains(queueName, "mirs_")) {
            return "mirs_";
        } else if (StringUtils.contains(queueName, "ocr_")) {
            return "ocr_";
        } else if (StringUtils.contains(queueName, "null")) {
            return "null";
        } else if (StringUtils.contains(queueName, "LightQueue")) {
            return "LightQueue";
        } else if (StringUtils.contains(queueName, "WeightQueue")) {
            return "WeightQueue";
        } else if (StringUtils.contains(queueName, "report_edit_check")) {
            return "report_edit_check";
        } else if (StringUtils.contains(queueName, "saas")) {
            return "saas";
        } else if (StringUtils.contains(queueName, "sdtm_")) {
            return "sdtm_";
        } else if (StringUtils.contains(queueName, "social.")) {
            return "social.";
        } else if (StringUtils.contains(queueName, "statisticsJob")) {
            return "statisticsJob";
        } else if (StringUtils.contains(queueName, "subject")) {
            return "subject";
        } else if (StringUtils.contains(queueName, "tms_")) {
            return "tms_";
        } else if (StringUtils.contains(queueName, "blind_create")) {
            return "blind_create";
        } else if (StringUtils.startsWith(queueName, "et") || StringUtils.startsWith(queueName, "ev") || StringUtils.startsWith(queueName, "export")) {
            return "e开头没用得";
        } else if (StringUtils.startsWith(queueName, "file_") || StringUtils.startsWith(queueName, "form")) {
            return "f开头没用得";
        } else if (StringUtils.startsWith(queueName, "h")) {
            return "h开头没用得";
        } else if (StringUtils.startsWith(queueName, "i")) {
            return "i开头没用得";
        } else if (StringUtils.startsWith(queueName, "j")) {
            return "j开头没用得";
        } else if (StringUtils.startsWith(queueName, "l")) {
            return "l开头没用得";
        } else if (StringUtils.startsWith(queueName, "m")) {
            return "m开头没用得";
        } else if (StringUtils.startsWith(queueName, "partner")) {
            return "partner";
        } else if (StringUtils.startsWith(queueName, "patient")) {
            return "patient";
        } else if (StringUtils.startsWith(queueName, "pdf_")) {
            return "pdf_";
        } else if (StringUtils.startsWith(queueName, "person")) {
            return "person";
        } else if (StringUtils.startsWith(queueName, "pms.")) {
            return "pms.";
        } else if (StringUtils.startsWith(queueName, "publish")) {
            return "publish";
        } else if (StringUtils.startsWith(queueName, "tms")) {
            return "tms";
        } else if (StringUtils.startsWith(queueName, "tos")) {
            return "tos";
        } else if (StringUtils.startsWith(queueName, "q")) {
            return "q开头没用得";
        } else if (StringUtils.startsWith(queueName, "r") && !StringUtils.startsWith(queueName, "rpa_")) {
            return "r开头没用得";
        } else if (StringUtils.startsWith(queueName, "u")) {
            return "u开头没用得";
        } else if (StringUtils.startsWith(queueName, "v")) {
            return "v开头没用得";
        } else if (StringUtils.startsWith(queueName, "w")) {
            return "w开头没用得";
        } else if (StringUtils.startsWith(queueName, "x")) {
            return "x开头没用得";
        } else if (StringUtils.startsWith(queueName, "y")) {
            return "y开头没用得";
        } else if (StringUtils.startsWith(queueName, "z")) {
            return "z开头没用得";
        } else if (StringUtils.startsWith(queueName, "pv.queue.")) {
            return "4.0服务队列";
        } else if (StringUtils.startsWith(queueName, "t") && (!StringUtils.startsWith(queueName, "tm.") && !StringUtils.startsWith(queueName, "transport"))) {
            return "t开头没用得";
        } else if (StringUtils.startsWith(queueName, "san") || StringUtils.startsWith(queueName, "sas")
                || StringUtils.startsWith(queueName, "sc") || StringUtils.startsWith(queueName, "sd")
                || StringUtils.startsWith(queueName, "send") || StringUtils.startsWith(queueName, "sfe")
                || StringUtils.startsWith(queueName, "sh") || StringUtils.startsWith(queueName, "simi")
                || StringUtils.startsWith(queueName, "sms") || StringUtils.startsWith(queueName, "softium")
                || StringUtils.startsWith(queueName, "st") || StringUtils.startsWith(queueName, "sync")) {
            return "s开头没用得";
        } else {
            return "pv";
        }
    }
}
