package com.mzfuture.entire.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class PagerPayload<T> {

    public PagerPayload(Page<T> pageData, Pageable pageRequest) {
        this.data = pageData.getContent();
        this.total = pageData.getTotalElements();
        // Convert Spring's 0-based page number to frontend's 1-based page number
        this.page = pageRequest.getPageNumber() + 1;
        this.size = pageRequest.getPageSize();
    }

    public PagerPayload(List<T> data, Long total, Pageable pageRequest) {
        this.data = data;
        this.total = total;
        // Convert Spring's 0-based page number to frontend's 1-based page number
        this.page = pageRequest.getPageNumber() + 1;
        this.size = pageRequest.getPageSize();
    }

    private final List<T> data;

    /// Current page number, starting from 1
    private final int page;

    /// Number of items per page
    private final int size;

    /// Total number of items
    private final long total;
}

