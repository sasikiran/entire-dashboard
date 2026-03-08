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
 * 验证 JGit Repository.resolve() 与 RefDatabase 在真实仓库下的行为，
 * 便于对比「返回非 null」与「返回 null」的差异。
 */
class CheckpointGitReaderResolveTest {

    /**
     * 使用本地真实仓库 data/5 做验证（需先存在且含 .git）。
     * 运行前请把 REPO_DIR 改成你本机路径，或通过 -Drepo.dir=... 传入。
     */
    private static final String REPO_DIR = System.getProperty("repo.dir",
            "data/5");

    @Test
    void resolveOnRealRepo() throws IOException {
        File repoDir = new File(REPO_DIR);
        if (!repoDir.exists() || !new File(repoDir, ".git").exists()) {
            System.out.println("SKIP: 仓库不存在或非 Git 目录: " + repoDir.getAbsolutePath());
            return;
        }

        // 明确指定 .git 目录，避免被当成 bare 仓库
        File gitDir = new File(repoDir, ".git");
        if (!gitDir.isDirectory()) {
            System.out.println("SKIP: .git 不是目录（可能是 worktree 的 git 文件）: " + gitDir.getAbsolutePath());
            return;
        }
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .setWorkTree(repoDir)
                .build()) {
            System.out.println("Git 目录: " + repository.getDirectory().getAbsolutePath());
            System.out.println("WorkTree: " + (repository.isBare() ? "(bare)" : repository.getWorkTree().getAbsolutePath()));

            // 1) resolve 各种 ref
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
            System.out.println("refDb.getRefsByPrefix(\"refs/heads\") 数量: " + (byPrefix != null ? byPrefix.size() : 0));
            if (byPrefix != null) {
                byPrefix.forEach(ref ->
                        System.out.println("  " + ref.getName() + " -> " + ref.getObjectId().getName()));
            }

            // 3) 至少有一种方式能拿到 develop 的 commit
            ObjectId byResolve = repository.resolve("refs/heads/develop");
            ObjectId byExact = exactDevelop != null ? exactDevelop.getObjectId() : null;
            assertNotNull(byResolve != null ? byResolve : byExact,
                    "develop 应能通过 resolve 或 exactRef 得到 commit");
        }
    }

    /**
     * 在临时目录用 JGit 创建最小仓库（松散 ref），验证 resolve 能返回非 null 的示例。
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
            System.out.println("(临时仓库-松散ref) repository.resolve(\"HEAD\") = " + (head != null ? head.getName() : "null"));
            System.out.println("(临时仓库-松散ref) repository.resolve(\"refs/heads/main\") = " + (main != null ? main.getName() : "null"));
            assertNotNull(head, "HEAD 应有值");
            assertNotNull(main, "refs/heads/main 应有值");
        }
    }
}
