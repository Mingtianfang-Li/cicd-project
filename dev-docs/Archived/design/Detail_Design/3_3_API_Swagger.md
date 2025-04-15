# ✅ Step-by-Step Guide for Generating Swagger/OpenAPI Docs
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## 1⃣ Add Swagger/OpenAPI Dependencies to Gradle

### 🔹 Modify `backend/build.gradle`:
```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2' // Latest version
}
```

### 🔹 For Gradle Kotlin DSL (`build.gradle.kts`)
```kotlin
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
}
```

---

## 2⃣ Enable Swagger UI in `backend/src/main/java/config/OpenAPIConfig.java`
Create a new configuration class to define API metadata.

### 📈 `OpenAPIConfig.java`
```java
package edu.neu.cs6510.sp25.t1.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CI/CD System API")
                .version("1.0")
                .description("API Documentation for CI/CD system - pipelines, stages, jobs, and executions.")
            );
    }
}
```

---

## 3⃣ Annotate Your API Controllers
For each REST API endpoint, add Swagger annotations.

### 📈 Example: `PipelineController.java`
```java
package edu.neu.cs6510.sp25.t1.backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pipeline")
@Tag(name = "Pipeline API", description = "Endpoints for managing pipeline executions")
public class PipelineController {

    @PostMapping("/execute")
    @Operation(summary = "Trigger a pipeline execution", description = "Starts a new pipeline execution based on the provided request.")
    public PipelineExecutionResponse executePipeline(@RequestBody PipelineExecutionRequest request) {
        // Implementation
        return new PipelineExecutionResponse("12345", "PENDING");
    }

    @GetMapping("/status/{executionId}")
    @Operation(summary = "Get pipeline execution status", description = "Retrieves the status of a running or completed pipeline.")
    public PipelineExecutionResponse getPipelineStatus(@PathVariable String executionId) {
        // Implementation
        return new PipelineExecutionResponse(executionId, "RUNNING");
    }
}
```

### 🛠️ Other Controllers to Annotate
- `JobController.java`
- `StageController.java`
- `ReportController.java`

---

## 4⃣ Start the Application and Access Swagger UI
Now, run your Spring Boot application:
```bash
./gradlew bootRun
```
Then, open Swagger UI in your browser:
```bash
http://localhost:8080/swagger-ui.html
```
🔹 You should see all available API endpoints with descriptions.

---

## 5⃣ Validate & Export OpenAPI Specification
If you want to generate and export the OpenAPI JSON/YAML spec, visit:
```bash
http://localhost:8080/v3/api-docs  (JSON)
http://localhost:8080/v3/api-docs.yaml  (YAML)
```
You can download this file and share it with frontend teams or use it for API testing.

---

## ✅ Verification Steps

| Task | How to Verify? |
|------|---------------|
| Swagger UI is accessible | Open `http://localhost:8080/swagger-ui.html` |
| API endpoints appear correctly | Check if `PipelineController`, `JobController`, `StageController` APIs are visible |
| API documentation matches the design | Ensure request/response models are displayed |
| OpenAPI JSON/YAML is generated properly | Open `http://localhost:8080/v3/api-docs` |

---

## 🎯 Final Deliverable
- API documentation auto-generated via Swagger UI.
- Validations for API models and responses.
- OpenAPI JSON/YAML spec ready for sharing.