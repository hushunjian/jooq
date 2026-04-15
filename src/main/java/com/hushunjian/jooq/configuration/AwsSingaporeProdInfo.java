package com.hushunjian.jooq.configuration;

public class AwsSingaporeProdInfo {

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
}
