package com.pearadmin.common.tools.sms;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * 创建日期：2026-04-10
 * 短信发送工具类 (双层鉴权：Header网关校验 + Body业务签名)
 **/
@Slf4j
@Component
public class SmsUtil {

    private static String APP_KEY;
    private static String SECRET;
    private static String SYS_TYPE;
    private static String SMS_API_URL;

    // 新增：网关专用请求头参数
    private static String X_APP_ID;
    private static String X_APP_KEY;

    @Value("${sms.app-key}")
    public void setAppKey(String appKey) { APP_KEY = appKey; }

    @Value("${sms.secret}")
    public void setSecret(String secret) { SECRET = secret; }

    @Value("${sms.sys-type}")
    public void setSysType(String sysType) { SYS_TYPE = sysType; }

    @Value("${sms.api-url}")
    public void setSmsApiUrl(String smsApiUrl) { SMS_API_URL = smsApiUrl; }

    @Value("${sms.x-app-id}")
    public void setXAppId(String xAppId) { X_APP_ID = xAppId; }

    @Value("${sms.x-app-key}")
    public void setXAppKey(String xAppKey) { X_APP_KEY = xAppKey; }

    /**
     * 下发短信接口
     * @param phoneNumber 目标手机号
     * @param content     短信内容
     * @return 接口返回的 JSON 字符串，调用异常则返回 null
     */
    public static String sendSms(String phoneNumber, String content) {
        try {
            // 1. 组装内层业务参数 (Body)
            Map<String, String> map = new TreeMap<>();
            map.put("method", "api.send");
            map.put("app_key", APP_KEY);
            map.put("timestamp", DateUtil.now());
            map.put("requestId", DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(8));
            map.put("phoneNumber", phoneNumber);
            map.put("destId", "10001");
            map.put("content", content);
            map.put("lanType", "0");
            map.put("sysType", SYS_TYPE);
            map.put("v", "1.0");

            // 2. 生成 MD5 签名
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                buf.append(entry.getKey()).append(entry.getValue());
            }
            String signStr = SECRET + buf + SECRET;
            String sign = SecureUtil.md5(signStr).toUpperCase();
            map.put("sign", sign);

            log.info("========== 当前请求短信网关URL: {} ==========", SMS_API_URL);
            log.info("短信请求参数(Body): {}", JSONObject.toJSONString(map));

            // 3. 发送 HTTP 请求，携带外层网关 Header 鉴权
            String result = HttpRequest.post(SMS_API_URL)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("X-APP-ID", X_APP_ID)   // 网关凭证ID
                    .header("X-APP-KEY", X_APP_KEY) // 网关凭证KEY
                    .body(JSONObject.toJSONString(map))
                    .timeout(15000) // 15 秒超时时间
                    .execute()
                    .body();

            log.info("下发短信响应结果: {}", result);
            return result;

        } catch (Exception e) {
            log.error("下发短信接口调用异常: ", e);
            return null;
        }
    }
}