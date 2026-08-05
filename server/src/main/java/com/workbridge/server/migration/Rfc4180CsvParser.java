package com.workbridge.server.migration;

import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;

public class Rfc4180CsvParser {

    public List<List<String>> parse(String input) {
        if (input == null) {
            throw new BusinessException(ErrorCode.INVALID_MIGRATION_FILE);
        }

        String text = input.startsWith("\uFEFF") ? input.substring(1) : input;
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (quoted) {
                if (current == '"') {
                    if (index + 1 < text.length() && text.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(current);
                }
            } else if (current == '"') {
                if (field.length() != 0) {
                    throw new BusinessException(
                            ErrorCode.INVALID_MIGRATION_FILE,
                            "CSV 인용부호 위치가 올바르지 않습니다.");
                }
                quoted = true;
            } else if (current == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (current == '\r' || current == '\n') {
                row.add(field.toString());
                field.setLength(0);
                rows.add(List.copyOf(row));
                row.clear();
                if (current == '\r'
                        && index + 1 < text.length()
                        && text.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                field.append(current);
            }
        }

        if (quoted) {
            throw new BusinessException(
                    ErrorCode.INVALID_MIGRATION_FILE,
                    "CSV 인용부호가 닫히지 않았습니다.");
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(List.copyOf(row));
        }

        return rows.stream()
                .filter(values -> values.stream().anyMatch(value -> !value.isBlank()))
                .toList();
    }
}
