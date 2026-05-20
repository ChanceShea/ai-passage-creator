package com.shea.aipassagecreator.config;

import com.shea.aipassagecreator.constant.UserConstant;
import com.shea.aipassagecreator.domain.entity.User;
import com.shea.aipassagecreator.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * @author : Shea.
 * @since : 2026/5/20 10:16
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final IUserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/oauth2/**", "/login/**","/v3/api-docs").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            // 只调用一次，获取用户对象
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            User user = userService.handleGithubLogin(oAuth2User);

                            // 存入 session
                            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

                            // 重定向到前端
                            response.sendRedirect("http://localhost:5173");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("http://localhost:5173");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            request.getSession().invalidate();
                            response.sendRedirect("http://localhost:5173");
                        })
                );

        return http.build();
    }
}