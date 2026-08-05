package com.workbridge.server.audit;

import java.util.Locale;
import java.util.Set;

public final class MaskingUtils {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "password", "passwd", "token", "secret", "authorization", "credential"
    );

    private MaskingUtils() {
    }

    public static String maskRejectedValue(String fieldName, Object rejectedValue) {
        if (rejectedValue == null) {
            return null;
        }

        String normalizedField = fieldName == null ? "" : fieldName.toLowerCase(Locale.ROOT);
        if (SENSITIVE_KEYWORDS.stream().anyMatch(normalizedField::contains)) {
            return "[MASKED]";
        }

        String value = String.valueOf(rejectedValue);
        if (normalizedField.contains("email")) {
            return maskEmail(value);
        }
        if (normalizedField.contains("phone")) {
            return maskPhone(value);
        }
        if (normalizedField.contains("businessregistration")) {
            return maskBusinessRegistrationNumber(value);
        }
        return value.length() > 100 ? value.substring(0, 100) + "..." : value;
    }


    public static String maskLoginId(String value) {
        if (value == null || value.isBlank()) {
            return "***";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 2) {
            return trimmed.charAt(0) + "***";
        }
        return trimmed.substring(0, 2) + "***";
    }

    private static String maskEmail(String value) {
        int atIndex = value.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return value.charAt(0) + "***" + value.substring(atIndex);
    }

    private static String maskPhone(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            return "****";
        }
        return "***-****-" + digits.substring(digits.length() - 4);
    }

    private static String maskBusinessRegistrationNumber(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() != 10) {
            return "***-**-*****";
        }
        return digits.substring(0, 3) + "-**-" + digits.substring(5);
    }
}
