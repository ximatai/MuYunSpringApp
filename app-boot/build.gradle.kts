plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":app-demo-web"))
    implementation(platform("net.ximatai.muyun.spring:muyun-spring-bom:${providers.gradleProperty("muyunSpringVersion").get()}"))
    implementation("net.ximatai.muyun.spring:muyun-spring-boot-starter")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
