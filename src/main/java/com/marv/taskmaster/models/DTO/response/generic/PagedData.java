package com.marv.taskmaster.models.DTO.response.generic;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedData<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    // Constructor that takes a Spring 'Page' object and extracts metadata
    public PagedData(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber() + 1; // Return 1-based index to client
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}