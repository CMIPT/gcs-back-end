package edu.cmipt.gcs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("edu.cmipt.gcs.dao")
@EnableTransactionManagement
public class GcsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GcsApplication.class, args);
    }
}
