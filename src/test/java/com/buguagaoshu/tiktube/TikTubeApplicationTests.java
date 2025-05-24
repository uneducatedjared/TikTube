package com.buguagaoshu.tiktube;


import com.buguagaoshu.tiktube.cache.AIConfigCache;
import com.buguagaoshu.tiktube.cache.AdsCountRecorder;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.entity.AiConfigEntity;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import com.buguagaoshu.tiktube.repository.impl.FileRepositoryInOSS;
import com.buguagaoshu.tiktube.service.*;
import com.buguagaoshu.tiktube.utils.AesUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class TikTubeApplicationTests {

    @Autowired
    private AdsCountRecorder adsCountRecorder;

    @Autowired
    private RedisRepository redisRepository;

    @Test
    void contextLoads() {


    }
}
