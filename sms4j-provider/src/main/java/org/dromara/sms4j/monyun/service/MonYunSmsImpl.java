package org.dromara.sms4j.monyun.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.comm.constant.Constant;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.comm.delayedTime.DelayedTime;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.dromara.sms4j.comm.utils.SmsUtils;
import org.dromara.sms4j.monyun.config.MonYunConfig;
import org.dromara.sms4j.provider.service.AbstractSmsBlend;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author mine
 * @email thirteenthree@outlook.com
 * @date 2023/12/15
 */
@Slf4j
public class MonYunSmsImpl  extends AbstractSmsBlend<MonYunConfig> {
    private int retry = 0;
    protected MonYunSmsImpl(MonYunConfig config, Executor pool, DelayedTime delayed) {
        super(config, pool, delayed);
    }

    protected MonYunSmsImpl(MonYunConfig config) {
        super(config);
    }

    @Override
    public String getSupplier() {
        return SupplierConstant.MONYUN;
    }

    @Override
    public SmsResponse sendMessage(String phone, String message) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Content", message);
        return sendMessage(phone, getConfig().getTemplateId(), map);
    }

    @Override
    public SmsResponse sendMessage(String phone, String templateId, LinkedHashMap<String, String> messages) {
        String messageStr = messages.get("Content");
        return singleSend(phone, messageStr);
    }

    private SmsResponse singleSend(String phone, String messageStr) {
        String requestUrl;
        try {
            requestUrl = getConfig().getRequestUrl();
            if (requestUrl == null || requestUrl.isEmpty()) {
                requestUrl = "api01.monyun.cn:7901";
            }
            if(!requestUrl.startsWith("http://")){
                requestUrl = "http://" + requestUrl;
            }
            requestUrl += "/sms/v2/std";
            if (phone.contains(",")) {
                requestUrl = requestUrl + "/batch_send";
            }else{
                requestUrl = requestUrl + "/single_send";
            }

        } catch (Exception e) {
            log.error("MonYun send message error", e);
            throw new SmsBlendException(e.getMessage());
        }
        log.debug("requestUrl {}", requestUrl);
        try {
            Map<String, String> headers = new LinkedHashMap<>(1);
            headers.put("Content-Type", Constant.APPLICATION_JSON_UTF8);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mobile", phone);
            body.put("content", URLUtil.encode(messageStr, Charset.forName(CharsetUtil.GBK)));
            body.put("apikey", getConfig().getAccessKeySecret());
            SmsResponse smsResponse = convertToResponse(http.postJson(requestUrl, headers, body));
            if(smsResponse.isSuccess() || retry == getConfig().getMaxRetries()){
                retry = 0;
                return smsResponse;
            }
            return requestRetry(phone, messageStr);
        }catch (SmsBlendException e){
            return requestRetry(phone, messageStr);
        }
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String message) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Content", message);
        return massTexting(phones, getConfig().getTemplateId(), map);
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String templateId, LinkedHashMap<String, String> messages) {
        String messageStr = JSONUtil.toJsonStr(messages);
        return singleSend(SmsUtils.arrayToString(phones), messageStr);
    }


    private SmsResponse requestRetry(String phone, String message) {
        http.safeSleep(getConfig().getRetryInterval());
        retry++;
        log.warn("短信第 {" + retry + "} 次重新发送");
        return singleSend(phone, message);
    }

    private SmsResponse convertToResponse(JSONObject resJson) {
        SmsResponse smsResponse = new SmsResponse();
        smsResponse.setSuccess("0".equals(resJson.getStr("result")));
        smsResponse.setData(resJson);
        smsResponse.setConfigId(getConfigId());
        return smsResponse;
    }

    public static void main(String[] args) {
        MonYunConfig monYunConfig = new MonYunConfig();
        monYunConfig.setAccessKeySecret(System.getenv("mon_api_key"));
        String phone = System.getenv("phone");
        MonYunSmsImpl monYunSms = new MonYunSmsImpl(monYunConfig, Executors.newFixedThreadPool(1), new DelayedTime());
        SmsResponse smsResponse = monYunSms.sendMessage(phone, "您的验证码为123456");
        log.info(smsResponse.toString());
    }
}
