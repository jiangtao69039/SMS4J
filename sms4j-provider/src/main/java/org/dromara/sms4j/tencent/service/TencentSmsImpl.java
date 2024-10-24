package org.dromara.sms4j.tencent.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.comm.constant.Constant;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.comm.delayedTime.DelayedTime;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.dromara.sms4j.comm.utils.SmsUtils;
import org.dromara.sms4j.provider.service.AbstractSmsBlend;
import org.dromara.sms4j.tencent.config.TencentConfig;
import org.dromara.sms4j.tencent.utils.TencentUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author wind
 */
@Slf4j
public class TencentSmsImpl extends AbstractSmsBlend<TencentConfig> {

    private int retry = 0;

    public TencentSmsImpl(TencentConfig tencentSmsConfig, Executor pool, DelayedTime delayed) {
        super(tencentSmsConfig, pool, delayed);
    }

    public TencentSmsImpl(TencentConfig tencentSmsConfig) {
        super(tencentSmsConfig);
    }

    @Override
    public String getSupplier() {
        return SupplierConstant.TENCENT;
    }

    @Override
    public SmsResponse sendMessage(String phone, String message) {
        String[] split = message.split("&");
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < split.length; i++) {
            map.put(String.valueOf(i), split[i]);
        }
        return sendMessage(phone, getConfig().getTemplateId(), map);
    }

    @Override
    public SmsResponse sendMessage(String phone, String templateId, LinkedHashMap<String, String> messages) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            list.add(entry.getValue());
        }
        String[] s = new String[list.size()];
        return getSmsResponse(new String[]{StrUtil.addPrefixIfNot(phone, "+86")}, list.toArray(s), templateId);
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String message) {
        String[] split = message.split("&");
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < split.length; i++) {
            map.put(String.valueOf(i), split[i]);
        }
        return massTexting(phones, getConfig().getTemplateId(), map);
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String templateId, LinkedHashMap<String, String> messages) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            list.add(entry.getValue());
        }
        String[] s = new String[list.size()];
        return getSmsResponse(SmsUtils.listToArray(phones), list.toArray(s), templateId);
    }

    private SmsResponse getSmsResponse(String[] phones, String[] messages, String templateId) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature;
        try {
            signature = TencentUtils.generateSignature(this.getConfig(), templateId, messages, phones, timestamp);
        } catch (Exception e) {
            log.error("tencent send message error", e);
            throw new SmsBlendException(e.getMessage());
        }
        Map<String, String> headsMap = TencentUtils.generateHeadsMap(signature, timestamp, getConfig().getAction(),
                getConfig().getVersion(), getConfig().getTerritory(), getConfig().getRequestUrl());
        Map<String, Object> requestBody = TencentUtils.generateRequestBody(phones, getConfig().getSdkAppId(),
                getConfig().getSignature(), templateId, messages);
        String url = Constant.HTTPS_PREFIX + getConfig().getRequestUrl();

        SmsResponse smsResponse = new SmsResponse();
        smsResponse.setConfigId(getConfigId());
        while(retry <= getConfig().getMaxRetries()) {
            try {
                return getResponse(http.postJson(url, headsMap, requestBody));
            } catch (SmsBlendException e) {
                log.error("发送请求时发生异常:"+e.getMessage());
                if (retry == getConfig().getMaxRetries()) {
                    retry = 0;
                    return smsResponse;
                }
                requestRetry(phones, messages, templateId);
            }
        }
        return smsResponse;
    }

    private void requestRetry(String[] phones, String[] messages, String templateId) {
        http.safeSleep(getConfig().getRetryInterval());
        retry++;
        log.warn("短信第 {" + retry + "} 次重新发送");
//        return getSmsResponse(phones, messages, templateId);
    }

    private SmsResponse getResponse(JSONObject resJson) {
        SmsResponse smsResponse = new SmsResponse();
        JSONObject response = resJson.getJSONObject("Response");
        // 根据 Error 判断是否配置错误
        String error = response.getStr("Error");
        smsResponse.setSuccess(StrUtil.isBlank(error));
        // 根据 SendStatusSet 判断是否不为Ok
        JSONArray sendStatusSet = response.getJSONArray("SendStatusSet");
        if (sendStatusSet != null) {
            boolean success = true;
            for (Object obj : sendStatusSet) {
                JSONObject jsonObject = (JSONObject) obj;
                String code = jsonObject.getStr("Code");
                if (!"Ok".equals(code)) {
                    success = false;
                    break;
                }
            }
            smsResponse.setSuccess(success);
        }
        smsResponse.setData(resJson);
        smsResponse.setConfigId(getConfigId());
        return smsResponse;
    }

}