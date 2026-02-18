plugins {
    java
}

group = "xyz.lokili"
version = "2.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to version)
    }
}

tasks.jar {
    archiveFileName.set("LoUtils-${version}.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}
