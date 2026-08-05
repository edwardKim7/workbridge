package com.workbridge.mockerp;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MockErpService {

    private static final long TIMEOUT_DELAY_MILLIS = 6_000L;

    private final ConcurrentMap<String, StoredSalesDocument> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(100_000);

    public ErpSalesResponse process(String idempotencyKey, ErpSalesRequest request, MockMode mode) {
        if (mode == MockMode.HTTP_500) {
            throw new MockErpException(500, "MOCK_HTTP_500");
        }
        if (mode == MockMode.VALIDATION_400) {
            throw new MockErpException(400, "MOCK_VALIDATION_400");
        }
        if (mode == MockMode.TIMEOUT_BEFORE_COMMIT) {
            delayPastClientTimeout();
            throw new MockErpException(504, "MOCK_TIMEOUT_BEFORE_COMMIT");
        }

        StoredSalesDocument previous = store.get(idempotencyKey);
        if (previous != null) {
            return new ErpSalesResponse(
                    previous.documentNo(),
                    true,
                    "동일 Idempotency Key의 기존 처리 결과입니다."
            );
        }

        StoredSalesDocument candidate = new StoredSalesDocument(
                "ERP-" + sequence.incrementAndGet(),
                request
        );
        StoredSalesDocument winner = store.putIfAbsent(idempotencyKey, candidate);
        StoredSalesDocument persisted = winner == null ? candidate : winner;

        if (mode == MockMode.TIMEOUT_AFTER_COMMIT) {
            delayPastClientTimeout();
        }

        return new ErpSalesResponse(
                persisted.documentNo(),
                winner != null,
                winner == null ? "Mock ERP 저장 완료" : "동일 요청의 기존 결과 반환"
        );
    }

    public List<Map<String, Object>> records() {
        return store.entrySet().stream()
                .map(entry -> Map.<String, Object>of(
                        "idempotencyKey", entry.getKey(),
                        "documentNo", entry.getValue().documentNo(),
                        "opportunityNo", entry.getValue().request().opportunityNo()
                ))
                .toList();
    }

    public void clear() {
        store.clear();
    }

    private void delayPastClientTimeout() {
        try {
            Thread.sleep(TIMEOUT_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MockErpException(503, "MOCK_INTERRUPTED");
        }
    }

    private record StoredSalesDocument(String documentNo, ErpSalesRequest request) {
    }
}
