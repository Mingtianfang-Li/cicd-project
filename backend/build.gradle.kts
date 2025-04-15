plugins {
    java
    application
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common")) // Shared code

    // Spring Boot Core Dependencies
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.3")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.3")

    // OpenAPI (Swagger) for API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // Database and JPA (PostgreSQL + Hibernate)
    //implementation("org.hibernate.orm:hibernate-core:6.6.9.Final")
    runtimeOnly("org.postgresql:postgresql:42.7.5")

    // REMOVED: rabbitMQ dependency as we now use in-memory queues
    // implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Jakarta Persistence (JPA Annotations)
    //implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.17")

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

    // ✅ Spring Boot Test (For MockMvc & Integration Tests)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    implementation("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testImplementation("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

    //rmq
    implementation ("org.springframework.boot:spring-boot-starter-amqp")

    //fileutil
    implementation ("commons-io:commons-io:2.15.1")

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
}

application {
    mainClass.set("edu.neu.cs6510.sp25.t1.backend.BackendApp")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "edu.neu.cs6510.sp25.t1.BackendApp"
    }
}