package com.rongai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.rongai.mapper")
public class RongAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RongAiApplication.class, args);
    }
}
