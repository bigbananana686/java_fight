package com.itheima.java_fight.pojo;

import java.util.List;

public class PageResult<T> {

    private Long totalPages;
    private Long total;
    private List<T> records;

    public PageResult(Long totalPages, Long total, List<T> records) {
        this.totalPages = totalPages;
        this.total = total;
        this.records = records;
    }

    public PageResult() {
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}