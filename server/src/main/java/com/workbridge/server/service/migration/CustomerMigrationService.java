package com.workbridge.server.service.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workbridge.server.api.common.PageResponse;
import com.workbridge.server.config.MigrationProperties;
import com.workbridge.server.domain.migration.*;
import com.workbridge.server.dto.migration.*;
import com.workbridge.server.exception.*;
import com.workbridge.server.migration.*;
import com.workbridge.server.repository.migration.MigrationRepository;
import com.workbridge.server.security.CurrentEmployee;
import com.workbridge.server.service.audit.AuditService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.Paths;
import java.security.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CustomerMigrationService {
    public static final List<String> HEADERS = List.of("business_registration_no", "customer_name", "representative_name",
        "contact_name", "contact_email", "contact_phone", "established_date", "assigned_sales_employee_no");
    private final MigrationRepository repository;
    private final MigrationTransactionService tx;
    private final MigrationProperties properties;
    private final CustomerMigrationValidator validator;
    private final CurrentEmployee current;
    private final AuditService audit;
    private final ObjectMapper mapper;
    private final Clock clock;
    public CustomerMigrationService(MigrationRepository r, MigrationTransactionService tx, MigrationProperties p,
        CustomerMigrationValidator v, CurrentEmployee c, AuditService a, ObjectMapper m, Clock clock) {
        repository = r;
        this.tx = tx;
        properties = p;
        validator = v;
        current = c;
        audit = a;
        mapper = m;
        this.clock = clock;
    }
    public MigrationDetailResponse migrate(MultipartFile file) {
        Long actor = current.requireEmployeeId();
        byte[] bytes = validateFile(file);
        String hash = sha(bytes);
        if (repository.completedByHash(hash).isPresent()) throw new BusinessException(ErrorCode.DUPLICATE_MIGRATION_FILE);
        MigrationBatchRecord batch = new MigrationBatchRecord();
        batch.setBatchNo("MIG-" + LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + UUID
            .randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        batch.setMigrationType("CUSTOMER");
        batch.setOriginalFileName(safeName(file.getOriginalFilename()));
        batch.setFileSize(bytes.length);
        batch.setFileHash(hash);
        batch.setDedupKey(hash);
        batch.setStatus(MigrationBatchStatus.RECEIVED);
        batch.setExecutedBy(actor);
        batch.setStartedAt(LocalDateTime.now(clock));
        try {
            tx.create(batch);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_MIGRATION_FILE);
        }
        int total = 0;
        try {
            List<List<String >> rows = new Rfc4180CsvParser().parse(decode(bytes));
            if (rows.isEmpty() || !HEADERS.equals(rows.get(0))) throw new BusinessException(ErrorCode
                .INVALID_MIGRATION_FILE, "CSV 헤더가 제공된 양식과 일치하지 않습니다.");
            if (rows.size() == 1) throw new BusinessException(ErrorCode.INVALID_MIGRATION_FILE, "CSV에 데이터 행이 없습니다.");
            List<MigrationStagingRecord> staging = new ArrayList<> ();
            List<MigrationErrorRecord> errors = new ArrayList<> ();
            Set<String> seen = new HashSet<> ();
            for (int i = 1; i<rows.size(); i++) {
                List<String> values = rows.get(i);
                int rowNo = i + 1;
                total++;
                if (values.size() != HEADERS.size()) {
                    MigrationStagingRecord s = new MigrationStagingRecord();
                    s.setBatchId(batch.getBatchId());
                    s.setRowNo(rowNo);
                    s.setRawData(String.join(",", values));
                    s.setValidationStatus(MigrationValidationStatus.INVALID);
                    s.setOperationType(MigrationOperationType.NONE);
                    staging.add(s);
                    errors.add(new MigrationErrorRecord(batch.getBatchId(), rowNo, null, "COLUMN_COUNT",
                        "열 개수가 8개여야 합니다.", String.valueOf(values.size())));
                    continue;
                }
                CustomerMigrationValidator.Validation v = validator.validate(rowNo, values, seen);
                MigrationStagingRecord s = v.staging();
                s.setBatchId(batch.getBatchId());
                s.setNormalizedData(json(Map.of("assignedSalesEmployeeNo", v.assignedEmployeeNo() == null ? "" : v
                    .assignedEmployeeNo())));
                Long customer = s.getBusinessRegistrationNo() == null ? null :
                repository.customer(s.getBusinessRegistrationNo());
                Long sales = v.assignedEmployeeNo() == null ? null :
                repository.sales(v.assignedEmployeeNo());
                s.setExistingCustomerId(customer);
                s.setAssignedSalesId(sales);
                if (sales == null) v.errors().add(new MigrationErrorRecord(batch.getBatchId(), rowNo,
                    "assigned_sales_employee_no", "SALES_EMPLOYEE_NOT_FOUND", "활성 SALES 역할 사번을 찾을 수 없습니다.",
                    v.assignedEmployeeNo()));
                s.setValidationStatus(v.errors().isEmpty() ? MigrationValidationStatus.VALID : MigrationValidationStatus
                    .INVALID);
                s.setOperationType(s.getValidationStatus() == MigrationValidationStatus.VALID ? (customer == null ? MigrationOperationType
                    .INSERT : MigrationOperationType.UPDATE) : MigrationOperationType.NONE);
                for (MigrationErrorRecord e : v.errors()) e.setBatchId(batch.getBatchId());
                staging.add(s);
                errors.addAll(v.errors());
            }
            tx.stage(batch.getBatchId(), staging, errors, properties.getChunkSize());
            int failure = (int) errors.stream().map(MigrationErrorRecord::getRowNo).distinct().count();
            int success = tx.apply(batch.getBatchId(), actor);
            tx.finish(batch.getBatchId(), total, success, failure);
            audit.safeRecordIndependent(actor, "CUSTOMER_MIGRATION", "MIGRATION_BATCH", String.valueOf(batch
                .getBatchId()), null, Map.of("total", total, "success", success, "failure", failure));
            return get(batch.getBatchId());
        } catch (RuntimeException e) {
            tx.fail(batch.getBatchId(), total, total);
            throw e;
        }
    }
    @Transactional(readOnly = true)
    public PageResponse<MigrationBatchResponse> search(MigrationBatchStatus status, int page, int size) {
        if (page<0 || size<1 || size> 100) throw new BusinessException(ErrorCode.INVALID_REQUEST);
        return PageResponse.of(repository.page(status, page * size, size).stream().map(MigrationBatchResponse::new)
            .toList(), page, size, repository.count(status));
    }
    @Transactional(readOnly = true)
    public MigrationDetailResponse get(Long id) {
        MigrationBatchRecord b = repository.find(id).orElseThrow(() -> new BusinessException(ErrorCode
            .MIGRATION_BATCH_NOT_FOUND));
        return new MigrationDetailResponse(new MigrationBatchResponse(b), repository.errors(id).stream()
            .map(MigrationErrorResponse::new).toList());
    }
    @Transactional(readOnly = true)
    public byte[] errorCsv(Long id) {
        MigrationDetailResponse d = get(id);
        StringBuilder b = new StringBuilder("row_no,field_name,error_code,error_message,raw_value\r\n");
        for (MigrationErrorResponse e : d.errors()) b.append(e.rowNo()).append(',').append(csv(e.fieldName()))
            .append(',').append(csv(e.errorCode())).append(',').append(csv(e.errorMessage())).append(',')
            .append(csv(safeSpreadsheet(e.rawValue()))).append("\r\n");
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }
    private byte[] validateFile(MultipartFile f) {
        if (f == null || f.isEmpty()) throw new BusinessException(ErrorCode.INVALID_MIGRATION_FILE, "CSV 파일을 선택해 주세요.");
        String n = safeName(f.getOriginalFilename());
        if (!n.toLowerCase(Locale.ROOT).endsWith(".csv")) throw new BusinessException(ErrorCode.INVALID_MIGRATION_FILE,
            ".csv 확장자만 업로드할 수 있습니다.");
        if (f.getSize()> properties.getMaxFileSize().toBytes()) throw new BusinessException(ErrorCode
            .FILE_SIZE_EXCEEDED);
        try {
            return f.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_MIGRATION_FILE, "업로드 파일을 읽을 수 없습니다.");
        }
    }
    private String decode(byte[] b) {
        try {
            CharsetDecoder d = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction
                .REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            return d.decode(ByteBuffer.wrap(b)).toString();
        } catch (CharacterCodingException e) {
            throw new BusinessException(ErrorCode.INVALID_MIGRATION_FILE, "CSV는 UTF-8 인코딩이어야 합니다.");
        }
    }
    private String sha(byte[] b) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    private String safeName(String n) {
        if (n == null) return "customers.csv";
        String v = Paths.get(n).getFileName().toString();
        return v.length() <= 255 ? v :
        v.substring(v.length() - 255);
    }
    private String json(Object v) {
        try {
            return mapper.writeValueAsString(v);
        } catch (Exception e) {
            return "{}";
        }
    }
    private String safeSpreadsheet(String v) {
        if (v == null || v.isEmpty()) return v;
        char c = v.charAt(0);
        return c == '=' || c == '+' || c == '-' || c == '@' ? "'" + v :
        v;
    }
    private String csv(String v) {
        String n = v == null ? "" :
        v;
        return "\"" + n.replace("\"", "\"\"") + "\"";
    }
}
