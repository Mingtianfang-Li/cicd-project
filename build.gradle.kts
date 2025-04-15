plugins {
    java
    jacoco
    checkstyle
    pmd
    id("com.github.ben-manes.versions") version "0.52.0" // finding outdated dependencies
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")

    repositories {
        mavenCentral()
    }

    dependencies {
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
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    jacoco {
        toolVersion = "0.8.12"
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        // Instead of afterEvaluate, use configure block to modify classDirectories
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        // Backend module exclusions
                        "**/config/**", // Ignore all 'config' folders
                        "**/database/**", // Ignore entire 'database' folder
                        "**/database/entity/**", // Ignore entities inside database folder
                        "**/database/repository/**", // Ignore repository inside database folder
                        "**/backendApp.*", // Ignore entry point file backendApp

                        // Common module exclusions
                        "**/config/**", // Ignore all 'config' folders
                        "**/enums/status/**" // Ignore all 'enums' ending in status
                    )
                }
            })
        )
    }

    tasks.jacocoTestCoverageVerification {
        dependsOn(tasks.jacocoTestReport)
        violationRules {
            rule {
                limit{
                    minimum = "0.0".toBigDecimal()
                }
            }
        }

//        violationRules {
//            rule {
//                element = "CLASS"
//                excludes = listOf(
//                    // Backend module exclusions
//                    "edu.neu.cs6510.sp25.t1.backend.config.*",
//                    "edu.neu.cs6510.sp25.t1.backend.database.*",
//                    "edu.neu.cs6510.sp25.t1.backend.database.entity.*",
//                    "edu.neu.cs6510.sp25.t1.backend.database.repository.*",
//                    "edu.neu.cs6510.sp25.t1.backend.backendApp",
//
//                    // Common module exclusions
//                    "edu.neu.cs6510.sp25.t1.common.config.*",
//                    "edu.neu.cs6510.sp25.t1.common.enums.status.*"
//                )
//
//                limit {
//                    counter = "LINE"
//                    value = "COVEREDRATIO"
//                    minimum = "0.7".toBigDecimal()
//                }
//
//                limit {
//                    counter = "BRANCH"
//                    value = "COVEREDRATIO"
//                    minimum = "0.7".toBigDecimal()
//                }
//            }
//        }
    }


    tasks.check {
        dependsOn(tasks.test)
//        dependsOn(tasks.jacocoTestCoverageVerification)
    }

    checkstyle {
        toolVersion = "9.3"
        configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
        configProperties["checkstyle.suppressions.file"] = file("$rootDir/config/checkstyle/checkstyle-suppressions.xml").absolutePath
    }

    tasks.withType<Checkstyle> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    pmd {
        toolVersion = "6.55.0"
        rulesMinimumPriority.set(5)
    }

    tasks.withType<Pmd> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
