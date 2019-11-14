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
package gina.impl;

import gina.api.GinaApiLdapBaseAble;
import gina.impl.jndi.GinaLdapContext;
import gina.impl.jndi.GinaLdapQuery;
import gina.impl.util.DnPart;
import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapEncoder;
import gina.impl.util.GinaLdapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Ceci est la classe principale de cette bibliotheque. Elle fournit l'acces au VLDAP Gina et les methodes
 * d'interrogation du VLDAP Gina, par exemple pour l'obtention de la liste des utilisateurs pouvant acceder
 * a une application donnee.
 */
public class GinaLdapAccess implements GinaApiLdapBaseAble {

    /**
     * Message d'erreur pour les methodes non implementees.
     */
    public static final String NOT_IMPLEMENTED = "Not implemented";

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapAccess.class);

    private static final String ROLE_UTILISATEUR = "UTILISATEUR";

    /**
     * Relative distinguished names.
     */
    private enum Rdn {
        APPLICATION_BASE("o=gina"),
        PROPERTY_BASE("o=gina-property"),
        ROLE("ou=Groups"),
        USER("ou=Users");

        public final String value;

        Rdn(String value) {
            this.value = value;
        }
    }

    private GinaLdapConfiguration ldapConf;

    private GinaLdapContext ldapContext;

    public GinaLdapAccess(GinaLdapConfiguration ldapConf) {
        Validate.notNull(ldapConf);
        this.ldapConf = ldapConf;
        this.ldapContext = createLdapContext();
    }

    @Override
    public void close() {
        closeDirContext();
    }

    private void closeDirContext() {
        if (ldapContext != null) {
            LOGGER.info("Fermeture du contexte LDAP");
            ldapContext.close();
            ldapContext = null;
        }
    }

    private GinaLdapContext createLdapContext() {
        Properties env = new Properties();

        env.put(Context.INITIAL_CONTEXT_FACTORY, GinaLdapConfiguration.LDAP_CONTEXT_FACTORY);
        env.put(Context.SECURITY_AUTHENTICATION, GinaLdapConfiguration.LDAP_AUTHENTICATION_MODE);
        env.put(Context.REFERRAL, GinaLdapConfiguration.LDAP_REFERRAL_MODE);
        env.put("java.naming.ldap.version", "3");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(ldapConf.getLdapConnectionTimeout()));
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(ldapConf.getLdapReadTimeout()));

        // URL
        String url = ldapConf.getLdapServerUrl();
        if (url.startsWith("ldaps")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            url = url.replaceFirst("ldaps", "ldap");
        }
        env.put(Context.PROVIDER_URL, url);

        // user
        String user = ldapConf.getLdapUser();
        if (StringUtils.isNotBlank(user)) {
            env.put(Context.SECURITY_PRINCIPAL, user);
        }

        // password
        if (StringUtils.isNotBlank(ldapConf.getLdapPassword())) {
            env.put(Context.SECURITY_CREDENTIALS, ldapConf.getLdapPassword());
        }

        GinaLdapContext result;
        try {
            LOGGER.info("Creation du contexte LDAP");
            result = new GinaLdapContext(env);
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        }

        return result;
    }

    private static String makeDn(String baseDn, String rdn) {
        return StringUtils.isBlank(baseDn) ? rdn : rdn + "," + baseDn;
    }

    protected String getUserBaseDn() {
        String dom = ldapConf.getGinaDomain();
        String app = ldapConf.getGinaApplication();
        String baseDN = StringUtils.isBlank(app) ? getDomainDn(dom) : getApplicationDn(dom, app);
        return makeDn(baseDN, Rdn.USER.value);
    }

    protected String getUserDn(String user) {
        return makeDn(getUserBaseDn(), DnPart.toString("cn", user));
    }

    protected String getApplicationBaseDn() {
        return Rdn.APPLICATION_BASE.value;
    }

    protected String getPropertyBaseDn() {
        return Rdn.PROPERTY_BASE.value;
    }

    protected String getUserPropertyBaseDn(String prestation, String type) {
        String dn = getPropertyBaseDn();
        dn = makeDn(dn, DnPart.toString("ou", type));
        dn = makeDn(dn, DnPart.toString("ou", getUser()));
        dn = makeDn(dn, DnPart.toString("cn", prestation));
        return dn;
    }

    protected String getDomainDn(String domain) {
        return makeDn(getApplicationBaseDn(), DnPart.toString("ou", domain));
    }

    protected String getApplicationDn(String domain, String application) {
        return makeDn(getDomainDn(domain), DnPart.toString("ou", application));
    }

    protected String getApplicationDn(String domApp) {
        int pos = domApp.indexOf('.');
        if (pos < 0) {
            throw new IllegalArgumentException("Malformed application name [" + domApp + "]");
        }
        String domain = domApp.substring(0, pos);
        String app = domApp.substring(pos + 1);
        return getApplicationDn(domain, app);
    }

    protected String getRoleBaseDn(String domApp) {
        return makeDn(getApplicationDn(domApp), Rdn.ROLE.value);
    }

    protected String getRoleDn(String application, String role) {
        return makeDn(getRoleBaseDn(application), DnPart.toString("cn", role));
    }

    private GinaLdapContext getLdapContext() {
        if (ldapContext == null) {
            throw new GinaException("Pas de context LDAP. Il a probablement deja ete detruit par un appel a close()");
        }
        return ldapContext;
    }

    @Override
    public boolean isValidUser(String user) {
        long tm = System.currentTimeMillis();
        GinaLdapQuery qry = getLdapContext().newQuery()
                    .setBaseDn(getUserBaseDn())
                    .setFilter(GinaLdapUtils.getLdapFilterUser(user))
                    .setScope(GinaLdapQuery.Scope.ONELEVEL);
        Boolean result = qry.unique((dn, attrs) -> true);
        logExecutionTime("isValidUser(" + user + ")", tm);
        return result != null ? result : false;
    }

    @Override
    public Map<String, String> getUserAttrs(String user, String[] attrs) {
        long tm = System.currentTimeMillis();
        Map<String, String> result = getUserDnAttrs(getUserDn(user), attrs);
        logExecutionTime("getUserAttrs(" + user + "," + Arrays.toString(attrs) + ")", tm);
        return result;
    }

    private Map<String, String> getUserDnAttrs(String userDn, final String[] attrs) {
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setFilter(GinaLdapUtils.getLdapFilterUser())
                .setAttributes(attrs)
                .setScope(GinaLdapQuery.Scope.OBJECT)
                .setBaseDn(userDn);
        return qry.unique((dn, names) -> {
            LOGGER.debug("consume dn = {}, names = {}  ->  {}",
                    dn, names, GinaLdapUtils.attributesToUser(dn, names, attrs));
            return GinaLdapUtils.attributesToUser(dn, names, attrs);
        });
    }

    @Override
    public List<String> getUserRoles(String user) {
        long tm = System.currentTimeMillis();
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(getUserDn(user))
                .setFilter(GinaLdapUtils.getLdapFilterUser()).setScope(GinaLdapQuery.Scope.OBJECT)
                .setAttributes(GinaLdapUtils.ATTRIBUTE_MEMBEROF);
        List<String> roles = qry.unique((dn, attrs) ->
            GinaLdapUtils.allValues(attrs, GinaLdapUtils.ATTRIBUTE_MEMBEROF)
                    .stream()
                    .map(group -> GinaLdapUtils.roleDnToString(group))
                    .collect(Collectors.toList())
        );
        logExecutionTime("getUserRoles(" + user + ")", tm);
        return roles == null ? Collections.<String>emptyList() : roles;
    }

    @Override
    public List<String> getUserRoles(String user, String domApp) {
        long tm = System.currentTimeMillis();

        String suffix = "," + getRoleBaseDn(domApp);
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(getUserDn(user))
                .setFilter(GinaLdapUtils.getLdapFilterUser()).setScope(GinaLdapQuery.Scope.OBJECT)
                .setAttributes(GinaLdapUtils.ATTRIBUTE_MEMBEROF);
        List<String> roles = qry.unique((dn, attrs) ->
            GinaLdapUtils.allValues(attrs, GinaLdapUtils.ATTRIBUTE_MEMBEROF)
                    .stream()
                    .filter(group -> group.endsWith(suffix))
                    .map(group -> DnPart.parse(group.substring(0, group.length() - suffix.length())))
                    .filter(parts -> parts != null
                            && parts.length == 1
                            && parts[0].getAttr().equalsIgnoreCase(GinaLdapUtils.ATTRIBUTE_CN))
                    .map(parts -> parts[0].getValue())
                    .collect(Collectors.toList())
        );
        roles = roles == null ? Collections.emptyList() : roles;

        logExecutionTime("getUserRoles(" + user + "," + domApp + ")", tm);
        return roles;
    }

    @Override
    public boolean hasUserRole(String user, String role) {
        return getUserRoles(user).contains(role);
    }

    @Override
    public boolean hasUserRole(String user, String application, String role) {
        long tm = System.currentTimeMillis();
        Boolean result = hasUserDnRole(getUserDn(user), application, role);
        logExecutionTime("hasUserRole(" + user + "," + application + "," + role + ")", tm);
        return result != null ? result : false;
    }

    private boolean hasUserDnRole(String userDn, String application, String role) {
        String filter = GinaLdapUtils.ldapFilterAnd(
                GinaLdapUtils.getLdapFilterUser(),
                GinaLdapUtils.ldapFilterEquals(GinaLdapUtils.ATTRIBUTE_MEMBEROF, getRoleDn(application, role)));
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(userDn)
                .setFilter(filter)
                .setScope(GinaLdapQuery.Scope.OBJECT);
        Boolean result = qry.first((dn, attrs) -> true);
        return result != null ? result : false;
    }

    @Override
    public List<String> getAppRoles(String domApp) {
        long tm = System.currentTimeMillis();
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(getRoleBaseDn(domApp))
                .setFilter(GinaLdapUtils.getLdapFilterGroup())
                .setScope(GinaLdapQuery.Scope.ONELEVEL)
                .setAttributes(GinaLdapUtils.ATTRIBUTE_CN);
        List<String> result = qry.forEach((dn, attrs) -> GinaLdapUtils.firstValue(attrs, GinaLdapUtils.ATTRIBUTE_CN));
        logExecutionTime("getAppRoles(" + domApp + ")", tm);
        return result;
    }

    @Override
    public List<String> getBusinessRoles(String domApp) {
        String encodedApplication = GinaLdapEncoder.filterEncode(domApp);
        List<String> roles = getAppRoles(encodedApplication);
        return roles.stream()
                .filter(r -> r.startsWith("RM-"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String[] attrs) {
        return getUsers(application, ROLE_UTILISATEUR, attrs);
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String role, String[] attrs) {
        long tm = System.currentTimeMillis();
        String filter = GinaLdapUtils.ldapFilterEquals(GinaLdapUtils.ATTRIBUTE_MEMBEROF, getRoleDn(application, role));
        List<Map<String, String>> result = queryUsers(filter, attrs);
        logExecutionTime("getUsers(" + application + "," + role + "," + Arrays.toString(attrs) + ")", tm);
        return result;
    }

    private List<Map<String, String>> queryUsers(String cond, final String[] attrs) {
        String filter = GinaLdapUtils.getLdapFilterUser();
        if (cond != null) {
            filter = GinaLdapUtils.ldapFilterAnd(filter, cond);
        }
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(getUserBaseDn())
                .setFilter(filter)
                .setAttributes(attrs)
                .setScope(GinaLdapQuery.Scope.ONELEVEL);
        return qry.forEach((dn, names) -> GinaLdapUtils.attributesToUser(dn, names, attrs));
    }

    @Override
    @Deprecated
    public void sendMail(String from, String[] to, String[] cc, String subject, String text, String mimeType) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getUser() {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getLanguage() {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getEnvironment() {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getIntegrationUserRoles(String paramString1, String paramString2) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    @Deprecated
    public List<String> getIntegrationUserAttributes(String paramString1, String paramString2) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    @Deprecated
    public List<String> getInheritingRoles(String paramString1, String paramString2) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getPMProprieteMetier(String paramString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    @Deprecated
    public String getOwnIDUniqueForPPorPseudo() {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getOwnPMProprieteMetier(String paramString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getPPProprieteMetier(String paramString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getOwnPPProprieteMetier(String paramString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getUsersByPhone(String paramString, Boolean paramBoolean,
            String[] paramArrayOfString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getUsersBySIRHNumber(String sirhNumber, Boolean paramBoolean,
            String[] paramArrayOfString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getUsersByName(String paramString, Boolean paramBoolean,
            String[] paramArrayOfString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasRole(String application, String role) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getRoles(String application) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getAllUsers(String filter, String[] attrs) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public Map<String, String> getUserAttrs(String[] attrs) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    private void logExecutionTime(String operationDescription, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.debug(operationDescription + " effectue' en " + duration + "ms");
    }

    protected void logException(Throwable e) {
        LOGGER.error("Erreur : ", e);
    }

    @Override
    protected void finalize() throws Throwable {
        if (ldapContext != null) {
            LOGGER.warn("Appel au finaliseur pour fermer le contexte LDAP : mauvaise pratique. Le contexte LDAP "
                      + "aurait deja du etre ferme par un appel a close()");
        }
        super.finalize();
    }

}
