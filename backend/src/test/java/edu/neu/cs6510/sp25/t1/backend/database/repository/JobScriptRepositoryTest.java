package edu.neu.cs6510.sp25.t1.backend.database.repository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JobScriptRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private JobScriptRepository jobScriptRepository;

    @Mock
    private Query query;

    private UUID jobId;
    private String scriptContent;

    @BeforeEach
    public void setUp() {
        jobId = UUID.randomUUID();
        scriptContent = "echo 'Running test script'";

        // Mock the query building chain - only the basic setup that all tests will use
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        // We'll set up specific parameter mocks in each test method
    }

    @Test
    public void testSaveScript() {
        // Arrange
        when(query.setParameter(eq("jobId"), eq(jobId))).thenReturn(query);
        when(query.setParameter(eq("script"), eq(scriptContent))).thenReturn(query);

        // Act
        jobScriptRepository.saveScript(jobId, scriptContent);

        // Assert
        verify(entityManager).createNativeQuery("INSERT INTO job_scripts (job_id, script) VALUES (:jobId, :script)");
        verify(query).setParameter("jobId", jobId);
        verify(query).setParameter("script", scriptContent);
        verify(query).executeUpdate();
    }

    @Test
    public void testSaveScript_WithEmptyScript() {
        // Arrange
        String emptyScript = "";
        when(query.setParameter(eq("jobId"), eq(jobId))).thenReturn(query);
        when(query.setParameter(eq("script"), eq(emptyScript))).thenReturn(query);

        // Act
        jobScriptRepository.saveScript(jobId, emptyScript);

        // Assert
        verify(entityManager).createNativeQuery("INSERT INTO job_scripts (job_id, script) VALUES (:jobId, :script)");
        verify(query).setParameter("jobId", jobId);
        verify(query).setParameter("script", emptyScript);
        verify(query).executeUpdate();
    }

    @Test
    public void testSaveScript_WithLongScript() {
        // Arrange
        StringBuilder longScriptBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longScriptBuilder.append("echo 'Line ").append(i).append("'\n");
        }
        String longScript = longScriptBuilder.toString();
        when(query.setParameter(eq("jobId"), eq(jobId))).thenReturn(query);
        when(query.setParameter(eq("script"), eq(longScript))).thenReturn(query);

        // Act
        jobScriptRepository.saveScript(jobId, longScript);

        // Assert
        verify(entityManager).createNativeQuery("INSERT INTO job_scripts (job_id, script) VALUES (:jobId, :script)");
        verify(query).setParameter("jobId", jobId);
        verify(query).setParameter("script", longScript);
        verify(query).executeUpdate();
    }

    @Test
    public void testSaveScript_WithSpecialCharacters() {
        // Arrange
        String scriptWithSpecialChars = "#!/bin/bash\necho 'Special chars: !@#$%^&*()'\ngrep -E \"pattern\" file.txt";
        when(query.setParameter(eq("jobId"), eq(jobId))).thenReturn(query);
        when(query.setParameter(eq("script"), eq(scriptWithSpecialChars))).thenReturn(query);

        // Act
        jobScriptRepository.saveScript(jobId, scriptWithSpecialChars);

        // Assert
        verify(entityManager).createNativeQuery("INSERT INTO job_scripts (job_id, script) VALUES (:jobId, :script)");
        verify(query).setParameter("jobId", jobId);
        verify(query).setParameter("script", scriptWithSpecialChars);
        verify(query).executeUpdate();
    }
}