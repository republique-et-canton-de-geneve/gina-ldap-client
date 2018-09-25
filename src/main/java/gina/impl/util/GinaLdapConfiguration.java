package gina.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaLdapConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapConfiguration.class);

//    public static final String DOMAIN = "domain";
//    public static final String APPLICATION = "application";

    /**
     * Type d'accès au LDAP.
     */
    public enum Type {
        DOMAIN,
        APPLICATION;
    }

    // Configuration du LDAP
    public static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    public static final String LDAP_AUTHENTICATION_MODE = "simple";

    public static final String LDAP_REFERRAL_MODE = "follow";

    private String ldapServerUrl;

    private String ldapBaseDn;

    private String ldapUser;

    private String ldapPassword;

    private int ldapTimeLimit = GinaLdapUtils.LDAP_DEFAULT_TIMEOUT;

    private Type ldapType;

    /*
    public GinaLdapConfiguration(final String server, final String base) {
        this(server, base, null, null, GinaLdapUtils.LDAP_DEFAULT_TIMEOUT);
    }

    public GinaLdapConfiguration(final String server, final String base, final String user, final String password) {
        this(server, base, user, password, GinaLdapUtils.LDAP_DEFAULT_TIMEOUT);
    }
    */

    public GinaLdapConfiguration(
            String server, String base, String user, String password, GinaLdapConfiguration.Type type, int timeLimit) {
        Validate.notEmpty(server, "server");
        Validate.notEmpty(base, "base");
        Validate.notNull(user, "user");
        Validate.notNull(password, "password");
        Validate.notNull(type, "type");

        this.ldapServerUrl = server;
        this.ldapBaseDn = base;
        this.ldapUser = user;
        this.ldapPassword = password;
        this.ldapTimeLimit = timeLimit;

        /*
        int count = StringUtils.countMatches(user, ",ou=");
        LOGGER.debug("count = ", count);
        if (count > 2) {
            this.ldapType = Type.APPLICATION;
        } else {
            this.ldapType = Type.DOMAIN;
        }
        */
        this.ldapType = type;
    }

    public String getLdapServerUrl() {
        return ldapServerUrl;
    }

    public void setLdapServerUrl(String ldapServerUrl) {
        this.ldapServerUrl = ldapServerUrl;
    }

    public String getLdapBaseDn() {
        return ldapBaseDn;
    }

    public void setLdapBaseDn(String ldapBaseDn) {
        this.ldapBaseDn = ldapBaseDn;
    }

    public String getLdapUser() {
        return ldapUser;
    }

    public void setLdapUser(String ldapUser) {
        this.ldapUser = ldapUser;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = ldapPassword;
    }

    public int getLdapTimeLimit() {
        return ldapTimeLimit;
    }

    public void setLdapTimeLimit(int ldapTimeLimit) {
        this.ldapTimeLimit = ldapTimeLimit;
    }

    public Type getLdapType() {
        return ldapType;
    }

    public void setLdapType(Type ldapType) {
        this.ldapType = ldapType;
    }

}
