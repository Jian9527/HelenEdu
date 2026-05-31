package com.helen.eduedu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.helen.eduedu.mapper")
public class HleneEduApplication {

    public static void main(String[] args) {
        SpringApplication.run(HleneEduApplication.class, args);
    }
}
