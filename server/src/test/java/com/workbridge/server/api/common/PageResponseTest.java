package com.workbridge.server.api.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResponseTest {

    @Test
    void calculatesTotalPagesFromTotalElementsAndPageSize() {
        PageResponse<String> response = PageResponse.of(List.of("A", "B"), 1, 10, 21);

        assertThat(response.getItems()).containsExactly("A", "B");
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(21);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    void rejectsInvalidPageOrSize() {
        assertThatThrownBy(() -> PageResponse.of(List.of(), -1, 10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
        assertThatThrownBy(() -> PageResponse.of(List.of(), 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> PageResponse.of(List.of(), 0, 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalElements");
    }
}
