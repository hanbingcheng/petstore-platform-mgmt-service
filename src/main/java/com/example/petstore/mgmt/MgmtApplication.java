package com.example.petstore.mgmt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.petstore.mgmt.mapper")
public class MgmtApplication {
  public static void main(String[] args) {
    SpringApplication.run(MgmtApplication.class, args);
  }
}
