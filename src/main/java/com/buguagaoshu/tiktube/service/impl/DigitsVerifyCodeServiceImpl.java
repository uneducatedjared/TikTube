package com.buguagaoshu.tiktube.service.impl;


import com.buguagaoshu.tiktube.exception.VerifyFailedException;
import com.buguagaoshu.tiktube.repository.VerifyCodeRepository;
import com.buguagaoshu.tiktube.service.GenerateImageService;
import com.buguagaoshu.tiktube.service.SendMessageService;
import com.buguagaoshu.tiktube.service.VerifyCodeService;
import com.buguagaoshu.tiktube.utils.VerifyCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.security.SecureRandom;
import java.util.Objects;


@Slf4j
@Service
public class DigitsVerifyCodeServiceImpl implements VerifyCodeService {
    private final VerifyCodeRepository verifyCodeRepository;

    private final GenerateImageService generateImageService;

    private final SendMessageService sendMessageService;

    private final VerifyCodeUtil verifyCodeUtil;

    public static final long VERIFY_CODE_EXPIRE_TIMEOUT = 60000L;

    public static final long SEND_VERIFY_CODE_EXPIRE_TIMEOUT = 15L;

    public DigitsVerifyCodeServiceImpl(VerifyCodeRepository verifyCodeRepository,
                                       GenerateImageService generateImageService,
                                       SendMessageService sendMessageService,
                                       VerifyCodeUtil verifyCodeUtil) {
        this.verifyCodeRepository = verifyCodeRepository;
        this.generateImageService = generateImageService;
        this.sendMessageService = sendMessageService;
        this.verifyCodeUtil = verifyCodeUtil;
    }

    private static String randomDigitString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }

    /**
     * @param string 验证码
     * @param type 验证码类型
     *             S 手机或邮箱发送的验证码，有效时间长
     *             W web 页面的图形验证码，时间短
     * */
    private static String appendTimestamp(String string, String type) {
        return string + "#" + System.currentTimeMillis() + "#" + type;
    }

    @Override
    public void send(String key) {
        // 加长 S 类型验证码
        String verifyCode = randomDigitString(verifyCodeUtil.getLen() + 4);
        String verifyCodeWithTimestamp = appendTimestamp(verifyCode, "S");

        verifyCodeRepository.save(key, verifyCodeWithTimestamp, SEND_VERIFY_CODE_EXPIRE_TIMEOUT);
        sendMessageService.send(key, verifyCode);
    }

    @Override
    public void verify(String key, String code) {
        String lastVerifyCodeWithTimestamp = verifyCodeRepository.find(key);
        // 如果没有验证码，则随机生成一个
        if (lastVerifyCodeWithTimestamp == null) {
            lastVerifyCodeWithTimestamp = appendTimestamp(randomDigitString(verifyCodeUtil.getLen()), "W");
        }

        String[] lastVerifyCodeAndTimestamp = lastVerifyCodeWithTimestamp.split("#");
        String lastVerifyCode = lastVerifyCodeAndTimestamp[0];
        long timestamp = Long.parseLong(lastVerifyCodeAndTimestamp[1]);
        long expTime = VERIFY_CODE_EXPIRE_TIMEOUT;
        if ("S".equals(lastVerifyCodeAndTimestamp[2])) {
            expTime = VERIFY_CODE_EXPIRE_TIMEOUT * SEND_VERIFY_CODE_EXPIRE_TIMEOUT;
        }
        if (timestamp + expTime < System.currentTimeMillis()) {
            throw new VerifyFailedException("验证码已过期！");
        } else if (!Objects.equals(code, lastVerifyCode)) {
            log.info("验证码错误 key: {}, system code: {}", key, lastVerifyCodeWithTimestamp);
            log.info("验证码错误 key: {}, user input code: {}", key, code);
            throw new VerifyFailedException("验证码错误!");
        }
        verifyCodeRepository.remove(key);
    }

    @Override
    public Image image(String key) {
        String verifyCode = randomDigitString(verifyCodeUtil.getLen());
        String verifyCodeWithTimestamp = appendTimestamp(verifyCode, "W");
        Image image = generateImageService.imageWithDisturb(verifyCode);
        verifyCodeRepository.save(key, verifyCodeWithTimestamp); // 保存验证码到redis
        return image;
    }
}
