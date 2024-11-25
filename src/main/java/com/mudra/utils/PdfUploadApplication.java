package com.mudra.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PdfUploadApplication {
    public static void main(String[] arg) {
        SpringApplication.run(PdfUploadApplication.class, arg);
        log.info("* * * | PdfUploadApplication is started | * * *");
    }
}
