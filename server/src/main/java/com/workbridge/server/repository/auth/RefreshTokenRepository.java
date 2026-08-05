package com.workbridge.server.repository.auth;

import com.workbridge.server.domain.auth.RefreshTokenRecord;
import com.workbridge.server.mapper.auth.RefreshTokenMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class RefreshTokenRepository {
    private final RefreshTokenMapper mapper;

    public RefreshTokenRepository(RefreshTokenMapper mapper) {
        this.mapper = mapper;
    }

    public Long insert(RefreshTokenRecord t) {
        mapper.insert(t);
        return t.getRefreshTokenId();
    }

    public Optional<RefreshTokenRecord> findActiveForUpdate(String hash, LocalDateTime now) {
        return Optional.ofNullable(mapper.selectActiveForUpdate(hash, now));
    }

    public boolean rotate(Long id, Long replacement, LocalDateTime now) {
        return mapper.rotate(id, replacement, now) == 1;
    }

    public boolean revoke(String hash, Long employeeId, LocalDateTime now) {
        return mapper.revoke(hash, employeeId, now) == 1;
    }

    public int revokeAll(Long employeeId, LocalDateTime now) {
        return mapper.revokeAll(employeeId, now);
    }
}
