plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.0"
    id("io.freefair.lombok") version "8.10.2"
}

group = "cn.aetheris.yuki"
version = "26.7.1"
description = "An AntiCheat Plugin | Powered By Aetheris"

var devBuild = true

if (devBuild) {
    println("WARNING: Using dev build, don't leak it")
    println("WARNING: $description")
    println("WARNING: devBuild: $devBuild")
    println("WARNING: ver: $version")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public") // 阿里云Maven仓库（优先，包含 Maven Central 镜像）
    // 特殊包优先的仓库
    maven("https://nexus.scarsz.me/content/repositories/releases") // Configuralize仓库 (github.scarsz:configuralize)
    maven("https://jitpack.io/") // JitPack仓库 (fr.mrmicky:FastInv 等)
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://papermc.io/repo/repository/maven-releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot仓库
    maven("https://repo.viaversion.com") // ViaVersion仓库
    maven("https://mvn.lumine.io/repository/maven-public/") // Lumine仓库
    maven("https://repo.aikar.co/content/groups/aikar/") // ACF仓库
    maven("https://repo.opencollab.dev/maven-releases/") // Floodgate（正式版）仓库
    maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate（快照版）仓库
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.booky.dev/releases/") { content { includeGroup("net.kyori") } }
    maven("https://repo.convallyria.com/snapshots")
    mavenCentral() // Maven中央仓库 (放在最后，避免 429 影响前面的仓库解析)
}

dependencies {
    // 运行时需要的库（打包进 shadowJar，使构建产物自包含）
    implementation(files("packetevents-spigot-2.13.0.jar"))
    implementation("fr.mrmicky:FastInv:3.1.1")
    implementation("com.alibaba:QLExpress:3.3.4")
    implementation("net.kyori:adventure-text-serializer-plain:4.23.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.23.0")
    implementation("net.kyori:adventure-api:4.23.0")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("it.unimi.dsi:fastutil:8.5.15")
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
    implementation("org.yaml:snakeyaml:2.4")
    implementation("github.scarsz:configuralize:1.4.1") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }

    // 数据库驱动按需打包：默认仅打包 SQLite（配置默认值）
    // 通过 -PdbDrivers=mongodb,h2 等指定需要打包的驱动
    val dbDrivers = (project.findProperty("dbDrivers") as String? ?: "sqlite")
        .split(",").map { it.trim().lowercase() }.toSet()
    compileOnly("org.xerial:sqlite-jdbc:3.50.2.0")
    compileOnly("com.mysql:mysql-connector-j:9.1.0")
    compileOnly("com.h2database:h2:2.3.232")
    compileOnly("org.mongodb:mongodb-driver-sync:5.3.0-beta0")
    if ("sqlite" in dbDrivers) implementation("org.xerial:sqlite-jdbc:3.50.2.0")
    if ("mysql" in dbDrivers || "mariadb" in dbDrivers) implementation("com.mysql:mysql-connector-j:9.1.0")
    if ("h2" in dbDrivers) implementation("com.h2database:h2:2.3.232")
    if ("mongodb" in dbDrivers) implementation("org.mongodb:mongodb-driver-sync:5.3.0-beta0")

    // 服务器提供或可选 hook（运行时由服务器 classpath 提供）
    compileOnly("com.google.guava:guava:33.4.8-jre")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2") // javax.annotation.Nullable
    compileOnly("org.slf4j:slf4j-api:2.0.17") // 服务器提供 SLF4J API + provider，不打包进 jar 避免冲突
    compileOnly("net.md-5:bungeecord-api:1.21-R0.3")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.85.Final")
    compileOnly(files("ViaVersion-5.10.1-SNAPSHOT.jar"))
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Manifold 编译期处理器
    compileOnly("systems.manifold:manifold-ext-rt:2025.1.24")
    compileOnly("systems.manifold:manifold-collections:2025.1.24")
    compileOnly("systems.manifold:manifold-params-rt:2025.1.24")
    compileOnly("systems.manifold:manifold-rt:2025.1.24")
    annotationProcessor("systems.manifold:manifold-ext:2025.1.24")
    annotationProcessor("systems.manifold:manifold-strings:2025.1.24")
    annotationProcessor("systems.manifold:manifold-params:2025.1.24")
    annotationProcessor("systems.manifold:manifold-collections:2025.1.24")
    annotationProcessor("systems.manifold:manifold:2025.1.24")
    testAnnotationProcessor("systems.manifold:manifold-ext:2025.1.24")
    testAnnotationProcessor("systems.manifold:manifold:2025.1.24")
    testAnnotationProcessor("systems.manifold:manifold-params:2025.1.24")
    testAnnotationProcessor("systems.manifold:manifold-strings:2025.1.24")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isFork = true
    options.isIncremental = true
    options.compilerArgs.addAll(
        listOf(
            "--add-modules", "jdk.incubator.vector"
        )
    )
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

// Prevent jar task from producing same filename as shadowJar (avoids Gradle implicit-dependency validation error)
tasks.jar {
    archiveClassifier.set("plain")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing.publications.create<MavenPublication>("maven") {
    artifact(tasks["shadowJar"])
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    archiveFileName.set("Yuki-${project.version}.jar")
    relocate("io.github.retrooper.packetevents", "cn.aetheris.libs.io.github.retrooper.packetevents")
    relocate("com.github.retrooper.packetevents", "cn.aetheris.libs.com.github.retrooper.packetevents")
    relocate("fr.mrmicky.fastinv", "cn.aetheris.libs.fr.mrmicky.fastinv")
    relocate("github.scarsz.configuralize", "cn.aetheris.libs.github.scarsz.configuralize")
    relocate("com.github.puregero", "cn.aetheris.libs.com.github.puregero")
    relocate("com.google.code.gson", "cn.aetheris.libs.com.google.code.gson")
    relocate("alexh", "cn.aetheris.libs.alexh")
    relocate("it.unimi.dsi.fastutil", "cn.aetheris.libs.it.unimi.dsi.fastutil")
    relocate("okio", "cn.aetheris.libs.okio")
    relocate("net.kyori", "cn.aetheris.libs.net.kyori")
    relocate("org.json", "cn.aetheris.libs.org.json")
    relocate("org.intellij", "cn.aetheris.libs.intellij")
    relocate("org.apache", "cn.aetheris.libs.org.apache")
    relocate("org.jetbrains", "cn.aetheris.libs.org.jetbrains")
    relocate("org.apache.commons.lang3", "cn.aetheris.libs.org.apache.commons.lang3")
    relocate("commons-lang", "cn.aetheris.libs.org.apache.commons-lang")
    relocate("com.zaxxer.hikari", "cn.aetheris.libs.com.zaxxer.hikari")
    relocate("assets.org.apache.commons.math3", "cn.aetheris.libs.assets.org.apache.commons.math3")
    relocate("org.apache.commons.math3", "cn.aetheris.libs.org.apache.commons.math3")
    relocate("org.bson", "cn.aetheris.libs.org.bson")
    relocate("com.mongodb", "cn.aetheris.libs.com.mongodb")
    relocate("org.yaml.snakeyaml", "cn.aetheris.libs.org.yaml.snakeyaml")
    relocate("javassist", "cn.aetheris.libs.javassist")
    relocate("com.j256.ormlite", "cn.aetheris.libs.com.j256.ormlite")
    relocate("javax.annotation", "cn.aetheris.libs.javax.annotation")
    relocate("com.zaxxer", "cn.aetheris.libs.com.zaxxer")
    relocate("com.mysql", "cn.aetheris.libs.com.mysql")
    relocate("org.h2", "cn.aetheris.libs.org.h2")
    relocate("org.xerial", "cn.aetheris.libs.org.xerial")
    relocate("com.ql.util", "cn.aetheris.libs.com.ql.util")
    mergeServiceFiles()
    exclude("assets/mappings") // Exclude new PE mappings folder
    exclude("META-INF/**") // 排除签名和 MRJAR 版本化类文件，避免冲突
    exclude("org/slf4j/**") // 排除传递依赖打入的 slf4j-api，运行时由服务器提供完整 SLF4J（API + provider），避免 NOP 警告
}
