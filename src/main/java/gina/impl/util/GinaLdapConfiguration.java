/*
 * GINA LDAP client
 *
 * Copyright 2016-2019 Republique et canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gina.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static gina.impl.util.GinaLdapUtils.DEFAULT_LDAP_CONNECTION_TIMEOUT;
import static gina.impl.util.GinaLdapUtils.DEFAULT_LDAP_READ_TIMEOUT;

/**
 * Bean (DTO) contenant toutes les informations necessaires a l'acces au VLDAP.
 */
public class GinaLdapConfiguration {

    // Configuration du LDAP
    public static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String LDAP_AUTHENTICATION_MODE = "simple";
    public static final String LDAP_REFERRAL_MODE = "follow";

    private final String ldapServerUrl;

    private final String ldapUser;

    private final String ldapPassword;

    private final int ldapConnectionTimeout;

    private final int ldapReadTimeout;

    private final String ginaDomain;

    private final String ginaApplication;

    public GinaLdapConfiguration(
            String server,
            String user,
            String password,
            String domain,
            String application) {
        this(
            server,
            user,
            password,
            domain,
            application,
            DEFAULT_LDAP_CONNECTION_TIMEOUT,
            DEFAULT_LDAP_READ_TIMEOUT);
    }

    public GinaLdapConfiguration(
            String server,
            String user,
            String password,
            String domain,
            String application,
            int ldapReadTimeout) {
        this(
            server,
            user,
            password,
            domain,
            application,
            DEFAULT_LDAP_CONNECTION_TIMEOUT,
            ldapReadTimeout);
    }

    public GinaLdapConfiguration(
            String server,
            String user,
            String password,
            String domain,
            String application,
            int ldapConnectionTimeout,
            int ldapReadTimeout) {
        Validate.notEmpty(server, "server");
        Validate.notNull(domain, "ginaDomain");

        this.ldapServerUrl = server;
        this.ldapUser = user;
        this.ldapPassword = password;
        this.ginaDomain = domain;
        this.ginaApplication = application;
        this.ldapConnectionTimeout = ldapConnectionTimeout;
        this.ldapReadTimeout = ldapReadTimeout;
    }

    public String getLdapServerUrl() {
        return ldapServerUrl;
    }

    public String getLdapUser() {
        return ldapUser;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public int getLdapConnectionTimeout() {
        return ldapConnectionTimeout;
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

    public String getGinaDomain() {
        return ginaDomain;
    }

    public String getGinaApplication() {
        return ginaApplication;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ldapServerUrl", ldapServerUrl)
                .append("ldapUser", ldapUser)
                .append("ldapPassword", StringUtils.isBlank(ldapPassword) ? "<MISSING>" : "***")
                .append("ldapConnectionTimeout", ldapConnectionTimeout)
                .append("ldapReadTimeout", ldapReadTimeout)
                .append("ginaDomain", ginaDomain)
                .append("ginaApplication", ginaApplication)
                .toString();
    }

}
