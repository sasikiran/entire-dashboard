package com.mzfuture.entire.gitrepo.repository;

import com.mzfuture.entire.gitrepo.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repo, Long>, QuerydslPredicateExecutor<Repo> {
    boolean existsByName(String name);

    boolean existsByWebUrl(String webUrl);

    boolean existsByNameAndIdNot(String name, Long id);

    /// 根据WebURL查找仓库
    ///
    /// @param webUrl 仓库WebURL
    /// @return 仓库实体Optional
    Optional<Repo> findByWebUrl(String webUrl);
}
