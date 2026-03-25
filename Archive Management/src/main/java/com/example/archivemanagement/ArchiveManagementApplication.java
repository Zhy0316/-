package com.example.archivemanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.archivemanagement.mapper")
public class ArchiveManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchiveManagementApplication.class, args);
    }

}
