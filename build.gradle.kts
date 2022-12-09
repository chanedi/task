plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.4.0"
}

group = "com.lufax"
version = "1.1.1.2021.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.alibaba:fastjson:1.2.83")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.1")
//    version.set("2019.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.tasks", "Git4Idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }

    patchPluginXml {
        sinceBuild.set("211")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
