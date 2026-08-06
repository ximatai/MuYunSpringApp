package net.ximatai.muyun.app.demo;

import net.ximatai.muyun.database.spring.boot.sql.annotation.EnableMuYunRepositories;
import org.springframework.context.annotation.Configuration;

/** Wires this business domain into the application's repository scan. */
@Configuration
@EnableMuYunRepositories(basePackages = "net.ximatai.muyun.app.demo")
public class TodoDemoConfiguration {
}
