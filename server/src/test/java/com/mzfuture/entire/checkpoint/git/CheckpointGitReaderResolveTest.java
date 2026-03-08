package com.mzfuture.entire.checkpoint.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verify JGit Repository.resolve() and RefDatabase behavior on real repositories,
 * to compare the difference between "returns non-null" and "returns null".
 */
class CheckpointGitReaderResolveTest {

    /**
     * Verify using local real repository data/5 (must exist first and contain .git).
     * Before running, change REPO_DIR to your local path, or pass via -Drepo.dir=...
     */
    private static final String REPO_DIR = System.getProperty("repo.dir",
            "data/5");

    @Test
    void resolveOnRealRepo() throws IOException {
        File repoDir = new File(REPO_DIR);
        if (!repoDir.exists() || !new File(repoDir, ".git").exists()) {
            System.out.println("SKIP: Repository does not exist or not a Git directory: " + repoDir.getAbsolutePath());
            return;
        }

        // Explicitly specify .git directory to avoid being treated as bare repository
        File gitDir = new File(repoDir, ".git");
        if (!gitDir.isDirectory()) {
            System.out.println("SKIP: .git is not a directory (possibly a git file for worktree): " + gitDir.getAbsolutePath());
            return;
        }
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .setWorkTree(repoDir)
                .build()) {
            System.out.println("Git directory: " + repository.getDirectory().getAbsolutePath());
            System.out.println("WorkTree: " + (repository.isBare() ? "(bare)" : repository.getWorkTree().getAbsolutePath()));

            // 1) resolve various refs
            String[] refsToResolve = {"HEAD", "refs/heads/main", "refs/heads/develop"};
            for (String refName : refsToResolve) {
                ObjectId id = repository.resolve(refName);
                System.out.println("repository.resolve(\"" + refName + "\") = "
                        + (id != null ? id.getName() : "null"));
            }

            // 2) RefDatabase
            RefDatabase refDb = repository.getRefDatabase();
            refDb.refresh();

            Ref exactDevelop = refDb.exactRef("refs/heads/develop");
            System.out.println("refDb.exactRef(\"refs/heads/develop\") = "
                    + (exactDevelop != null ? exactDevelop.getObjectId().getName() : "null"));

            List<Ref> byPrefix = refDb.getRefsByPrefix("refs/heads");
            System.out.println("refDb.getRefsByPrefix(\"refs/heads\") count: " + (byPrefix != null ? byPrefix.size() : 0));
            if (byPrefix != null) {
                byPrefix.forEach(ref ->
                        System.out.println("  " + ref.getName() + " -> " + ref.getObjectId().getName()));
            }

            // 3) At least one method can get develop's commit
            ObjectId byResolve = repository.resolve("refs/heads/develop");
            ObjectId byExact = exactDevelop != null ? exactDevelop.getObjectId() : null;
            assertNotNull(byResolve != null ? byResolve : byExact,
                    "develop should be able to get commit through resolve or exactRef");
        }
    }

    /**
     * Create minimal repository with JGit in temp dir (loose ref), verify resolve returns non-null.
     */
    @Test
    void resolveReturnsNonNullWhenLooseRefExists(@TempDir Path tempDir) throws Exception {
        File repoDir = tempDir.toFile();
        try (Git git = Git.init().setDirectory(repoDir).setInitialBranch("main").call()) {
            git.commit()
                    .setAllowEmpty(true)
                    .setMessage("init")
                    .setAuthor("test", "test@test")
                    .setCommitter("test", "test@test")
                    .call();
        }
        File gitDir = new File(repoDir, ".git");
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .setWorkTree(repoDir)
                .build()) {
            ObjectId head = repository.resolve("HEAD");
            ObjectId main = repository.resolve("refs/heads/main");
            System.out.println("(temp repo-loose ref) repository.resolve(\"HEAD\") = " + (head != null ? head.getName() : "null"));
            System.out.println("(temp repo-loose ref) repository.resolve(\"refs/heads/main\") = " + (main != null ? main.getName() : "null"));
            assertNotNull(head, "HEAD should have value");
            assertNotNull(main, "refs/heads/main should have value");
        }
    }
}
