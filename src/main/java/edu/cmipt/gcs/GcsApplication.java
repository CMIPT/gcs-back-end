package edu.cmipt.gcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class GcsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GcsApplication.class, args);
    }
}
