package com.hushunjian.jooq.configuration;

public class AwsUatInfo {

    private static final String targetFileName = "aws-uat";

    private static final String namespace = "aws_uat";

    private static final String redis_host = "uat-aws-itaimei-redis.taimei.com";

    private static final String redis_password = "SpamDyogM1MnqXlM0bJ8";

    /**
     * 美西UAT不需要
     * 新加坡生产需要
     */
    private static final String redis_url = "";
    //private static final String redis_url = "rediss://fs3fasklfs8343dfsafd@master.uat-itaimei-redis.pyoiqi.usw1.cache.amazonaws.com:6379";

    /**
     * 美西UAT需要服务后缀
     * 新加坡生产不需要
     */
    private static final Boolean removeNameSpace = false;
    //private static final Boolean removeNameSpace = true;

    private static final String mongo_url = "mongodb://uatuser:uatuserTM@uat-itaimei-db-edcmongo01.taimei.com:20001,uat-itaimei-db-edcmongo02.taimei.com:20001,uat-itaimei-db-edcmongo03.taimei.com:20001";

    private static final String mongo_rs_info = "replicaSet=itaimei";

    private static final String rabbit_host = "uat-itaimei-mw-mq.taimei.com";

    private static final String rabbit_username = "pv";

    private static final String rabbit_password = "PV_adminuser@123";

    private static final String v4_datasource_username = "usr_pv";

    private static final String v4_datasource_password = "Qmo__^^123OxTrew";

    private static final String v5_datasource_username = "usr_pvcaps";

    private static final String v5_datasource_password = "CpOuuY12__^xPo12";

    private static final String zkAddress = "uat-itaimei-mw-zk01.taimei.com:2181,uat-itaimei-mw-zk02.taimei.com:2181,uat-itaimei-mw-zk03.taimei.com:2181";

    private static final String elasticsearch_uris = "uat-itaimei-mw-es7.taimei.com:80";

    private static final String kafka_bootstrap_servers = "10.132.169.210:9092,10.132.174.60:9092,10.132.172.164:9092";

    private static final String data_source_url = "uat-itaimei-db-tenentmysql.taimei.com:3308";

    private static final String topic_prefix = "UAT__";

    private static final String oppugnBaseUrl = "https://global-uat.trialos.com/";

    private static final String as2MdnUrl = "https://global-uat-edi.trialos.com/as2/mdn";

    private static final String env_prefix = "global-uat.trialos.com";
}
