package org.dromara.sms4j.example;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.comm.utils.SmsUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author handy
 */
@Slf4j
@SpringBootTest
public class SmsUtilTest {

    @Test
    public void getRandomString() {
        String randomString = SmsUtil.getRandomString();
        log.info(randomString);
        Assert.isTrue(randomString.length() == 6);
    }

    @Test
    public void testGetRandomString() {
        String randomString = SmsUtil.getRandomString(4);
        log.info(randomString);
        Assert.isTrue(randomString.length() == 4);
    }

    @Test
    public void getRandomInt() {
        String randomInt = SmsUtil.getRandomInt(4);
        log.info(randomInt);
        Assert.isTrue(randomInt.length() == 4);
    }

    @Test
    public void isEmpty() {
        Assert.isTrue(SmsUtil.isEmpty(""));
    }

    @Test
    public void isNotEmpty() {
        Assert.isTrue(SmsUtil.isNotEmpty("not"));
    }

    @Test
    public void jsonForObject() {
        AlibabaConfig alibabaConfig = SmsUtil.jsonForObject("{'templateName':'Test'}", AlibabaConfig.class);
        Assert.isTrue(alibabaConfig.getTemplateName().equals("Test"));
    }

    @Test
    public void copyBean() {
        AlibabaConfig alibabaConfig = SmsUtil.jsonForObject("{'templateName':'Test'}", AlibabaConfig.class);
        AlibabaConfig alibabaConfig1 = new AlibabaConfig();
        SmsUtil.copyBean(alibabaConfig, alibabaConfig1);
        Assert.isTrue(alibabaConfig1.getTemplateName().equals("Test"));
    }

    @Test
    public void getNewMap() {
        SmsUtil.getNewMap();
    }

    @Test
    public void listToString() {
        List<String> list = new ArrayList<>();
        list.add("12312341234");
        list.add("12312341235");
        String str = SmsUtil.listToString(list);
        log.info(str);
        Assert.isTrue(str.equals("12312341234,12312341235"));
    }

    @Test
    public void arrayToString() {
        List<String> list = new ArrayList<>();
        list.add("12312341234");
        list.add("12312341235");
        String str = SmsUtil.arrayToString(list);
        log.info(str);
        Assert.isTrue(str.equals("+8612312341234,+8612312341235"));
    }

    @Test
    public void listToArray() {
        List<String> list = new ArrayList<>();
        list.add("12312341234");
        list.add("12312341235");
        String[] str = SmsUtil.listToArray(list);
        Assert.isTrue(str[0].equals("+8612312341234") && str[1].equals("+8612312341235"));
    }

}
