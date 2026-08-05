package com.workbridge.server.service.auth;

import com.workbridge.server.config.AuthenticationProperties;
import com.workbridge.server.domain.organization.EmployeeCredentialRecord;
import com.workbridge.server.repository.organization.EmployeeRepository;
import com.workbridge.server.security.WorkBridgePrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmployeeAuthenticationService {

    private static final String DUMMY_BCRYPT_HASH =
            "$2y$10$kxIhQFtMMcXM6G4SWmvv6uN0m3ULIwUBCK.Sbg8jw71Ed6TFrgRGW";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProperties properties;
    private final Clock clock;

    public EmployeeAuthenticationService(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationProperties properties,
            Clock clock
    ) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = AuthenticationException.class)
    public WorkBridgePrincipal authenticate(String rawLoginId, String rawPassword) {
        String loginId = normalizeLoginId(rawLoginId);
        String password = rawPassword == null ? "" : rawPassword;
        Optional<EmployeeCredentialRecord> optionalCredential = employeeRepository.findCredentialByLoginId(loginId);

        if (optionalCredential.isEmpty()) {
            passwordEncoder.matches(password, DUMMY_BCRYPT_HASH);
            throw new BadCredentialsException("인증 정보가 일치하지 않습니다.");
        }

        EmployeeCredentialRecord credential = optionalCredential.get();
        LocalDateTime now = LocalDateTime.now(clock);

        if (credential.getLockedUntil() != null && !credential.getLockedUntil().isAfter(now)) {
            employeeRepository.clearExpiredLock(credential.getEmployeeId(), now);
        }

        if (!credential.isActive()) {
            throw new DisabledException("비활성 계정입니다.");
        }
        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(now)) {
            throw new LockedException("로그인 실패로 잠긴 계정입니다.");
        }

        if (!passwordEncoder.matches(password, credential.getPasswordHash())) {
            employeeRepository.recordLoginFailure(
                    credential.getEmployeeId(),
                    properties.getMaxFailures(),
                    now.plus(properties.getLockDuration()),
                    now
            );
            throw new BadCredentialsException("인증 정보가 일치하지 않습니다.");
        }

        List<String> roleCodes = employeeRepository.findRoleCodes(credential.getEmployeeId());
        if (roleCodes.isEmpty()) {
            throw new DisabledException("활성 역할이 없는 계정입니다.");
        }

        employeeRepository.recordLoginSuccess(credential.getEmployeeId(), now);
        return new WorkBridgePrincipal(
                credential.getEmployeeId(),
                credential.getEmployeeNo(),
                credential.getLoginId(),
                credential.getEmployeeName(),
                credential.getEmail(),
                credential.getDepartmentId(),
                credential.getDepartmentName(),
                roleCodes
        );
    }

    private String normalizeLoginId(String loginId) {
        if (loginId == null) {
            return "";
        }
        return loginId.trim().toLowerCase(Locale.ROOT);
    }
}
