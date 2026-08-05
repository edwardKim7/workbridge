package com.workbridge.server.api.common;

import java.util.List;
import java.util.Objects;

public class PageResponse<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    private PageResponse(List<T> items, int page, int size, long totalElements, int totalPages) {
        this.items = List.copyOf(items);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements) {
        Objects.requireNonNull(items, "items는 null일 수 없습니다.");
        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size는 1 이상이어야 합니다.");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements는 0 이상이어야 합니다.");
        }
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(items, page, size, totalElements, totalPages);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
