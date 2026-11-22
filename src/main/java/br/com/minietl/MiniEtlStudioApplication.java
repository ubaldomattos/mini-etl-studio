package br.com.minietl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MiniEtlStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniEtlStudioApplication.class, args);
    }
}
