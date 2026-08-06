plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

allprojects {
    group = "net.ximatai.muyun.app"
    version = providers.gradleProperty("appVersion").get()

    repositories {
        providers.gradleProperty("muyunRepository").orNull?.let { frameworkRepository ->
            maven { url = uri(frameworkRepository) }
        }
        mavenCentral()
    }
}

subprojects {
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
