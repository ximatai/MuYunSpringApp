plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    implementation(platform("net.ximatai.muyun.spring:muyun-spring-bom:${providers.gradleProperty("muyunSpringVersion").get()}"))
    implementation("net.ximatai.muyun.spring:muyun-spring-boot-starter")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${providers.gradleProperty("springBootVersion").get()}"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
