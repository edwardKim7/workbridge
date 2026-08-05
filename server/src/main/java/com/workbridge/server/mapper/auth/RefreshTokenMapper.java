package com.workbridge.server.mapper.auth;

import com.workbridge.server.domain.auth.RefreshTokenRecord;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

public interface RefreshTokenMapper {
    int insert(@Param("token") RefreshTokenRecord token);
    RefreshTokenRecord selectActiveForUpdate(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);
    int rotate(@Param("tokenId") Long tokenId, @Param("replacementId") Long replacementId, @Param("now") LocalDateTime now);
    int revoke(@Param("tokenHash") String tokenHash, @Param("employeeId") Long employeeId, @Param("now") LocalDateTime now);
    int revokeAll(@Param("employeeId") Long employeeId, @Param("now") LocalDateTime now);
}
