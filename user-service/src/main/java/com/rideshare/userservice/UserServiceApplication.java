package com.rideshare.userservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
@Slf4j
@EnableCaching
@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
