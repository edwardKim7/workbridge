package com.workbridge.server.migration;

import com.workbridge.server.domain.migration.MigrationErrorRecord;
import com.workbridge.server.domain.migration.MigrationOperationType;
import com.workbridge.server.domain.migration.MigrationStagingRecord;
import com.workbridge.server.domain.migration.MigrationValidationStatus;
import com.workbridge.server.domain.sales.BusinessRegistrationNumber;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CustomerMigrationValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Validation validate(int rowNo, List<String> values, Set<String> seenBusinessNumbers) {
        List<MigrationErrorRecord> errors = new ArrayList<>();
        MigrationStagingRecord staging = new MigrationStagingRecord();
        staging.setRowNo(rowNo);
        staging.setRawData(String.join(",", values));

        String businessNumber = valueAt(values, 0);
        if (!BusinessRegistrationNumber.isValid(businessNumber)) {
            errors.add(error(
                    rowNo,
                    "business_registration_no",
                    "INVALID_BUSINESS_NO",
                    "사업자번호는 숫자 10자리여야 합니다.",
                    businessNumber));
            businessNumber = truncate(
                    BusinessRegistrationNumber.normalize(businessNumber), 10);
        } else {
            businessNumber = BusinessRegistrationNumber.normalize(businessNumber);
        }

        if (businessNumber != null
                && !businessNumber.isBlank()
                && !seenBusinessNumbers.add(businessNumber)) {
            errors.add(error(
                    rowNo,
                    "business_registration_no",
                    "DUPLICATE_IN_FILE",
                    "파일 안에 같은 사업자번호가 중복되었습니다.",
                    businessNumber));
        }

        staging.setBusinessRegistrationNo(businessNumber);
        staging.setCustomerName(required(
                rowNo, "customer_name", valueAt(values, 1), 200, errors));
        staging.setRepresentativeName(optional(
                valueAt(values, 2), 100, errors, rowNo, "representative_name"));
        staging.setContactName(optional(
                valueAt(values, 3), 100, errors, rowNo, "contact_name"));

        String email = optional(
                valueAt(values, 4), 200, errors, rowNo, "contact_email");
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add(error(
                    rowNo,
                    "contact_email",
                    "INVALID_EMAIL",
                    "이메일 형식이 올바르지 않습니다.",
                    email));
        }
        staging.setContactEmail(email);
        staging.setContactPhone(optional(
                valueAt(values, 5), 30, errors, rowNo, "contact_phone"));

        String establishedDate = valueAt(values, 6);
        if (establishedDate != null && !establishedDate.isBlank()) {
            try {
                staging.setEstablishedDate(LocalDate.parse(establishedDate.trim()));
            } catch (DateTimeParseException exception) {
                errors.add(error(
                        rowNo,
                        "established_date",
                        "INVALID_DATE",
                        "날짜는 yyyy-MM-dd 형식이어야 합니다.",
                        establishedDate));
            }
        }

        String assignedEmployeeNo = required(
                rowNo,
                "assigned_sales_employee_no",
                valueAt(values, 7),
                30,
                errors);
        staging.setNormalizedData(assignedEmployeeNo);
        staging.setValidationStatus(
                errors.isEmpty()
                        ? MigrationValidationStatus.VALID
                        : MigrationValidationStatus.INVALID);
        staging.setOperationType(MigrationOperationType.NONE);

        return new Validation(staging, errors, assignedEmployeeNo);
    }

    private String required(
            int rowNo,
            String fieldName,
            String value,
            int maxLength,
            List<MigrationErrorRecord> errors) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            errors.add(error(rowNo, fieldName, "REQUIRED", "필수값입니다.", value));
            return null;
        }
        if (normalized.length() > maxLength) {
            errors.add(error(
                    rowNo,
                    fieldName,
                    "TOO_LONG",
                    "최대 " + maxLength + "자까지 입력할 수 있습니다.",
                    normalized));
            return normalized.substring(0, maxLength);
        }
        return normalized;
    }

    private String optional(
            String value,
            int maxLength,
            List<MigrationErrorRecord> errors,
            int rowNo,
            String fieldName) {
        String normalized = trimToNull(value);
        if (normalized != null && normalized.length() > maxLength) {
            errors.add(error(
                    rowNo,
                    fieldName,
                    "TOO_LONG",
                    "최대 " + maxLength + "자까지 입력할 수 있습니다.",
                    normalized));
            return normalized.substring(0, maxLength);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String valueAt(List<String> values, int index) {
        return index < values.size() ? values.get(index) : null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private MigrationErrorRecord error(
            int rowNo,
            String fieldName,
            String errorCode,
            String message,
            String rawValue) {
        return new MigrationErrorRecord(
                null,
                rowNo,
                fieldName,
                errorCode,
                message,
                rawValue == null ? null : truncate(rawValue, 500));
    }

    public record Validation(
            MigrationStagingRecord staging,
            List<MigrationErrorRecord> errors,
            String assignedEmployeeNo) {}
}
