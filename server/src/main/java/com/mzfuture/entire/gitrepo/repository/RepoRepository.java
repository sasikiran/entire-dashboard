package com.mzfuture.entire.gitrepo.repository;

import com.mzfuture.entire.gitrepo.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repo, Long>, QuerydslPredicateExecutor<Repo> {
    boolean existsByName(String name);

    boolean existsByWebUrl(String webUrl);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByWebUrlAndIdNot(String webUrl, Long id);

    /// Find repository by Web URL
    ///
    /// @param webUrl Repository Web URL
    /// @return Repository entity Optional
    Optional<Repo> findByWebUrl(String webUrl);
}
