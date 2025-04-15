package edu.neu.cs6510.sp25.t1.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "artifacts.storagePath=/tmp/artifacts",
                    "artifacts.retentionDays=30"
            );

    @Test
    void propertiesAreBoundCorrectly() {
        contextRunner.run(context -> {
            ArtifactProperties props = context.getBean(ArtifactProperties.class);
            assertThat(props.getStoragePath()).isEqualTo("/tmp/artifacts");
            assertThat(props.getRetentionDays()).isEqualTo(30);
        });
    }

    @EnableConfigurationProperties(ArtifactProperties.class)
    static class TestConfig {
    }
}
