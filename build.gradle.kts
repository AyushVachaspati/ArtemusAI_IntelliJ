fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
  id("java")
  alias(libs.plugins.kotlin) // Kotlin support
  alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
  alias(libs.plugins.changelog) // Gradle Changelog Plugin
  alias(libs.plugins.qodana) // Gradle Qodana Plugin
  id("com.google.protobuf") version "0.9.4"
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

dependencies {
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.3")

  implementation("io.grpc:grpc-netty-shaded:1.59.0")
  implementation("io.grpc:grpc-protobuf:1.59.0")
  implementation("io.grpc:grpc-stub:1.59.0")

  implementation("com.google.protobuf:protobuf-java:3.25.1")
  implementation("com.google.protobuf:protobuf-java-util:3.25.1")
  implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
  implementation("io.grpc:protoc-gen-grpc-kotlin:1.4.0")


}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.25.1"
  }

  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.59.0"
    }
    create("grpckt") {
      artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.0:jdk8@jar"
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