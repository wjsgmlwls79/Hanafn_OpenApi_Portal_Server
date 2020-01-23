package com.hanafn.openapi.portal.cmct;

import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.exception.BusinessException;
import com.hanafn.openapi.portal.views.dto.HfnEnum;
import com.hanafn.openapi.portal.views.vo.ApiPolicyVO;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

@Component
@Slf4j
public class RedisCommunicater {

    private static MessageSourceAccessor messageSource;
    private static String redisAccessControlKey;
    private static String redisCompanyCodeKey;
    private static String redisEncryptKey;
    private static String redisFileLmtCntKey;
    private static String redisFileCntKey;
    private static String redisFileSizeKey;
    private static String redisTimeLimitedKey;
    private static String redisTimeCntKey;
    private static String redisTxKey;
    private static String redisAppEchoResponseYNKey;    // APP 대응답 여부 설정 키
    private static String redisApiEchoResponseKey;      // API 대응답 값 설정 키
    private static RedisClient client;
    private static StatefulRedisConnection<String, String> sender;
    private static final Logger logger = LoggerFactory.getLogger(RedisCommunicater.class);

    @Autowired
    private Environment environment;

    @Autowired
    public RedisCommunicater(MessageSourceAccessor messageSource) {
        this.messageSource = messageSource;
    }

    @Value("${redis.accessControl.key}")    // gw|accessControl|
    public void setRedisAccessControlKey (String redisAccessControlKey) { this.redisAccessControlKey = redisAccessControlKey; }

    @Value("${redis.companyCode.key}")      // gw|companyCode|
    public void setRedisCompanyCodeKey (String redisCompanyCodeKey) {
        this.redisCompanyCodeKey = redisCompanyCodeKey;
    }

    @Value("${redis.encrypt.key}")          // gw|encryptKey|
    public void setRedisEncryptKey (String redisEncryptKey) {
        this.redisEncryptKey = redisEncryptKey;
    }

    @Value("${redis.fileCount.key}")        // gw|fileCountConnection|
    public void setRedisFileCntKey (String redisFileGwCntKey) { this.redisFileCntKey = redisFileGwCntKey; }

    @Value("${redis.fileLmtCount.key}")     // gw|fileLimitedCountConnection|
    public void setRedisFileLmtCntKey (String redisFileLmtCntKey) { this.redisFileLmtCntKey = redisFileLmtCntKey; }

    @Value("${redis.fileSize.key}")         // gw|limitedFileSize|
    public void setRedisFileSizeKey (String redisFileSizeKey) { this.redisFileSizeKey = redisFileSizeKey; }

    @Value("${redis.tmLimitedCnt.key}")     // gw|timeLimitedCountConnection|
    public void setRedisTimeLimitedKey (String redisTimeLimitedKey) { this.redisTimeLimitedKey = redisTimeLimitedKey; }

    @Value("${redis.timeCnt.key}")          // gw|timeCountConnection|
    public void setRedisTimeCntKey (String redisTimeCntKey) { this.redisTimeCntKey = redisTimeCntKey; }

    @Value("${redis.txRestriction.key}")    // gw|transactionRestriction|
    public void setRedisTxKey (String redisTxKey) { this.redisTxKey = redisTxKey; }

    @Value("${redis.app.echoResponseYN.key}")   // gw|isEchoResponse|
    public void setAppEchoResponseYNKey (String redisAppEchoResponseYNKey) { this.redisAppEchoResponseYNKey = redisAppEchoResponseYNKey; }

    @Value("${redis.api.echoResponse.key}")     // gw|echoResponse|
    public void setApiEchoResponseKey (String redisApiEchoResponseKey) { this.redisApiEchoResponseKey = redisApiEchoResponseKey; }

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public RedisClient redisClient() {

        ArrayList<String> sentinelHost = new ArrayList<String>();
        ArrayList<Integer> sentinelPort = new ArrayList<Integer>();

        String sentinelNode = environment.getProperty("spring.redis.sentinel.nodes");
        if (sentinelNode != null) {
            StringTokenizer firstToken = new StringTokenizer( sentinelNode, "," );
            for( int fcnt = 1; firstToken.hasMoreElements(); fcnt++ ){
                String firstStr = firstToken.nextToken();
                StringTokenizer secondToken = new StringTokenizer( firstStr, ":" );
                for( int scnt = 1; secondToken.hasMoreElements(); scnt++ ){
                    String secondStr = secondToken.nextToken();
                    if (scnt % 2 == 0) {
                        sentinelPort.add(Integer.parseInt(secondStr));
                    }else {
                        sentinelHost.add(secondStr);
                    }
                }
            }
            if (sentinelHost.size() >2) {
                RedisURI redisUri = RedisURI.Builder
                        .sentinel(sentinelHost.get(0), "openapi-master")
                        .withSentinel(sentinelHost.get(0))
                        .withSentinel(sentinelHost.get(1))
                        .build();
                client = RedisClient.create(redisUri);
            }else {
                client = RedisClient.create(RedisURI.Builder.redis(redisHost, redisPort).build());
            }

        } else {
            client = RedisClient.create(RedisURI.Builder.redis(redisHost, redisPort).build());
        }
        return client;
    }

    private static void init(){
        if(sender == null || !sender.isOpen()){
            sender = client.connect();
        }
    }

    /**
     * 이용기관 Redis Set와 연동한다.
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void useorgRedisSet(String entrCd, String hfnCd, String value) throws BusinessException {
        init();

        hfnCd = HfnEnum.resolve(hfnCd).getCode();
        logger.debug("[Redis ORG SET] hfnCd ["+hfnCd+"]");

        String url = entrCd;
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + hfnCd + "|" + url;
        logger.debug("[Redis ORG SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis ORG SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("useorgRedisSet error :" + key + " : " + value );
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis ORG SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis ORG SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis ORG SET] Get ["+get+"]");
    }

    /**
     * 이용기관 Redis Del와 연동한다.
     * @param entrCd
     * @param hfnCd
     * @throws BusinessException
     */
    public static void useorgRedisDel(String entrCd, String hfnCd) throws BusinessException {
        init();

        String url = entrCd;
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + hfnCd + "|" + url;
        logger.debug("[Redis ORG DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis ORG DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis ORG DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis ORG DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis ORG DEL] Get ["+get+"]");
    }

    /**
     * 클라이언트/이용기관 Redis Set와 연동한다.
     * @param clientId
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void clientUseorgRedisSet(String clientId, String value) throws BusinessException {
        init();

        String url = clientId;
        url = StringUtils.replace(url, "-", "_");
        String key = redisCompanyCodeKey + url;
        logger.debug("[Redis CLIENT-ORG SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis CLIENT-ORG SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("clientUseorgRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis CLIENT-ORG SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis CLIENT-ORG SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis CLIENT-ORG SET] Get ["+get+"]");
    }

    /**
     * 클라이언트/이용기관 Redis Del와 연동한다.
     * @param clientId
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void clientUseorgRedisDel(String clientId) throws BusinessException {
        init();

        String url = clientId;
        url = StringUtils.replace(url, "-", "_");
        String key = redisCompanyCodeKey + url;
        logger.debug("[Redis CLIENT-ORG DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis CLIENT-ORG DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis CLIENT-ORG DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis CLIENT-ORG DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis CLIENT-ORG DEL] Get ["+get+"]");
    }

    /**
     * Api등록 후 RedisSet와 연동한다.
     * @param url
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiRedisSet(String url, String value) throws BusinessException {
        init();

        String key = redisAccessControlKey + url;
        logger.debug("[Redis API SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis API SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("apiRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis API SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis API SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis API SET] Get ["+get+"]");
    }

    /**
     * Api삭제 후 RedisDel와 연동한다.
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */

    public static void apiRedisDel(String url) throws BusinessException {
        init();

        String key = redisAccessControlKey + url;
        logger.debug("[Redis API DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis API DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis API DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis API DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis API DEL] Get ["+get+"]");
    }

    /**
     * App Redis Set와 연동한다.
     * @param clientId
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void appRedisSet(String clientId, String value) throws BusinessException {
        init();

        String url = clientId;
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + url;
        logger.debug("[Redis Client SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis Client SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("appRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis Client SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis Client SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis Client SET] Get ["+get+"]");
    }

    /**
     * App Redis Del와 연동한다.
     * @param clientId
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void appRedisDel(String clientId) throws BusinessException {
        init();

        String url = clientId;
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + url;
        logger.debug("[Redis Client DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis Client DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis Client DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis Client DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis Client DEL] Get ["+get+"]");
    }

    /**
     * App API Redis Set와 연동한다.
     * @param clientId
     * @param url
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void appApiRedisSet(String clientId, String url, String value) throws BusinessException {
        init();

        url = clientId + "|"+ url;
        url = StringUtils.replace(url, "-", "_");

        String key = redisAccessControlKey +  url;
        logger.debug("[Redis APP-API SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis APP-API SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("appApiRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis APP-API SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis APP-API SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis APP-API SET] Get ["+get+"]");
    }

    /**
     * App API Redis Del와 연동한다.
     * @param clientId
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void appApiRedisDel(String clientId, String url) throws BusinessException {
        init();

        url = clientId + "|"+ url;
        url = StringUtils.replace(url, "-", "_");

        String key = redisAccessControlKey + url;
        logger.debug("[Redis APP-API DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis APP-API DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis APP-API DEL] Message["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis APP-API DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis APP-API DEL] Get ["+get+"]");
    }

    /**
     * App IP Redis Set와 연동한다.
     * @param clientId
     * @param ip
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void appIpRedisSet(String clientId, String ip, String value) throws BusinessException {
        init();

        String url = clientId + "|" + ip;
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + url;
        logger.debug("[Redis IP SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis IP SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("appIpRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis IP SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis IP SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis IP SET] Get ["+get+"]");
    }

    /**
     * App IP Redis Del와 연동한다.
     * @param clientId
     * @param ip
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void appIpRedisDel(String clientId, String ip) throws BusinessException {
        init();

        String url = clientId + "|" + ip;
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + url;
        logger.debug("[Redis IP DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis IP DEL] Result ["+result+"]");

        String message = "del$" + key;

        logger.debug("[Redis IP DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis IP DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis IP DEL] Get ["+get+"]");
    }

    /**
     * exclusionIp Redis Set와 연동한다.
     * @param value
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void exclusionIpRedisSet(String value) throws BusinessException {
        init();

        String url = "exclusionIp";
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + url;
        logger.debug("[Redis Exclusion IP SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis Exclusion IP SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("exclusionIpRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis Exclusion IP SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis Exclusion IP SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis Exclusion IP SET] Get ["+get+"]");
    }

    /**
     * exclusionIp Redis Set와 연동한다.
     * @return getResult
     * @throws BusinessException
     */
    public static String exclusionIpRedisGet() throws BusinessException {
        init();

        String url = "exclusionIp";
        url = StringUtils.replace(url, "-", "_");
        String key = redisAccessControlKey + url;
        logger.debug("[Redis Exclusion IP GET] Key ["+key+"]");

        String getResult = sender.sync().get(key);
        logger.debug("[Redis IP GET] Get ["+getResult+"]");

        return getResult;
    }

    /**
     * 기관코드 별 암호화 키 설정
     */
    public static void encKeyRedisSet(String entrCd, String encKey) throws BusinessException {
        init();

        String url = entrCd;
        url = StringUtils.replace(url, "-", "_");
        String key = redisEncryptKey + url;
        logger.debug("★redisEncryptUrl:"  + key);
        logger.debug("[Redis ENC_KEY SET] Key ["+key+"]");

        String result = sender.sync().set(key, encKey);
        logger.debug("[Redis ENC_KEY SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("encKeyRedisSet error : " + key + " : " + encKey);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + encKey;
        logger.debug("[Redis ENC_KEY SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis ENC_KEY SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis ENC_KEY SET] Get ["+get+"]");
    }

    /**
     * 기관코드 별 암호화 키 삭제
     */
    public static void encKeyRedisDel(String entrCd, String encKey) throws BusinessException {
        init();

        String url = entrCd;
        url = StringUtils.replace(url, "-", "_");
        String key = redisEncryptKey + url;
        logger.debug("[Redis ENC_KEY DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis ENC_KEY DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis ENC_KEY DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis ENC_KEY DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis ENC_KEY DEL] Get ["+get+"]");
    }

    /**
     * 파일 동시 접속 수 제한
     * @param url
     * @param value (Long)
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiFileCntRedisSet(String url, String value) throws BusinessException {
        init();

        String key = redisFileLmtCntKey + url;
        logger.debug("[Redis FILE COUNT SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis FILE COUNT SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("apiFileCntRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis FILE COUNT SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis FILE COUNT SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis FILE COUNT SET] Get ["+get+"]");
    }

    /**
     * 파일 동시 접속 수 제한 초기화 1
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiFileCntRedisDel(String url) throws BusinessException {
        init();

        String key = redisFileLmtCntKey + url;
        logger.debug("[Redis fileLimitedCountConnection DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis fileLimitedCountConnection DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis fileLimitedCountConnection DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis fileLimitedCountConnection DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis fileLimitedCountConnection DEL] Get ["+get+"]");

        apiFileGwCntRedisDel(url);
    }

    /**
     * 파일 동시 접속 수 제한 초기화 2
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiFileGwCntRedisDel(String url) throws BusinessException {
        init();

        String key = redisFileCntKey + url;
        logger.debug("[Redis fileCountConnection DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis fileCountConnection DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis fileCountConnection DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis fileCountConnection DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis fileCountConnection DEL] Get ["+get+"]");
    }

    /**
     * 파일 크기 제어
     * @param url
     * @param value (Long)
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiFileSizeRedisSet(String url, String value) throws BusinessException {
        init();

        String key = redisFileSizeKey + url;
        logger.debug("[Redis FILE SIZE SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis FILE SIZE SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("apiFileSizeRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis FILE SIZE SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis FILE SIZE SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis FILE SIZE SET] Get ["+get+"]");
    }

    /**
     * 파일 크기 제어 초기화
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiFileSizeRedisDel(String url) throws BusinessException {
        init();

        String key = redisFileSizeKey + url;
        logger.debug("[Redis FILE SIZE DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis FILE SIZE DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis FILE SIZE DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis FILE SIZE DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis FILE SIZE DEL] Get ["+get+"]");
    }

    /**
     * API 사용량(사용주기-분,초/할당량) 제어
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiTimeLimitedRedisSet(String url, String value) throws BusinessException {
        init();

        logger.debug("[Redis timeLimitedCountConnection SET] value ["+value+"]");

        String key = redisTimeLimitedKey + url;
        logger.debug("[Redis timeLimitedCountConnection SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis timeLimitedCountConnection SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("apiTimeLimitedRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis timeLimitedCountConnection SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis timeLimitedCountConnection SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis timeLimitedCountConnection SET] Get ["+get+"]");
    }

    /**
     * API 사용량(사용주기-분,초/할당량) 제어 초기화 1
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiTimeLimitedRedisDel(String url) throws BusinessException {
        init();

        String key = redisTimeLimitedKey + url;
        logger.debug("[Redis timeLimitedCountConnection DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis timeLimitedCountConnection DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis timeLimitedCountConnection DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis timeLimitedCountConnection DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis timeLimitedCountConnection DEL] Get ["+get+"]");

        apiTimeCntRedisDel(url);
    }

    /**
     * API 사용량(사용주기-분,초/할당량) 제어 초기화 2
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiTimeCntRedisDel(String url) throws BusinessException {
        init();

        String key = redisTimeCntKey + url;
        logger.debug("[Redis timeCountConnection DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis timeCountConnection DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis timeCountConnection DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis timeCountConnection DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis timeCountConnection DEL] Get ["+get+"]");
    }

    /**
     * 거래제한 (요일/시간)
     * @param url
     * @param value
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiTxRedisSet(String url, String value) throws BusinessException {
        init();

        logger.debug("[Redis transactionRestriction SET] value ["+value+"]");

        String key = redisTxKey + url;
        logger.debug("[Redis transactionRestriction SET] Key ["+key+"]");

        String result = sender.sync().set(key, value);
        logger.debug("[Redis transactionRestriction SET] Result ["+result+"]");

        if(!StringUtils.equals(result, "OK")){
            log.error("apiTxRedisSet error : " + key + " : " + value);
            throw new BusinessException("E097",messageSource.getMessage("E097"));
        }

        String message = "set$" + key + "$" + value;
        logger.debug("[Redis transactionRestriction SET] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis transactionRestriction SET] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis transactionRestriction SET] Get ["+get+"]");
    }

    /**
     * 거래제한 (요일/시간) 초기화
     * @param url
     * @return 응답 메시지
     * @throws BusinessException
     */
    public static void apiTxRedisDel (String url) throws BusinessException {
        init();

        String key = redisTxKey + url;
        logger.debug("[Redis transactionRestriction DEL] Key ["+key+"]");

        long result = sender.sync().del(key);
        logger.debug("[Redis transactionRestriction DEL] Result ["+result+"]");

        String message = "del$" + key;
        logger.debug("[Redis transactionRestriction DEL] Message ["+message+"]");

        Long publish = sender.sync().publish("channel_openapi_gateway", message);
        logger.debug("[Redis transactionRestriction DEL] Publish ["+publish+"]");

        String get = sender.sync().get(key);
        logger.debug("[Redis transactionRestriction DEL] Get ["+get+"]");
    }

    /**
     * 로그인 token 저장
     * @param token = token
     */
    public static void setToken(String token) throws BusinessException {
        init();

        String result = sender.sync().setex(token, 1800, "Y");
        logger.debug("[Redis token SET] Result ["+result+"]");

        String message = "set$" + token;
        logger.debug("[Redis token SET] Message ["+message+"]");

        String get = sender.sync().get(token);
        logger.debug("[Redis token SET] Get ["+get+"]");
    }

    /**
     * 로그아웃 token 삭제
     */
    public static void delToken(String token) throws BusinessException {
        init();

        if (token != null) {
            long result = sender.sync().del(token);
            logger.debug("[Redis token DEL] Result ["+result+"]");

            String message = "del$" + token;
            logger.debug("[Redis token DEL] Message ["+message+"]");

            String get = sender.sync().get(token);
            logger.debug("[Redis token DEL] Get ["+get+"]");
        }
    }

    /**
     * token 검증
     */
    public static void validToken(String tokenValue) throws BusinessException {

        /** Redis 연결 **/
        init();

        logger.debug("[Redis validToken] tokenValue ["+tokenValue+"]");
        String token = sender.sync().get(tokenValue);
        logger.debug("[Redis validToken] token ["+token+"]");

        if (token == null || "".equals(token)) {
            log.error("validToken error");
            throw new BusinessException("E029",messageSource.getMessage("E029", Locale.KOREA));
        }
    }

    /**
     *  APP 대응답 설정
     * @Params
     *    # String clientId = APP의 APP KEY
     *    # String clientId = 대응답 사용여부 (true/false)
     */
    public static void setAppEchoResponse(String appKey, String value) throws BusinessException {

        /** Redis 연결 **/
        init();

        /** Redis에 값 세팅 **/
        String key = redisAppEchoResponseYNKey + appKey;
        logger.debug("[Redis App Echo YN SET] Key [" + key + "]");


        String result = sender.sync().set(key, value);
        logger.debug("[Redis App Echo YN SET] Result [" + result + "]");

        if(!StringUtils.equals(result, "OK")) {
            log.error("setAppEchoResponse error : " + key + " : " + value);
            throw new BusinessException("E180",messageSource.getMessage("E180"));
        }

        /** Redis에서 Publish 하여 GW에게 알려줌 **/
        String msg = "set$" + key + "$" + value;
        logger.debug("[Redis App Echo YN SET] Message [" + msg + "]");

        Long publish = sender.sync().publish("channel_openapi_gateway", msg);
        logger.debug("[Redis App Echo YN SET] Publish [" + publish + "]");

        /** Redis에 세팅된 값 확인 **/
        String get = sender.sync().get(key);
        logger.debug("[Redis App Echo YN SET] Get [" + get + "]");
    }

    /**
     *  APP 대응답 설정 해제
     */
    public static void delAppEchoResponse(String appKey) throws BusinessException {

        /** Redis 연결 **/
        init();

        /** Redis에 값 세팅 **/
        String key = redisAppEchoResponseYNKey + appKey;
        logger.debug("[Redis App Echo YN DEL] Key [" + key + "]");

        Long result = sender.sync().del(key);
        logger.debug("[Redis App Echo YN DEL] Result [" + result + "]");

        /** Redis에서 Publish 하여 GW에게 알려줌 **/
        String msg = "del$" + key;
        logger.debug("[Redis App Echo YN DEL] Message [" + msg + "]");

        Long publish = sender.sync().publish("channel_openapi_gateway", msg);
        logger.debug("[Redis App Echo YN DEL] Publish [" + publish + "]");

        /** Redis에 세팅된 값 확인 **/
        String get = sender.sync().get(key);
        logger.debug("[Redis App Echo YN DEL] Get [" + get + "]");
    }

    /**
     *  API 대응답 값 설정
     * @Params
     *      # String url = 해당 API의 접속 URL
     *      # String value = 해당 API의 대응답 값
     */
    public static void setApiEchoResponse(String url, String value) throws BusinessException {

        /** Redis 연결 **/
        init();

        /** Redis에 값 세팅 **/
        String key = redisApiEchoResponseKey + url;
        logger.debug("[Redis API Echo Response SET] Key [" + key + "]");


        String result = sender.sync().set(key, value);
        logger.debug("[Redis API Echo Response SET] Result [" + result + "]");

        if(!StringUtils.equals(result, "OK")) {
            log.error("setApiEchoResponse error : " + key + " : " + value);
            throw new BusinessException("E181",messageSource.getMessage("E181"));
        }

        /** Redis에서 Publish 하여 GW에게 알려줌 **/
        String msg = "set$" + key + "$" + value;
        logger.debug("[Redis API Echo Response SET] Message [" + msg + "]");

        Long publish = sender.sync().publish("channel_openapi_gateway", msg);
        logger.debug("[Redis API Echo Response SET] Publish [" + publish + "]");

        /** Redis에 세팅된 값 확인 **/
        String get = sender.sync().get(key);
        logger.debug("[Redis API Echo Response SET] Get [" + get + "]");
    }

    /**
     *  API 대응답 값 설정
     * @Params
     *      # String url = 해당 API의 접속 URL
     *      # String value = 해당 API의 대응답 값
     */
    public static void setApiEchoResponse(String url, String searchKey, String searchValue, String value) throws BusinessException {

        /** Redis 연결 **/
        init();

        /** Redis에 값 세팅 **/
        String key = redisApiEchoResponseKey + url + "|" + searchKey + "|" + searchValue;
        logger.debug("[Redis API Echo Response SET] Key [" + key + "]");


        String result = sender.sync().set(key, value);
        logger.debug("[Redis API Echo Response SET] Result [" + result + "]");

        if(!StringUtils.equals(result, "OK")) {
            log.error("setApiEchoResponse error : " + key + " : " + value);
            throw new BusinessException("E181",messageSource.getMessage("E181"));
        }

        /** Redis에서 Publish 하여 GW에게 알려줌 **/
        String msg = "set$" + key + "$" + value;
        logger.debug("[Redis API Echo Response SET] Message [" + msg + "]");

        Long publish = sender.sync().publish("channel_openapi_gateway", msg);
        logger.debug("[Redis API Echo Response SET] Publish [" + publish + "]");

        /** Redis에 세팅된 값 확인 **/
        String get = sender.sync().get(key);
        logger.debug("[Redis API Echo Response SET] Get [" + get + "]");
    }

    /**
     *  API 대응답 값 해제
     */
    public static void delApiEchoResponse(String url) throws BusinessException {

        /** Redis 연결 **/
        init();

        /** Redis에 값 세팅 **/
        String key = redisApiEchoResponseKey + url;
        logger.debug("[Redis API Echo Response DEL] Key [" + key + "]");

        Long result = sender.sync().del(key);
        logger.debug("[Redis API Echo Response DEL] Result [" + result + "]");

        /** Redis에서 Publish 하여 GW에게 알려줌 **/
        String msg = "del$" + key;
        logger.debug("[Redis API Echo Response DEL] Message [" + msg + "]");

        Long publish = sender.sync().publish("channel_openapi_gateway", msg);
        logger.debug("[Redis API Echo Response DEL] Publish [" + publish + "]");

        /** Redis에 세팅된 값 확인 **/
        String get = sender.sync().get(key);
        logger.debug("[Redis API Echo Response DEL] Get [" + get + "]");
    }

    /**
     *  API 대응답 값 해제
     */
    public static void delApiEchoResponse(String url, String searchKey, String searchValue) throws BusinessException {

        /** Redis 연결 **/
        init();

        /** Redis에 값 세팅 **/
        String key = redisApiEchoResponseKey + url + "|" + searchKey + "|" + searchValue;
        logger.debug("[Redis API Echo Response DEL] Key [" + key + "]");

        Long result = sender.sync().del(key);
        logger.debug("[Redis API Echo Response DEL] Result [" + result + "]");

        /** Redis에서 Publish 하여 GW에게 알려줌 **/
        String msg = "del$" + key;
        logger.debug("[Redis API Echo Response DEL] Message [" + msg + "]");

        Long publish = sender.sync().publish("channel_openapi_gateway", msg);
        logger.debug("[Redis API Echo Response DEL] Publish [" + publish + "]");

        /** Redis에 세팅된 값 확인 **/
        String get = sender.sync().get(key);
        logger.debug("[Redis API Echo Response DEL] Get [" + get + "]");
    }
}