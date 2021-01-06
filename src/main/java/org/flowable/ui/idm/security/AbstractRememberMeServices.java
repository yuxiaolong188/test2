//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.flowable.ui.idm.security;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Slf4j
public abstract class AbstractRememberMeServices implements RememberMeServices, InitializingBean, LogoutHandler {
    public static final String SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY = "remember-me";
    public static final String DEFAULT_PARAMETER = "remember-me";
    public static final int TWO_WEEKS_S = 1209600;
    private static final String DELIMITER = ":";
    // protected final Log log = LogFactory.getLog(this.getClass());
    protected final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private UserDetailsService userDetailsService;
    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private String cookieName = "remember-me";
    private String cookieDomain;
    private String parameter = "remember-me";
    private boolean alwaysRemember;
    private String key;
    private int tokenValiditySeconds = 1209600;
    private Boolean useSecureCookie = null;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    protected AbstractRememberMeServices(String key, UserDetailsService userDetailsService) {
        Assert.hasLength(key, "key cannot be empty or null");
        Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
        this.key = key;
        this.userDetailsService = userDetailsService;
    }

    public void afterPropertiesSet() {
        Assert.hasLength(this.key, "key cannot be empty or null");
        Assert.notNull(this.userDetailsService, "A UserDetailsService is required");
    }

    public final Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String rememberMeCookie = this.extractRememberMeCookie(request);
        if(rememberMeCookie == null) {
            return null;
        } else {
            log.debug("Remember-me cookie detected");
            if(rememberMeCookie.length() == 0) {
                log.debug("Cookie was empty");
                this.cancelCookie(request, response);
                return null;
            } else {
                UserDetails user = null;

                try {
                    String[] cookieTokens = this.decodeCookie(rememberMeCookie);
                    user = this.processAutoLoginCookie(cookieTokens, request, response);
                    this.userDetailsChecker = new AccountStatusUserDetailsChecker();
                    this.userDetailsChecker.check(user);
                    log.debug("Remember-me cookie accepted");
                    return this.createSuccessfulAuthentication(request, user);
                } catch (CookieTheftException var6) {
                    this.cancelCookie(request, response);
                    throw var6;
                } catch (UsernameNotFoundException var7) {
                    log.debug("Remember-me login was valid but corresponding user not found.", var7);
                } catch (InvalidCookieException var8) {
                    log.debug("Invalid remember-me cookie: " + var8.getMessage());
                } catch (AccountStatusException var9) {
                    log.debug("Invalid UserDetails: " + var9.getMessage());
                } catch (RememberMeAuthenticationException var10) {
                    log.debug(var10.getMessage());
                }

                this.cancelCookie(request, response);
                return null;
            }
        }
    }

    protected String extractRememberMeCookie(HttpServletRequest request) {
        try {
            // 兼容TOKEN
            // 如果带TOKEN则走TOKEN流程
            // Cookie: FLOWABLE_REMEMBER_ME=am11NnBjZDNXb0R0blhxZ1VWZUk5QSUzRCUzRDpEb0pqZ053Ym1WT2RadVlwNEtvcEdnJTNEJTNE; Admin-Token=admin-token
            String token = request.getHeader("token");
            if (!StringUtils.isEmpty(token)) {
                String[] tokenItems = token.split(",");
                for (String tokenItem : tokenItems) {
                    tokenItem = tokenItem.trim();
                    if (tokenItem.contains("=")) {
                        String key = tokenItem.split("=")[0];
                        String value = tokenItem.split("=")[1];
                        if(this.cookieName.equals(key)) {
                            if (value.contains(";")) {
                                value = value.split(";")[0];
                            }
                            return value;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("TOKEN获取错误!", e);
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length != 0) {
            Cookie[] var3 = cookies;
            int var4 = cookies.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Cookie cookie = var3[var5];
                if(this.cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }

            return null;
        } else {
            return null;
        }
    }

    protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
        RememberMeAuthenticationToken auth = new RememberMeAuthenticationToken(this.key, user, this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
        auth.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return auth;
    }

    protected String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        for(int j = 0; j < cookieValue.length() % 4; ++j) {
            cookieValue = cookieValue + "=";
        }

        try {
            Base64.getDecoder().decode(cookieValue.getBytes());
        } catch (IllegalArgumentException var7) {
            throw new InvalidCookieException("Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        }

        String cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));
        String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, ":");

        for(int i = 0; i < tokens.length; ++i) {
            try {
                tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException var6) {
                log.error(var6.getMessage(), var6);
            }
        }

        return tokens;
    }

    protected String encodeCookie(String[] cookieTokens) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < cookieTokens.length; ++i) {
            try {
                sb.append(URLEncoder.encode(cookieTokens[i], StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException var5) {
                log.error(var5.getMessage(), var5);
            }

            if(i < cookieTokens.length - 1) {
                sb.append(":");
            }
        }

        String value = sb.toString();
        sb = new StringBuilder(new String(Base64.getEncoder().encode(value.getBytes())));

        while(sb.charAt(sb.length() - 1) == 61) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public final void loginFail(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Interactive login attempt was unsuccessful.");
        this.cancelCookie(request, response);
        this.onLoginFail(request, response);
    }

    protected void onLoginFail(HttpServletRequest request, HttpServletResponse response) {
    }

    public final void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        if(!this.rememberMeRequested(request, this.parameter)) {
            log.debug("Remember-me login not requested.");
        } else {
            this.onLoginSuccess(request, response, successfulAuthentication);
        }
    }

    protected abstract void onLoginSuccess(HttpServletRequest var1, HttpServletResponse var2, Authentication var3);

    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        if(this.alwaysRemember) {
            return true;
        } else {
            String paramValue = request.getParameter(parameter);
            if(paramValue != null && (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on") || paramValue.equalsIgnoreCase("yes") || paramValue.equals("1"))) {
                return true;
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Did not send remember-me cookie (principal did not set parameter '" + parameter + "')");
                }

                return false;
            }
        }
    }

    protected abstract UserDetails processAutoLoginCookie(String[] var1, HttpServletRequest var2, HttpServletResponse var3) throws RememberMeAuthenticationException, UsernameNotFoundException;

    protected void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Cancelling cookie");
        Cookie cookie = new Cookie(this.cookieName, (String)null);
        cookie.setMaxAge(0);
        cookie.setPath(this.getCookiePath(request));
        if(this.cookieDomain != null) {
            cookie.setDomain(this.cookieDomain);
        }

        response.addCookie(cookie);
    }

    protected void setCookie(String[] tokens, int maxAge, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = this.encodeCookie(tokens);
        Cookie cookie = new Cookie(this.cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath(this.getCookiePath(request));
        if(this.cookieDomain != null) {
            cookie.setDomain(this.cookieDomain);
        }

        if(maxAge < 1) {
            cookie.setVersion(1);
        }

        if(this.useSecureCookie == null) {
            cookie.setSecure(request.isSecure());
        } else {
            cookie.setSecure(this.useSecureCookie.booleanValue());
        }

        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0?contextPath:"/";
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if(log.isDebugEnabled()) {
            log.debug("Logout of user " + (authentication == null?"Unknown":authentication.getName()));
        }

        this.cancelCookie(request, response);
    }

    public void setCookieName(String cookieName) {
        Assert.hasLength(cookieName, "Cookie name cannot be empty or null");
        this.cookieName = cookieName;
    }

    public void setCookieDomain(String cookieDomain) {
        Assert.hasLength(cookieDomain, "Cookie domain cannot be empty or null");
        this.cookieDomain = cookieDomain;
    }

    protected String getCookieName() {
        return this.cookieName;
    }

    public void setAlwaysRemember(boolean alwaysRemember) {
        this.alwaysRemember = alwaysRemember;
    }

    public void setParameter(String parameter) {
        Assert.hasText(parameter, "Parameter name cannot be empty or null");
        this.parameter = parameter;
    }

    public String getParameter() {
        return this.parameter;
    }

    protected UserDetailsService getUserDetailsService() {
        return this.userDetailsService;
    }

    public String getKey() {
        return this.key;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    protected int getTokenValiditySeconds() {
        return this.tokenValiditySeconds;
    }

    public void setUseSecureCookie(boolean useSecureCookie) {
        this.useSecureCookie = Boolean.valueOf(useSecureCookie);
    }

    protected AuthenticationDetailsSource<HttpServletRequest, ?> getAuthenticationDetailsSource() {
        return this.authenticationDetailsSource;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource cannot be null");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        this.userDetailsChecker = userDetailsChecker;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }
}
