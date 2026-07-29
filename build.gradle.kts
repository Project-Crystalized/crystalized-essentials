plugins {
    id("java")
    //Copied this from my test plugin, this allows to run server in IDEA
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "gg.crystalized.essentials"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
tasks {
    runServer {
        //I just copied pasted that from my plugin to make run server work so that I can test it in IDEA
        //Default comments that come with setting up the project with a plugin
        /*
            // Configure the Minecraft version for our task.
            // This is the only required configuration besides applying the plugin.
            // Your plugin's jar (or shadowJar if present) will be used automatically.

         */
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
