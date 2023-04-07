package be.cytomine.config;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import be.cytomine.config.security.ApiKeyFilter;
import be.cytomine.config.security.JWTConfigurer;
import be.cytomine.repository.security.SecUserRepository;
import be.cytomine.security.AjaxLogoutSuccessHandler;
import be.cytomine.security.DomainUserDetailsService;
import be.cytomine.security.SwitchUserFailureHandler;
import be.cytomine.security.SwitchUserSuccessHandler;
import be.cytomine.security.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.EmbeddedLdapServerContextSourceFactoryBean;
import org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration {

    private final TokenProvider tokenProvider;

    private final DomainUserDetailsService domainUserDetailsService;

    private final SecUserRepository secUserRepository;

    @Value("${application.authentication.jwt.token-validity-in-seconds}")
    Long tokenValidityInSeconds;


    public SecurityConfiguration(TokenProvider tokenProvider, DomainUserDetailsService domainUserDetailsService, SecUserRepository secUserRepository) {
        this.tokenProvider = tokenProvider;
        this.domainUserDetailsService = domainUserDetailsService;
        this.secUserRepository = secUserRepository;
    }

    @Bean
    public SwitchUserFilter switchUserFilter() {
        SwitchUserFilter filter = new SwitchUserFilter();
        filter.setUserDetailsService(domainUserDetailsService);
        filter.setSuccessHandler(switchUserSuccessHandler());
        filter.setFailureHandler(switchUserFailureHandler());
        filter.setUsernameParameter("username");
        filter.setSwitchUserUrl("/api/login/impersonate");
        return filter;
    }

    @Bean
    public SwitchUserSuccessHandler switchUserSuccessHandler() {
        return new SwitchUserSuccessHandler(tokenProvider, tokenValidityInSeconds);
    }

    @Bean
    public SwitchUserFailureHandler switchUserFailureHandler() {
        return new SwitchUserFailureHandler();
    }

    @Bean
    public AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler() {
        return new AjaxLogoutSuccessHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MessageDigestPasswordEncoder("SHA-256");
    }

    @Bean
    public EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean() {
        EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean = EmbeddedLdapServerContextSourceFactoryBean.fromEmbeddedLdapServer();
        contextSourceFactoryBean.setPort(0);
        return contextSourceFactoryBean;
    }

    @Bean
    public AuthenticationManager ldapAuthenticationManager(BaseLdapPathContextSource contextSource) {
        LdapPasswordComparisonAuthenticationManagerFactory factory = new LdapPasswordComparisonAuthenticationManagerFactory(contextSource, passwordEncoder());
        return factory.createAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers(HttpMethod.OPTIONS, "/**")
            .requestMatchers("/app/**/*.{js,html}")
            .requestMatchers("/i18n/**")
            .requestMatchers("/content/**")
            .requestMatchers("/h2-console/**")
            .requestMatchers("/test/**");
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf()
            .disable()
            .addFilterBefore(new ApiKeyFilter(domainUserDetailsService, secUserRepository), BasicAuthenticationFilter.class)
            .exceptionHandling().authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            .and()
            .authorizeRequests()
            .requestMatchers("/api/authenticate").permitAll()
            .requestMatchers("/api/register").permitAll()
            .requestMatchers("/api/activate").permitAll()
            .requestMatchers("/api/account/resetPassword/init").permitAll()
            .requestMatchers("/api/account/resetPassword/finish").permitAll()
            .requestMatchers("/api/login/impersonate*").hasAuthority("ROLE_ADMIN")
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/session/admin/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/server/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/server/**").permitAll()
            .requestMatchers("/**").permitAll()
            .and()
            .apply(securityConfigurerAdapter())
            .and()
            .addFilter(switchUserFilter())
            .headers()
            .cacheControl().disable();

        return http.build();
    }
}
