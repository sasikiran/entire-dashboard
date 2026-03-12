package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import org.springframework.stereotype.Component;

import java.io.File;

/// Local filesystem Git repository adapter
@Component
public class LocalAdapter implements GitPlatformAdapter {

    @Override
    public RepositoryPlatform getPlatform() {
        return RepositoryPlatform.LOCAL;
    }

    @Override
    public String buildAuthUrl(String webUrl, String accessToken) {
        return new File(webUrl).toURI().toString();
    }

    @Override
    public String buildCommitUrl(String webUrl, String commitSha) {
        return null;
    }

    @Override
    public boolean validateToken(String webUrl, String accessToken) {
        return true;
    }
}
