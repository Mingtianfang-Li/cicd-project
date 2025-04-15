# Gradle Dependency List
this document is to list all the dependencies used in the project and their versions.
* **Last Check**: 2025-03-05 by Yiwen

## Spring Boot Dependencies
```kotlin
implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")
implementation("org.springframework.boot:spring-boot-starter-validation:3.4.3")
implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.3")
implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.3")
implementation("org.springframework.boot:spring-boot-starter-webflux")
implementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
}
```

## OpenAPI(Swagger) Dependencies
```kotlin
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
```

## Database & JPA (PostgreSQL + Hibernate)
```kotlin
implementation("org.hibernate.orm:hibernate-core:6.6.9.Final")
runtimeOnly("org.postgresql:postgresql:42.7.5")
implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
```

## Logging
```kotlin
implementation("ch.qos.logback:logback-classic:1.5.17")
implementation("org.slf4j:slf4j-api:2.0.17")
```

## JSON Processing
```kotlin
implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.3")
implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.3")
```

## Junit
```kotlin
testImplementation(platform("org.junit:junit-bom:5.12.0"))
testImplementation("org.junit.jupiter:junit-jupiter")
testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
testImplementation("org.junit.jupiter:junit-jupiter-api")
testImplementation("org.junit.platform:junit-platform-launcher")
```

## Mockito
```kotlin
testImplementation("org.mockito:mockito-core:5.16.0")
testImplementation("org.mockito:mockito-junit-jupiter:5.16.0")
```

## CLI Tool
```kotlin
implementation("info.picocli:picocli:4.7.6")
annotationProcessor("info.picocli:picocli-codegen:4.7.6")
```

## ShadowJar
```kotlin
id("com.github.johnrengelman.shadow") version "8.1.1"
```


## Lombok
```kotlin
    implementation("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    testImplementation("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
```

## Plugins
```kotlin
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.7"
```

## HTTP Client

```kotlin
   // OkHttp for HTTP client operations
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

