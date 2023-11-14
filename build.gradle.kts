fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
  id("java")
  alias(libs.plugins.kotlin) // Kotlin support
  alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
  alias(libs.plugins.changelog) // Gradle Changelog Plugin
  alias(libs.plugins.qodana) // Gradle Qodana Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
  mavenCentral()
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
  jvmToolchain(11)
}

intellij {
  pluginName = properties("pluginName")
  version = properties("platformVersion")
  type = properties("platformType")

  plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.3")
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
    // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
    channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
  }
}
