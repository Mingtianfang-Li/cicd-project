package edu.neu.cs6510.sp25.t1.common.utils;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class GitCloneUtilTest {

    @TempDir
    private Path tempDir;

    @Test
    void cloneRepository() throws GitAPIException {
        // Use Mockito to mock Git operations
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            // Arrange
            String repoUrl = "https://github.com/Mingtianfang-Li/demoProject2";
            File targetDir = tempDir.toFile();

            // Mock the Git clone operation
            Git mockGit = mock(Git.class);
            Repository mockRepo = mock(Repository.class);
            File mockGitDir = mock(File.class);
            File mockParentFile = mock(File.class);

            // Set up the mock chain
            org.eclipse.jgit.api.CloneCommand mockCloneCmd = mock(org.eclipse.jgit.api.CloneCommand.class);
            gitMock.when(Git::cloneRepository).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setURI(repoUrl)).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setDirectory(targetDir)).thenReturn(mockCloneCmd);
            when(mockCloneCmd.call()).thenReturn(mockGit);
            when(mockGit.getRepository()).thenReturn(mockRepo);
            when(mockRepo.getDirectory()).thenReturn(mockGitDir);
            when(mockGitDir.getParentFile()).thenReturn(mockParentFile);

            // Act
            File result = GitCloneUtil.cloneRepository(repoUrl, targetDir);

            // Assert
            assertEquals(mockParentFile, result);
        }
    }

    @Test
    void testCloneRepository() throws GitAPIException {
        // Use Mockito to mock Git operations
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            // Arrange
            String repoUrl = "https://github.com/Mingtianfang-Li/demoProject2";
            String branch = "main";
            File targetDir = tempDir.toFile();

            // Mock the Git clone operation
            Git mockGit = mock(Git.class);
            Repository mockRepo = mock(Repository.class);
            File mockGitDir = mock(File.class);
            File mockParentFile = mock(File.class);

            // Set up the mock chain
            org.eclipse.jgit.api.CloneCommand mockCloneCmd = mock(org.eclipse.jgit.api.CloneCommand.class);
            gitMock.when(Git::cloneRepository).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setURI(repoUrl)).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setDirectory(targetDir)).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setBranch("refs/heads/" + branch)).thenReturn(mockCloneCmd);
            when(mockCloneCmd.call()).thenReturn(mockGit);
            when(mockGit.getRepository()).thenReturn(mockRepo);
            when(mockRepo.getDirectory()).thenReturn(mockGitDir);
            when(mockGitDir.getParentFile()).thenReturn(mockParentFile);

            // Act
            File result = GitCloneUtil.cloneRepository(repoUrl, targetDir, branch);

            // Assert
            assertEquals(mockParentFile, result);
        }
    }

    @Test
    void cloneRepositoryThrowsException() throws Exception {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            CloneCommand mockCloneCmd = mock(CloneCommand.class);
            gitMock.when(Git::cloneRepository).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setURI(anyString())).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setDirectory(any(File.class))).thenReturn(mockCloneCmd);
            when(mockCloneCmd.call()).thenThrow(new GitAPIException("Mock Git clone failure") {
            }); 

            assertThrows(GitAPIException.class, () -> GitCloneUtil.cloneRepository("url", tempDir.toFile()));
        }
    }

    @Test
    void checkoutCommit() throws Exception {
        // Use Mockito to mock Git operations
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            // Arrange
            File repoDir = tempDir.toFile();
            String commitHash = "7f175e2706e6202530715ec8b246ac5282bfd45a";

            // Mock the Git open operation
            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetchCmd = mock(org.eclipse.jgit.api.FetchCommand.class);
            org.eclipse.jgit.api.CheckoutCommand mockCheckoutCmd = mock(org.eclipse.jgit.api.CheckoutCommand.class);

            // Set up the mock chain
            gitMock.when(() -> Git.open(any(File.class))).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetchCmd);
            when(mockFetchCmd.call()).thenReturn(null);
            when(mockGit.checkout()).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.setName(commitHash)).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.call()).thenReturn(null);

            // Act - should not throw exception
            GitCloneUtil.checkoutCommit(repoDir, commitHash);

            // Assert - verify method calls
            gitMock.verify(() -> Git.open(repoDir));
            verify(mockGit).fetch();
            verify(mockFetchCmd).call();
            verify(mockGit).checkout();
            verify(mockCheckoutCmd).setName(commitHash);
            verify(mockCheckoutCmd).call();
        }
    }

    @Test
    void checkoutCommitThrowsException() {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String commitHash = "deadbeef";

            gitMock.when(() -> Git.open(repoDir)).thenThrow(new IOException("Mock Git open failure"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommit(repoDir, commitHash);
            });
        }
    }

    @Test
    void checkoutCommitInBranchThrowsException() {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String branch = "main";
            String commitHash = "abc123";

            gitMock.when(() -> Git.open(repoDir)).thenThrow(new IOException("Mock Git open failure"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommitInBranch(repoDir, branch, commitHash);
            });
        }
    }

    @Test
    void checkoutCommitInBranch() throws Exception {
        // Use Mockito to mock Git operations
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            // Arrange
            File repoDir = tempDir.toFile();
            String branchName = "main";
            String commitHash = "7f175e2706e6202530715ec8b246ac5282bfd45a";

            // Mock the Git open operation
            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetchCmd = mock(org.eclipse.jgit.api.FetchCommand.class);
            org.eclipse.jgit.api.CheckoutCommand mockCheckoutCmd = mock(org.eclipse.jgit.api.CheckoutCommand.class);

            // Set up the mock chain
            gitMock.when(() -> Git.open(any(File.class))).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetchCmd);
            when(mockFetchCmd.call()).thenReturn(null);
            when(mockGit.checkout()).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.setName(anyString())).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.setCreateBranch(anyBoolean())).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.setStartPoint(anyString())).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.setUpstreamMode(any())).thenReturn(mockCheckoutCmd);
            when(mockCheckoutCmd.call()).thenReturn(null);

            // Act - should not throw exception
            GitCloneUtil.checkoutCommitInBranch(repoDir, branchName, commitHash);

            // Assert - verify the key method calls
            gitMock.verify(() -> Git.open(repoDir));
            verify(mockGit).fetch();
            verify(mockFetchCmd).call();
            verify(mockGit, times(2)).checkout(); // Should be called twice
        }
    }

    @Test
    void isInsideGitRepo() {
        File nonGitDir = tempDir.toFile();
        boolean result = GitCloneUtil.isInsideGitRepo(nonGitDir);
        assertFalse(result);
    }

    @Test
    void getRepoUrlFromFileReturnsUrl(@TempDir Path tempDir) throws IOException {
        File gitDir = tempDir.resolve(".git").toFile(); // → tempDir/.git
        Repository repo = FileRepositoryBuilder.create(gitDir);
        repo.create();

        StoredConfig config = repo.getConfig();
        config.setString("remote", "origin", "url", "https://github.com/test/repo.git");
        config.save();

        String url = GitCloneUtil.getRepoUrlFromFile(tempDir.toFile());
        assertEquals("https://github.com/test/repo.git", url);
    }

    @Test
    void isInsideGitRepoReturnsTrue(@TempDir Path tempDir) throws GitAPIException {
        Git.init().setDirectory(tempDir.toFile()).call();
        assertTrue(GitCloneUtil.isInsideGitRepo(tempDir.toFile()));
    }

    @Test
    void getRepoUrlFromFileThrowsExceptionWhenNotGitRepo() {
        File nonGitDir = tempDir.toFile();
        assertThrows(IllegalArgumentException.class, () -> GitCloneUtil.getRepoUrlFromFile(nonGitDir));
    }

    @Test
    void cloneRepositoryInvalidBranchThrowsException() throws GitAPIException {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            CloneCommand mockCloneCmd = mock(CloneCommand.class);
            gitMock.when(Git::cloneRepository).thenReturn(mockCloneCmd);

            when(mockCloneCmd.setURI(anyString())).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setDirectory(any(File.class))).thenReturn(mockCloneCmd);
            when(mockCloneCmd.setBranch(anyString())).thenReturn(mockCloneCmd);
            when(mockCloneCmd.call()).thenThrow(new GitAPIException("Invalid branch") {
            });

            assertThrows(GitAPIException.class,
                    () -> GitCloneUtil.cloneRepository("url", tempDir.toFile(), "nonexistent-branch"));
        }
    }

    @Test
    void checkoutCommitInBranchFailsOnSecondCheckout() throws Exception {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String branchName = "main";
            String commitHash = "abc123";

            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetchCmd = mock(org.eclipse.jgit.api.FetchCommand.class);
            org.eclipse.jgit.api.CheckoutCommand mockCheckoutCmd1 = mock(org.eclipse.jgit.api.CheckoutCommand.class);
            org.eclipse.jgit.api.CheckoutCommand mockCheckoutCmd2 = mock(org.eclipse.jgit.api.CheckoutCommand.class);

            gitMock.when(() -> Git.open(any(File.class))).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetchCmd);
            when(mockFetchCmd.call()).thenReturn(null);

            // First checkout (branch)
            when(mockGit.checkout())
                    .thenReturn(mockCheckoutCmd1) // first checkout call
                    .thenReturn(mockCheckoutCmd2); // second checkout call

            when(mockCheckoutCmd1.setName(anyString())).thenReturn(mockCheckoutCmd1);
            when(mockCheckoutCmd1.setCreateBranch(anyBoolean())).thenReturn(mockCheckoutCmd1);
            when(mockCheckoutCmd1.setStartPoint(anyString())).thenReturn(mockCheckoutCmd1);
            when(mockCheckoutCmd1.setUpstreamMode(any())).thenReturn(mockCheckoutCmd1);
            when(mockCheckoutCmd1.call()).thenReturn(null);

            // Second checkout (commit)
            when(mockCheckoutCmd2.setName(anyString())).thenReturn(mockCheckoutCmd2);
            when(mockCheckoutCmd2.call()).thenThrow(new IOException("Mock checkout failure"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommitInBranch(repoDir, branchName, commitHash);
            });
        }
    }

    @Test
    void checkoutCommitFetchThrowsException() throws Exception {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String commitHash = "somehash";

            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetchCmd = mock(org.eclipse.jgit.api.FetchCommand.class);

            gitMock.when(() -> Git.open(repoDir)).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetchCmd);
            when(mockFetchCmd.call()).thenThrow(new IOException("fetch failed"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommit(repoDir, commitHash);
            });
        }
    }

    @Test
    void checkoutCommitInBranchFetchFails() throws Exception {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String branch = "main";
            String commitHash = "abc123";

            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetchCmd = mock(org.eclipse.jgit.api.FetchCommand.class);

            gitMock.when(() -> Git.open(repoDir)).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetchCmd);
            when(mockFetchCmd.call()).thenThrow(new IOException("fetch failed"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommitInBranch(repoDir, branch, commitHash);
            });
        }
    }

    @Test
    void getRepoUrlFromFileReturnsNullWhenNoOrigin() throws IOException {
        File gitDir = tempDir.resolve(".git").toFile();
        Repository repo = FileRepositoryBuilder.create(gitDir);
        repo.create();

        String url = GitCloneUtil.getRepoUrlFromFile(tempDir.toFile());
        assertNull(url);
    }

    @Test
    void checkoutCommitFailsOnCheckoutCall() throws Exception {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String commitHash = "abc";

            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetch = mock(org.eclipse.jgit.api.FetchCommand.class);
            org.eclipse.jgit.api.CheckoutCommand mockCheckout = mock(org.eclipse.jgit.api.CheckoutCommand.class);

            gitMock.when(() -> Git.open(repoDir)).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetch);
            when(mockFetch.call()).thenReturn(null);

            when(mockGit.checkout()).thenReturn(mockCheckout);
            when(mockCheckout.setName(commitHash)).thenReturn(mockCheckout);
            when(mockCheckout.call()).thenThrow(new RuntimeException("checkout fails"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommit(repoDir, commitHash);
            });
        }
    }

    @Test
    void checkoutCommitInBranchFailsOnFirstCheckout() throws Exception {
        try (MockedStatic<Git> gitMock = mockStatic(Git.class)) {
            File repoDir = tempDir.toFile();
            String branch = "main";
            String commitHash = "abc";

            Git mockGit = mock(Git.class);
            org.eclipse.jgit.api.FetchCommand mockFetch = mock(org.eclipse.jgit.api.FetchCommand.class);
            org.eclipse.jgit.api.CheckoutCommand mockCheckout = mock(org.eclipse.jgit.api.CheckoutCommand.class);

            gitMock.when(() -> Git.open(repoDir)).thenReturn(mockGit);
            when(mockGit.fetch()).thenReturn(mockFetch);
            when(mockFetch.call()).thenReturn(null);
            when(mockGit.checkout()).thenReturn(mockCheckout);
            when(mockCheckout.setName(anyString())).thenReturn(mockCheckout);
            when(mockCheckout.setCreateBranch(anyBoolean())).thenReturn(mockCheckout);
            when(mockCheckout.setStartPoint(anyString())).thenReturn(mockCheckout);
            when(mockCheckout.setUpstreamMode(any())).thenReturn(mockCheckout);
            when(mockCheckout.call()).thenThrow(new IOException("first checkout fails"));

            assertThrows(Exception.class, () -> {
                GitCloneUtil.checkoutCommitInBranch(repoDir, branch, commitHash);
            });
        }
    }

}