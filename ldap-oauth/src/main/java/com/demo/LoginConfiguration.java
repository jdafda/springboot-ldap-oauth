package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * Created by jdafda on 8/2/16.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${ldap.domain}")
    private String DOMAIN;

    @Value("${ldap.url}")
    private String URL;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure();
        http.requestMatchers().regexMatchers("/login", "/login.+", "/oauth/.+", "/j_spring_security_check", "/logout");
        AuthenticationEntryPoint entryPoint = entryPoint();
        http.exceptionHandling().authenticationEntryPoint(entryPoint);
        http.formLogin();
        http.addFilter(usernamePasswordAuthenticationFilter());
        http.authorizeRequests().antMatchers("/login").permitAll();
        http.authorizeRequests().antMatchers("/oauth/**").authenticated();
        http.authorizeRequests().antMatchers("/j_spring_security_check").anonymous().and().csrf().disable();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
        authManagerBuilder.parentAuthenticationManager(authenticationManager());
    }

    protected AuthenticationManager authenticationManager() {
        return new ProviderManager(Arrays.asList(activeDirectoryLdapAuthenticationProvider()));
    }

    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(DOMAIN, URL);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        return provider;
    }

    private AuthenticationEntryPoint entryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/login");
    }

    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/login?login_error=true");
        filter.setAuthenticationFailureHandler(failureHandler);
        return filter;
    }
}
