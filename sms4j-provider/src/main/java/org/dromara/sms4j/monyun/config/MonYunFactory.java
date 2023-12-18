package org.dromara.sms4j.monyun.config;

import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.monyun.service.MonYunSmsImpl;
import org.dromara.sms4j.provider.factory.AbstractProviderFactory;

/**
 * @author mine
 * @email thirteenthree@outlook.com
 * @date 2023/12/15
 */
public class MonYunFactory extends AbstractProviderFactory<MonYunSmsImpl,MonYunConfig> {
    private static final MonYunFactory INSTANCE = new MonYunFactory();
    /**
     * 获取建造者实例
     * @return 建造者实例
     */
    public static MonYunFactory instance() {
        return INSTANCE;
    }
    @Override
    public MonYunSmsImpl createSms(MonYunConfig monYunConfig) {
        return new MonYunSmsImpl(monYunConfig);
    }

    @Override
    public String getSupplier() {
        return SupplierConstant.MONYUN;
    }
}
