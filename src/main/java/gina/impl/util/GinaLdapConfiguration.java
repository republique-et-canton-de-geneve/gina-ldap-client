package gina.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import static gina.impl.util.GinaLdapUtils.DEFAULT_LDAP_CONNECTION_TIMEOUT;
import static gina.impl.util.GinaLdapUtils.DEFAULT_LDAP_READ_TIMEOUT;

public class GinaLdapConfiguration {

    /**
     * Type d'acces au LDAP.
     */
    public enum Type {
        DOMAIN,
        APPLICATION
    }

    // Configuration du LDAP
    public static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    public static final String LDAP_AUTHENTICATION_MODE = "simple";

    public static final String LDAP_REFERRAL_MODE = "follow";

    private String ldapServerUrl;

    private String ldapBaseDn;

    private String ldapUser;

    private String ldapPassword;

    private int ldapConnectionTimeout;

    private int ldapReadTimeout;

    private Type ldapType;

    public GinaLdapConfiguration(
            String server,
            String base,
            String user,
            String password,
            GinaLdapConfiguration.Type type) {
        this(
            server,
            base,
            user,
            password,
            type,
            DEFAULT_LDAP_CONNECTION_TIMEOUT,
            DEFAULT_LDAP_READ_TIMEOUT);
    }

    public GinaLdapConfiguration(
            String server,
            String base,
            String user,
            String password,
            GinaLdapConfiguration.Type type,
            int ldapReadTimeout) {
        this(
            server,
            base,
            user,
            password,
            type,
            DEFAULT_LDAP_CONNECTION_TIMEOUT,
            ldapReadTimeout);
    }

    public GinaLdapConfiguration(
            String server,
            String base,
            String user,
            String password,
            GinaLdapConfiguration.Type type,
            int ldapConnectionTimeout,
            int ldapReadTimeout) {
        Validate.notEmpty(server, "server");
        Validate.notEmpty(base, "base");
        Validate.notNull(user, "user");
        Validate.notNull(password, "password");
        Validate.notNull(type, "type");

        this.ldapServerUrl = server;
        this.ldapBaseDn = base;
        this.ldapUser = user;
        this.ldapPassword = password;
        this.ldapType = type;
        this.ldapConnectionTimeout = ldapConnectionTimeout;
        this.ldapReadTimeout = ldapReadTimeout;
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

    public int getLdapConnectionTimeout() {
        return ldapConnectionTimeout;
    }

    public void setLdapConnectionTimeout(int ldapConnectionTimeout) {
        this.ldapConnectionTimeout = ldapConnectionTimeout;
    }

    /**
     * Depuis janv. 2019, utiliser {@link #getLdapReadTimeout()}.
     */
    @Deprecated
    public int getLdapTimeLimit() {
        return getLdapReadTimeout();
    }

    public int getLdapReadTimeout() {
        return ldapReadTimeout;
    }

    public void setLdapReadTimeout(int ldapReadTimeout) {
        this.ldapReadTimeout = ldapReadTimeout;
    }

    public Type getLdapType() {
        return ldapType;
    }

    public void setLdapType(Type ldapType) {
        this.ldapType = ldapType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ldapServerUrl", ldapServerUrl)
                .append("ldapUser", ldapUser)
                .append("ldapPassword", StringUtils.isBlank(ldapPassword) ? "<MISSING>" : "***")
                .append("ldapBaseDn", ldapBaseDn)
                .append("ldapType", ldapType)
                .append("ldapConnectionTimeout", ldapConnectionTimeout)
                .append("ldapReadTimeout", ldapReadTimeout)
                .toString();
    }

}
