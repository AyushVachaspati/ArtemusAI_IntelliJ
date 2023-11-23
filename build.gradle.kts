import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
  id("java")
  alias(libs.plugins.kotlin) // Kotlin support
  alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
  alias(libs.plugins.changelog) // Gradle Changelog Plugin
  alias(libs.plugins.qodana) // Gradle Qodana Plugin
  id("com.google.protobuf") version "0.9.4"
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain(17)
}

intellij {
  pluginName = properties("pluginName")
  version = properties("platformVersion")
  type = properties("platformType")

  plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}


val grpcVersion = "1.59.0"
val protobufVersion = "3.25.1"
val kotlinGrpcVersion = "1.4.0"


dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.3")

  implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
  implementation("io.grpc:grpc-protobuf:$grpcVersion")
  implementation("io.grpc:grpc-stub:$grpcVersion")
  implementation("io.grpc:grpc-kotlin-stub:$kotlinGrpcVersion")

  implementation("com.google.protobuf:protobuf-java:$protobufVersion")
  implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
  implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
  implementation("io.grpc:protoc-gen-grpc-kotlin:$kotlinGrpcVersion")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:$protobufVersion"
  }

  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
    }
    create("grpckt") {
      artifact = "io.grpc:protoc-gen-grpc-kotlin:$kotlinGrpcVersion:jdk8@jar"
    }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        create("grpc")
        create("grpckt")
      }
      it.builtins {
        create("kotlin")
      }
    }
  }
}

tasks {
  wrapper {
    gradleVersion = properties("gradleVersion").get()
  }

  patchPluginXml {
    version = properties("pluginVersion")
    sinceBuild = properties("pluginSinceBuild")
    untilBuild = properties("pluginUntilBuild")
  }

  signPlugin {
    certificateChain = environment("CERTIFICATE_CHAIN")
    privateKey = environment("PRIVATE_KEY")
    password = environment("PRIVATE_KEY_PASSWORD")
  }

  publishPlugin {
    token = environment("PUBLISH_TOKEN")
    channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
  }
}

// Shadow all dependencies. Use relocate to shadow specific ones.
tasks {
  shadowJar{
    archiveBaseName = properties("pluginName").get()
    version = properties("pluginVersion").get()
    isEnableRelocation = true
    relocationPrefix = "artemus"
    mergeServiceFiles()

  }
}

tasks{
  buildPlugin{
    dependsOn(shadowJar)
  }
}

// set shadow jar to be used in runIde
tasks{
  prepareSandbox{
    pluginJar = shadowJar.get().archiveFile
    dependsOn(shadowJar)
  }
}
