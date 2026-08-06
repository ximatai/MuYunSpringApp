package net.ximatai.muyun.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Application host: it assembles modules but contains no business domain implementation. */
@SpringBootApplication
public class MuYunSpringAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MuYunSpringAppApplication.class, args);
    }
}
