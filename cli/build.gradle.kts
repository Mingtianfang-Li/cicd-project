plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    // Picocli for CLI
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")

    // Use JUnit BOM (Bill of Materials) to ensure version compatibility
    testImplementation(platform("org.junit:junit-bom:5.12.0"))

    // Then specify JUnit dependencies without versions - they'll use versions from the BOM
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.platform:junit-platform-launcher")

    // Mockito dependencies
    testImplementation("org.mockito:mockito-core:5.16.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.16.0")

    testImplementation("org.mockito:mockito-inline:5.2.0")

    // OkHttp for HTTP client operations
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jackson for JSON and YAML processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.3")

    // SLF4J and Logback for logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.17")

    implementation(project(":common"))
    implementation(project(":backend"))

    implementation("org.springframework.boot:spring-boot-starter-amqp:2.7.14")

    // git
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")

    // docker
    implementation ("com.github.docker-java:docker-java:3.2.13")
    implementation ("com.github.docker-java:docker-java-transport-okhttp:3.2.13")

    // k8s
    implementation ("io.kubernetes:client-java:18.0.0")
}

application {
    mainClass.set("edu.neu.cs6510.sp25.t1.cli.CliApp")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("ci-tool")
    archiveClassifier.set("")
    archiveVersion.set("")

    isZip64 = true

    manifest {
        attributes("Main-Class" to "edu.neu.cs6510.sp25.t1.cli.CliApp")
    }
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

