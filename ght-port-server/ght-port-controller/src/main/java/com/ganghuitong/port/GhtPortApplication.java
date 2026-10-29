package com.ganghuitong.port;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 港慧通港口物流管理系统启动类
 *
 * @author yangxueyong
 */
@SpringBootApplication(scanBasePackages = "com.ganghuitong.port")
public class GhtPortApplication {

    /**
     * 应用入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GhtPortApplication.class, args);
    }
}
