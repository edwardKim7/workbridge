package com.workbridge.server.config;

import com.workbridge.server.security.ApiAccessDeniedHandler;
import com.workbridge.server.security.ApiAuthenticationEntryPoint;
import com.workbridge.server.security.BearerTokenAuthenticationFilter;
import com.workbridge.server.security.LoginFailureHandler;
import com.workbridge.server.security.LoginSuccessHandler;
import com.workbridge.server.security.LogoutAuditSuccessHandler;
import com.workbridge.server.security.SessionAccountStatusFilter;
import com.workbridge.server.security.WebAccessDeniedHandler;
import com.workbridge.server.security.WorkBridgeAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            WorkBridgeAuthenticationProvider authenticationProvider,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            LogoutAuditSuccessHandler logoutSuccessHandler,
            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler,
            WebAccessDeniedHandler webAccessDeniedHandler,
            SessionAccountStatusFilter sessionAccountStatusFilter,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter)
            throws Exception {

        AuthenticationEntryPoint webAuthenticationEntryPoint =
                new LoginUrlAuthenticationEntryPoint("/login");
        AntPathRequestMatcher apiRequestMatcher = new AntPathRequestMatcher("/api/**");
        RequestMatcher bearerRequestMatcher = request -> {
            String authorization = request.getHeader("Authorization");
            return authorization != null && authorization.startsWith("Bearer ");
        };

        http.authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(
                                jakarta.servlet.DispatcherType.FORWARD,
                                jakarta.servlet.DispatcherType.ERROR
                        ).permitAll()
                        .requestMatchers(
                                "/",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/sample-data/**",
                                "/favicon.ico",
                                "/api/v1/system/ping",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/actuator/health",
                                "/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint, apiRequestMatcher)
                        .authenticationEntryPoint(webAuthenticationEntryPoint)
                        .defaultAccessDeniedHandlerFor(
                                apiAccessDeniedHandler, apiRequestMatcher)
                        .accessDeniedHandler(webAccessDeniedHandler))
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.migrateSession()))
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/api/v1/auth/login", "POST"),
                        new AntPathRequestMatcher("/api/v1/auth/refresh", "POST"),
                        bearerRequestMatcher))
                .addFilterBefore(
                        bearerTokenAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .addFilterAfter(
                        sessionAccountStatusFilter, AnonymousAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
