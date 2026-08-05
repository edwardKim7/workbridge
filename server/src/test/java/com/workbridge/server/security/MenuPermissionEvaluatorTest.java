package com.workbridge.server.security;

import com.workbridge.server.repository.organization.MenuRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuPermissionEvaluatorTest {

    @Test
    void delegatesReadAndWriteDecisionToDatabasePermissionForCurrentEmployee() {
        MenuRepository repository = mock(MenuRepository.class);
        MenuPermissionEvaluator evaluator = new MenuPermissionEvaluator(repository);
        WorkBridgePrincipal principal = new WorkBridgePrincipal(
                7L, "WB-0007", "tester", "테스터", "tester@example.com",
                1L, "본사", List.of("VIEWER")
        );
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
        when(repository.hasPermission(7L, "EMPLOYEE_ROLE", false)).thenReturn(true);
        when(repository.hasPermission(7L, "EMPLOYEE_ROLE", true)).thenReturn(false);

        assertThat(evaluator.hasRead(authentication, "EMPLOYEE_ROLE")).isTrue();
        assertThat(evaluator.hasWrite(authentication, "EMPLOYEE_ROLE")).isFalse();
        assertThat(evaluator.hasRead(null, "EMPLOYEE_ROLE")).isFalse();

        verify(repository).hasPermission(7L, "EMPLOYEE_ROLE", false);
        verify(repository).hasPermission(7L, "EMPLOYEE_ROLE", true);
    }
}
