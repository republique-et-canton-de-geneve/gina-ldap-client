package gina.impl.util;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class GinaLdapConfiguration {

    /**
     * Type d'acc�s au LDAP.
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ldapServerUrl", ldapServerUrl)
                .append("ldapUser", ldapUser)
                .append("ldapPassword", "***")
                .append("ldapBaseDn", ldapBaseDn)
                .append("ldapType", ldapType)
                .toString();
    }

}
