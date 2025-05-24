package com.buguagaoshu.tiktube;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MyConfigProperties.class)
public class TikTubeApplication {

    public final static String VERSION = "v1.3.0 2025-05-17";

    public static void main(String[] args) {
        SpringApplication.run(TikTubeApplication.class, args);
    }

}

