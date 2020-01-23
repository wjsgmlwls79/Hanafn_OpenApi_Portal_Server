package com.hanafn.openapi.portal.cmct;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class ApplicationStartup implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String redisIps = RedisCommunicater.exclusionIpRedisGet();

        InetAddress local;
        try {
            local = InetAddress.getLocalHost();
            String localIp = local.getHostAddress();

            if(localIp != null){
                log.debug("local ip : "+localIp);

                boolean ipdupCheck = ipdupCheck(redisIps, localIp);
                if(ipdupCheck == false){
                    String ip =ipSetting(redisIps, localIp);
                    log.debug("신규 등록 IP["+localIp+"]");
                    RedisCommunicater.exclusionIpRedisSet(ip);
                }else{
                    log.debug("이미 등록된 IP["+localIp+"]");
                }
            }else{
                log.debug("local ip : NULL");
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }


        //IP셋팅
    }

    public boolean ipdupCheck(String redisIps, String localIp) {

        if(redisIps == null){
            redisIps = "";
        }

        String[] ipList = StringUtils.split(redisIps, ",");

        if (ipList != null) {
            for (String ipInfo : ipList) {
                if (StringUtils.equals(ipInfo, localIp)) {
                    return true;
                }
            }
        }

        return false;
    }

    public String ipSetting(String redisIps, String localIp) {

        String sttingIp = "";

        if (redisIps == null) {
            redisIps = "";
        }

        String[] ipList = StringUtils.split(redisIps, ",");

        if(ipList.length == 0){
            sttingIp = localIp;
        }else{
            sttingIp = redisIps + ","+localIp;
        }

        return sttingIp;
    }
}