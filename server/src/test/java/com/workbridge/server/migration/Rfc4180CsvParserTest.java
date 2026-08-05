package com.workbridge.server.migration;

import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Rfc4180CsvParserTest {

    private final Rfc4180CsvParser parser = new Rfc4180CsvParser();

    @Test
    void quotedCommaAndEscapedQuoteAreParsed() {
        List<List<String>> rows = parser.parse("a,b\r\n1,\"가상고객, A\"\r\n2,\"담당자 \"\"홍길동\"\"\"\r\n");

        assertThat(rows).containsExactly(
                List.of("a", "b"),
                List.of("1", "가상고객, A"),
                List.of("2", "담당자 \"홍길동\"")
        );
    }

    @Test
    void unclosedQuoteIsRejected() {
        assertThatThrownBy(() -> parser.parse("a,b\n1,\"not-closed"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_MIGRATION_FILE));
    }
}
