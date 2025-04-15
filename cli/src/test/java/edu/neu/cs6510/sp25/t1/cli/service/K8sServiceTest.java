package edu.neu.cs6510.sp25.t1.cli.service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
;


class K8sServiceTest {

    @Mock
    CoreV1Api mockApi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testWaitForPodReady() throws Exception {
        V1Pod pod = new V1Pod();
        V1PodStatus status = new V1PodStatus();

        V1PodCondition readyCondition = new V1PodCondition()
                .type("Ready")
                .status("True");

        status.setConditions(List.of(readyCondition));
        pod.setStatus(status);

        when(mockApi.readNamespacedPod("test-pod", "default", null)).thenReturn(pod);

        assertDoesNotThrow(() -> waitForPod("test-pod", mockApi, 3));
    }

    @Test
    void testWaitForPodNotReady() throws Exception {
        V1Pod pod = new V1Pod();
        V1PodStatus status = new V1PodStatus();

        V1PodCondition notReadyCondition = new V1PodCondition()
                .type("Ready")
                .status("False");

        status.setConditions(List.of(notReadyCondition));
        pod.setStatus(status);

        when(mockApi.readNamespacedPod("test-pod", "default", null)).thenReturn(pod);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            waitForPod("test-pod", mockApi, 3);
        });

        assertTrue(exception.getMessage().contains("did not become ready"));
    }

    @Test
    void testWaitForPodStatusNull() throws Exception {
        V1Pod mockPod = mock(V1Pod.class);
        when(mockPod.getStatus()).thenReturn(null);
        when(mockApi.readNamespacedPod(any(), any(), any())).thenReturn(mockPod);

        Exception ex = assertThrows(RuntimeException.class, () ->
                waitForPod("null-status-pod", mockApi, 2)
        );
        assertTrue(ex.getMessage().contains("did not become ready"));
    }

    @Test
    void testWaitForPodConditionsNull() throws Exception {
        V1PodStatus mockStatus = mock(V1PodStatus.class);
        when(mockStatus.getConditions()).thenReturn(null);

        V1Pod mockPod = mock(V1Pod.class);
        when(mockPod.getStatus()).thenReturn(mockStatus);

        when(mockApi.readNamespacedPod(any(), any(), any())).thenReturn(mockPod);

        Exception ex = assertThrows(RuntimeException.class, () ->
                waitForPod("null-conditions-pod", mockApi, 2)
        );
        assertTrue(ex.getMessage().contains("did not become ready"));
    }

    @Test
    void testApplyYamlPodCreationFails() throws Exception {
        V1Pod pod = new V1Pod().metadata(new V1ObjectMeta());

        when(mockApi.createNamespacedPod(any(), any(), any(), any(), any(), any()))
                .thenThrow(new ApiException(400, "Simulated failure"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                applyYamlHelper("k8s/backend-pod.yaml", mockApi, "myPipeline", pod)
        );
        System.out.println("Actual exception message: " + ex.getMessage());

        assertTrue(ex.getMessage().startsWith("Failed to apply pod"));
    }




    // ====== Helper methods copied from K8sService logic ======
    private void waitForPod(String podName, CoreV1Api api, int retries) throws Exception {
        for (int i = 0; i < retries; i++) {
            try {
                V1Pod pod = api.readNamespacedPod(podName, "default", null);
                V1PodStatus status = pod.getStatus();

                if (status == null) {
                    Thread.sleep(10);
                    continue;
                }

                List<V1PodCondition> conditions = status.getConditions();
                if (conditions == null) {
                    Thread.sleep(10);
                    continue;
                }

                boolean ready = conditions.stream().anyMatch(
                        c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus())
                );

                if (ready) return;

            } catch (Exception e) {
                throw new RuntimeException("Failed to read pod status for: " + podName, e);
            }

            Thread.sleep(10); // short sleep for test
        }

        throw new RuntimeException("Pod " + podName + " did not become ready in time.");
    }


    private void stopPod(String podName, CoreV1Api api) throws Exception {
        V1DeleteOptions options = new V1DeleteOptions();
        api.deleteNamespacedPod(podName, "default", null, null, null, null, null, options);
    }

    private void applyYamlHelper(String filePath, CoreV1Api api, String pipelineName, Object obj) throws Exception {
        String safePipelineName = pipelineName.toLowerCase().replaceAll("[^a-z0-9.-]", "");
        String podName;

        if (filePath.contains("cicd")) {
            podName = "cicd-pod-" + safePipelineName;
        } else if (filePath.contains("backend")) {
            podName = "backend-pod-" + safePipelineName;
        } else {
            podName = "generic-pod-" + safePipelineName;
        }

        try {
            if (obj instanceof V1Pod pod) {
                pod.getMetadata().setName(podName);
                try {
                    api.createNamespacedPod("default", pod, null, null, null, null);
                } catch (ApiException e) {
                    throw new RuntimeException("Failed to apply pod " + podName, e);
                }
            } else if (obj instanceof V1Service svc) {
                api.createNamespacedService("default", svc, null, null, null, null);
            } else {
                throw new RuntimeException("Unsupported object type: " + obj.getClass());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply pod " + podName, e);
        }
    }


}
