package org.dromara.sms4j.monyun.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.provider.config.BaseConfig;

/**
 * @author mine
 * @email thirteenthree@outlook.com
 * @date 2023/12/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MonYunConfig extends BaseConfig {

    /**
     * 请求地址
     */
    private String requestUrl = "http://api01.monyun.cn:7901";

    /**
     * 接口名称
     */
    private String action = "SendSms";
    @Override
    public String getSupplier() {
        return SupplierConstant.MONYUN;
    }
}
