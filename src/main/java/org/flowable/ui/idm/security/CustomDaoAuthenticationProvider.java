package org.flowable.ui.idm.security;

import com.cims.bpm.security.advice.decrypt.Sm4Decrypt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author jbarrez
 */
@Data
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private Sm4Decrypt sm4Decrypt;

    protected void additionalAuthenticationChecks(org.springframework.security.core.userdetails.UserDetails userDetails,
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication) throws org.springframework.security.core.AuthenticationException {

        // Overriding this method to catch empty/null passwords. This happens when users are synced with LDAP sync:
        // they will have an external id, but no password (password is checked against ldap).
        //
        // The default DaoAuthenticationProvider will choke on an empty password (an arrayIndexOutOfBoundsException
        // somewhere deep in the bowels of password encryption), hence this override
        if (StringUtils.isEmpty(userDetails.getPassword())) {
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

       // super.additionalAuthenticationChecks(userDetails, authentication);
        preAdditionalAuthenticationChecks(userDetails, authentication);

    }


    //FlowableAuthenticationProvider


     void preAdditionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
         if (authentication.getCredentials() == null) {
             this.logger.debug("Authentication failed: no credentials provided");
             throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
         } else {
             String presentedPassword = authentication.getCredentials().toString();
             presentedPassword=sm4Decrypt.execute(presentedPassword);

             if (!super.getPasswordEncoder().matches(presentedPassword, userDetails.getPassword())) {
                 this.logger.debug("Authentication failed: password does not match stored value");
                 throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
             }
         }
    }


}
