package com.workbridge.server.domain.sales;

public final class BusinessRegistrationNumber {

    private static final String SUPPORTED_CHARACTERS = "[0-9\\s-]+";

    private BusinessRegistrationNumber() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replaceAll("\\s", "");
    }

    public static boolean hasSupportedCharacters(String value) {
        return value != null && value.matches(SUPPORTED_CHARACTERS);
    }

    public static boolean isValid(String value) {
        return hasSupportedCharacters(value) && normalize(value).length() == 10;
    }

    public static String format(String value) {
        if (!isValid(value)) {
            return value;
        }
        String digits = normalize(value);
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }
}
