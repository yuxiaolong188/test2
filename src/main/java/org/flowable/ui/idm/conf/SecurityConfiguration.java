package org.flowable.ui.idm.conf;

import com.cims.bpm.security.advice.decrypt.Sm4Decrypt;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.ui.common.properties.FlowableRestAppProperties;
import org.flowable.ui.common.security.ActuatorRequestMatcher;
import org.flowable.ui.common.security.ClearFlowableCookieLogoutHandler;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.flowable.ui.idm.properties.FlowableIdmAppProperties;
import org.flowable.ui.idm.security.AjaxAuthenticationFailureHandler;
import org.flowable.ui.idm.security.AjaxAuthenticationSuccessHandler;
import org.flowable.ui.idm.security.AjaxLogoutSuccessHandler;
import org.flowable.ui.idm.security.CustomDaoAuthenticationProvider;
import org.flowable.ui.idm.security.CustomLdapAuthenticationProvider;
import org.flowable.ui.idm.security.CustomPersistentRememberMeServices;
import org.flowable.ui.idm.security.Http401UnauthorizedEntryPoint;
import org.flowable.ui.idm.web.CustomFormLoginConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Based on http://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/#multiple-httpsecurity
 *
 * @author Joram Barrez
 * @author Tijs Rademakers
 * @author Filip Hrisafov
 */
@Configuration(proxyBeanMethods = false)
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    //
    // GLOBAL CONFIG
    //

    @Autowired
    protected IdmIdentityService identityService;
    
    @Autowired
    protected FlowableIdmAppProperties idmAppProperties;

    @Autowired
    Sm4Decrypt sm4Decrypt;
    
    @Bean
    public org.flowable.ui.idm.security.UserDetailsService userDetailsService() {
        org.flowable.ui.idm.security.UserDetailsService userDetailsService = new org.flowable.ui.idm.security.UserDetailsService();
        userDetailsService.setUserValidityPeriod(idmAppProperties.getSecurity().getUserValidityPeriod());
        return userDetailsService;
    }

    @Bean(name = "dbAuthenticationProvider")
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    @ConditionalOnProperty(prefix = "flowable.idm.ldap", name = "enabled", havingValue = "false", matchIfMissing = true)
    public AuthenticationProvider dbAuthenticationProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        CustomDaoAuthenticationProvider daoAuthenticationProvider = new CustomDaoAuthenticationProvider();
        daoAuthenticationProvider.setSm4Decrypt(sm4Decrypt);

        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean(name = "ldapAuthenticationProvider")
    @ConditionalOnProperty(prefix = "flowable.idm.ldap", name = "enabled", havingValue = "true")
    public AuthenticationProvider ldapAuthenticationProvider(UserDetailsService userDetailsService) {
        CustomLdapAuthenticationProvider ldapAuthenticationProvider = new CustomLdapAuthenticationProvider(
                userDetailsService, identityService);
        return ldapAuthenticationProvider;
    }

    //
    // REGULAR WEBAP CONFIG
    //

    @Configuration
    @Order(10) // API config first (has Order(1))
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private FlowableIdmAppProperties idmAppProperties;

        @Autowired
        private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

        @Autowired
        private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

        @Autowired
        private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

        @Autowired
        private Http401UnauthorizedEntryPoint authenticationEntryPoint;

        @Autowired
        private RememberMeServices rememberMeServices;



        @Override
        protected void configure(HttpSecurity http) throws Exception {



            http
                    .cors().disable()
                    .cors().and()

                    .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .rememberMe()
                    .rememberMeServices(rememberMeServices)
                    .key(idmAppProperties.getSecurity().getRememberMeKey())
                    .and()
                    .logout()
                    .logoutUrl("/app/logout")
                    .logoutSuccessHandler(ajaxLogoutSuccessHandler)
                    .addLogoutHandler(new ClearFlowableCookieLogoutHandler())
                    .permitAll()


                    .and()
                    .csrf()
                    .disable() // Disabled, cause enabling it will cause sessions
                    .headers()
                    .frameOptions()
                    .sameOrigin()
                    .addHeaderWriter(new XXssProtectionHeaderWriter())
                    .and()
                    .authorizeRequests()
                    .antMatchers("/*").permitAll()
                    .antMatchers("/app/rest/authenticate").permitAll()
                    .antMatchers("/app/**").hasAuthority(DefaultPrivileges.ACCESS_IDM)
                    .antMatchers("/rest/**").hasAuthority(DefaultPrivileges.ACCESS_ADMIN);

            // Custom login form configurer to allow for non-standard HTTP-methods (eg. LOCK)
            CustomFormLoginConfig<HttpSecurity> loginConfig = new CustomFormLoginConfig<>();
            loginConfig.loginProcessingUrl("/app/authentication")
                    .successHandler(ajaxAuthenticationSuccessHandler)
                    .failureHandler(ajaxAuthenticationFailureHandler)
                    .usernameParameter("j_username")
                    .passwordParameter("j_password")
                    .permitAll();

            http.apply(loginConfig);
        }

    }

    @Bean
    public CustomPersistentRememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        return new CustomPersistentRememberMeServices(idmAppProperties, userDetailsService);
    }

    //
    // BASIC AUTH
    //

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        protected final FlowableRestAppProperties restAppProperties;
        protected final FlowableIdmAppProperties idmAppProperties;



        public ApiWebSecurityConfigurationAdapter(FlowableRestAppProperties restAppProperties,
            FlowableIdmAppProperties idmAppProperties) {
            this.restAppProperties = restAppProperties;
            this.idmAppProperties = idmAppProperties;
        }

        protected void configure(HttpSecurity http) throws Exception {

          //  http.addFilterBefore(corsFilter, AnonymousAuthenticationFilter.class);


            http
                    .cors().disable()
                    .cors().and()


                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .csrf()
                    .disable();

            if (idmAppProperties.isRestEnabled()) {

                if (restAppProperties.isVerifyRestApiPrivilege()) {
                    http.antMatcher("/api/**").authorizeRequests().antMatchers("/api/**").hasAuthority(DefaultPrivileges.ACCESS_REST_API).and().httpBasic();
                } else {
                    http.antMatcher("/api/**").authorizeRequests().antMatchers("/api/**").authenticated().and().httpBasic();
                    
                }
                // 增加接口安全访问  by huxipi
                // http.antMatcher("/rest/**").authorizeRequests().antMatchers("/rest/**").hasAnyAuthority(DefaultPrivileges.ACCESS_ADMIN).and().httpBasic();
            } else {
                http.antMatcher("/api/**").authorizeRequests().antMatchers("/api/**").denyAll();
                
            }
            
        }
    }

    //
    // Actuator
    //

    @ConditionalOnClass(EndpointRequest.class)
    @Configuration
    @Order(5) // Actuator configuration should kick in before the Form Login there should always be http basic for the endpoints
    public static class ActuatorWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {



        @Override
        protected void configure(HttpSecurity http) throws Exception {



            http

                    .cors().disable()
                    .cors().and()

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable();

            http
                .requestMatcher(new ActuatorRequestMatcher())
                .authorizeRequests()
                .requestMatchers(EndpointRequest.to(InfoEndpoint.class, HealthEndpoint.class)).authenticated()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAnyAuthority(DefaultPrivileges.ACCESS_ADMIN)
                .and().httpBasic();
        }
    }
}
