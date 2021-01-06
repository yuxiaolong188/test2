//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.flowable.ui.idm.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.idm.api.Token;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.flowable.ui.common.security.FlowableAppUser;
import org.flowable.ui.idm.properties.FlowableIdmAppProperties;
import org.flowable.ui.idm.properties.FlowableIdmAppProperties.Cookie;
import org.flowable.ui.idm.service.PersistentTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

@Service
public class CustomPersistentRememberMeServices extends AbstractRememberMeServices implements CustomRememberMeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPersistentRememberMeServices.class);
    @Autowired
    private PersistentTokenService persistentTokenService;
    @Autowired
    private CustomUserDetailService customUserDetailService;
    @Autowired
    private IdmIdentityService identityService;
    private final String tokenDomain;
    private final int tokenMaxAgeInSeconds;
    private final long tokenMaxAgeInMilliseconds;
    private final long tokenRefreshDurationInMilliseconds;

    @Autowired
    public CustomPersistentRememberMeServices(FlowableIdmAppProperties properties, UserDetailsService userDetailsService) {
        super(properties.getSecurity().getRememberMeKey(), userDetailsService);
        this.setAlwaysRemember(true);
        Cookie cookie = properties.getSecurity().getCookie();
        this.tokenMaxAgeInSeconds = cookie.getMaxAge();
        LOGGER.info("Cookie max-age set to {} seconds", Integer.valueOf(this.tokenMaxAgeInSeconds));
        this.tokenMaxAgeInMilliseconds = (long)this.tokenMaxAgeInSeconds * 1000L;
        String domain = cookie.getDomain();
        if(domain != null) {
            LOGGER.info("Cookie domain set to {}", domain);
        }

        this.tokenDomain = domain;
        int tokenRefreshSeconds = cookie.getRefreshAge();
        LOGGER.info("Cookie refresh age set to {} seconds", Integer.valueOf(tokenRefreshSeconds));
        this.tokenRefreshDurationInMilliseconds = (long)cookie.getRefreshAge() * 1000L;
        this.setCookieName("FLOWABLE_REMEMBER_ME");
    }

    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        String userEmail = successfulAuthentication.getName();
        LOGGER.debug("Creating new persistent login for user {}", userEmail);
        FlowableAppUser appUser = (FlowableAppUser)successfulAuthentication.getPrincipal();
        Token token = this.createAndInsertPersistentToken(appUser.getUserObject(), request.getRemoteAddr(), request.getHeader("User-Agent"));
        this.addCookie(token, request, response);
    }

    @Transactional
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {
        Token token = this.getPersistentToken(cookieTokens);
        if((new Date()).getTime() - token.getTokenDate().getTime() > this.tokenRefreshDurationInMilliseconds) {
            try {
                token = this.persistentTokenService.createToken((User)((UserQuery)this.identityService.createUserQuery().userId(token.getUserId())).singleResult(), request.getRemoteAddr(), request.getHeader("User-Agent"));
                this.addCookie(token, request, response);
            } catch (DataAccessException var6) {
                LOGGER.error("Failed to update token: ", var6);
                throw new RememberMeAuthenticationException("Autologin failed due to data access problem: " + var6.getMessage());
            }
        }

        return this.customUserDetailService.loadByUserId(token.getUserId());
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String rememberMeCookie = this.extractRememberMeCookie(request);
        if(rememberMeCookie != null && rememberMeCookie.length() != 0) {
            try {
                String[] cookieTokens = this.decodeCookie(rememberMeCookie);
                Token token = this.getPersistentToken(cookieTokens);
                this.persistentTokenService.delete(token);
            } catch (InvalidCookieException var7) {
                LOGGER.info("Invalid cookie, no persistent token could be deleted");
            } catch (RememberMeAuthenticationException var8) {
                LOGGER.debug("No persistent token found, so no token could be deleted");
            }
        }

        super.logout(request, response, authentication);
    }

    private Token getPersistentToken(String[] cookieTokens) {
        if(cookieTokens.length != 2) {
            throw new InvalidCookieException("Cookie token did not contain 2 tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
        } else {
            String presentedSeries = cookieTokens[0];
            String presentedToken = cookieTokens[1];
            Token token = this.persistentTokenService.getPersistentToken(presentedSeries);
            if(token == null) {
                throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
            } else {
                if(!presentedToken.equals(token.getTokenValue())) {
                    token = this.persistentTokenService.getPersistentToken(presentedSeries, true);
                    if(token != null && !presentedToken.equals(token.getTokenValue())) {
                        if(token != null) {
                            this.persistentTokenService.delete(token);
                        }

                        throw new CookieTheftException("Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.");
                    }
                }

                if((new Date()).getTime() - token.getTokenDate().getTime() > this.tokenMaxAgeInMilliseconds) {
                    throw new RememberMeAuthenticationException("Remember-me login has expired");
                } else {
                    return token;
                }
            }
        }
    }

    private void addCookie(Token token, HttpServletRequest request, HttpServletResponse response) {
        this.setCookie(new String[]{token.getId(), token.getTokenValue()}, this.tokenMaxAgeInSeconds, request, response);
    }

    protected void setCookie(String[] tokens, int maxAge, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = this.encodeCookie(tokens);
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(this.getCookieName(), cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        if(this.tokenDomain != null) {
            cookie.setDomain(this.tokenDomain);
        }

        String xForwardedProtoHeader = request.getHeader("X-Forwarded-Proto");
        if(xForwardedProtoHeader != null) {
            cookie.setSecure(xForwardedProtoHeader.equals("https") || request.isSecure());
        } else {
            cookie.setSecure(request.isSecure());
        }

        Method setHttpOnlyMethod = ReflectionUtils.findMethod(javax.servlet.http.Cookie.class, "setHttpOnly", new Class[]{Boolean.TYPE});
        if(setHttpOnlyMethod != null) {
            ReflectionUtils.invokeMethod(setHttpOnlyMethod, cookie, new Object[]{Boolean.TRUE});
        }

        response.addCookie(cookie);
    }

    public Token createAndInsertPersistentToken(User user, String remoteAddress, String userAgent) {
        return this.persistentTokenService.createToken(user, remoteAddress, userAgent);
    }
}
