package com.mzfuture.entire.gitrepo.entity;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import com.mzfuture.entire.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "repository")
public class Repo extends BaseEntity {
    private String name;

    private String webUrl;

    @Enumerated(EnumType.STRING)
    private RepositoryPlatform platform;

    private String accessToken;

    /// Last successful checkpoint sync time (Unix milliseconds), null means never synced
    private Long lastSuccessfulSyncAt;
}
