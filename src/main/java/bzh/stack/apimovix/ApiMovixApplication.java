package bzh.stack.apimovix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import bzh.stack.apimovix.util.UrlUtil;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ApiMovixApplication {
    private static final Logger logger = LoggerFactory.getLogger(ApiMovixApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiMovixApplication.class, args);
        logger.info("Application start on : " + UrlUtil.getBaseUrl());
    }
} 