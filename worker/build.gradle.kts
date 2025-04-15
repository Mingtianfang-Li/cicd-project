plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common")) // Shared models and utilities
    implementation(project(":backend"))

    // Spring Boot for Worker Service API
    implementation("org.springframework.boot:spring-boot-starter-web")

    // WebClient (for sending job status updates)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // JPA and Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.3")
    runtimeOnly("org.postgresql:postgresql:42.7.5")

    // RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Swagger dependencies
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0")


    // Use JUnit BOM (Bill of Materials) to ensure version compatibility
    testImplementation(platform("org.junit:junit-bom:5.12.0"))

    // Then specify JUnit dependencies without versions - they'll use versions from the BOM
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.platform:junit-platform-launcher")

    // ✅ Mockito (For Unit Testing & Mocks)
    testImplementation("org.mockito:mockito-core:5.16.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.16.0")


    // Spring Boot Testing (Ensure JUnit 5 is used)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    // Lombok for reducing boilerplate code (e.g., @Getter, @Setter, @RequiredArgsConstructor)
    implementation("org.projectlombok:lombok:1.18.30")

    // Lombok annotation processor
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    // Required for Kotlin projects
    compileOnly("org.projectlombok:lombok:1.18.36")
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

    //adding docker library
    implementation ("com.github.docker-java:docker-java:3.2.13")
    implementation ("com.github.docker-java:docker-java-transport-okhttp:3.2.13")

    // file DB MinIO
    implementation("io.minio:minio:8.5.7")

    // git
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


tasks.test {
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("edu.neu.cs6510.sp25.t1.worker.WorkerApp")
}
