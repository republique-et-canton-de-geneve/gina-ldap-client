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
import javax.naming.directory.Attributes;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * TODO renommer cette classe. Exemple : GinaLdapAccess.
 */
public class GinaLdapCommon implements GinaApiLdapBaseAble {

    /**
     * Message d'erreur pour les methodes non implementees.
     */
    public static final String NOT_IMPLEMENTED = "Not implemented";

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapCommon.class);

    private static final String DEFAULT_LANG = "FR";

    private static final String CLASS_PROPERTY_VALUES = "propertyValues";

    private static final String ROLE_USER = "UTILISATEUR";

    private static final String APP_PREFIX_PATTERN = "^[A-Za-z0-9][A-Za-z0-9-]*$";

    /**
     * Relative distinguished names.
     */
    private enum Rdn {
        APPLICATION_BASE("o=gina"),
        PROPERTY_BASE("o=gina-property"),
        ROLE("ou=Groups"),
        USER("ou=Users");

        public String value;

        Rdn(String value) {
            this.value = value;
        }
    }

    private enum AttributeName {
        ENVIRONMENT("environment"),
        OWN_VALUE("ownValue"),
        VALUE("value"),
        PROPERTY_NAME("propertyName"),
        SURNAME("sn"),
        LOGIN_DISABLED("loginDisabled"),
        PHONE_NUMBER("phoneNumber"),
        EMPLOYEE_NUMBER("employeeNumber");

        public String value;

        AttributeName(String value) {
            this.value = value;
        }
    }

    private GinaLdapConfiguration ldapConf;

    private GinaLdapContext ldapContext;

    public GinaLdapCommon(GinaLdapConfiguration ldapConf) {
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

/*
    protected SearchControls getSearchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningObjFlag(false);

        return searchControls;
    }

    protected SearchControls getSearchControls(final String[] attributes) {
        final SearchControls searchControls = getSearchControls();

        if (attributes != null && attributes.length > 0) {
            searchControls.setReturningAttributes(attributes);
        }

        return searchControls;
    }
*/

    private GinaLdapContext getLdapContext() {
        if (ldapContext == null) {
            throw new GinaException("Pas de context LDAP. Il a probablement deja ete detruit par un appel a close()");
        }
        return ldapContext;
    }

    /*
    @Override
    public boolean isValidUser(String user) {
        final String encodedUser = GinaLdapEncoder.filterEncode(user);

        NamingEnumeration<?> answer = null;
        try {
            SearchControls searchControls = getSearchControls();
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute("cn", encodedUser));
            String searchFilter = GinaLdapUtils.getLdapFilterUser(encodedUser);
            answer = getLdapContext().search("", searchFilter, searchControls);

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("sr = {}", sr);
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    Attribute cn = attrs.get("cn");
                    if (cn != null) {
                        String cnString = (String) cn.get();
                        Attribute departmentNumber = attrs.get(GinaLdapUtils.ATTRIBUTE_DEPARTMENT_NUMBER);
                        if (encodedUser.equalsIgnoreCase(cnString) && departmentNumber != null && StringUtils
                                .isNotBlank((String) departmentNumber.get())) {
                            return true;
                        }
                    }
                }
            }
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
        }

        return false;
    }
     */

    @Override
    public boolean isValidUser(String user) {
        long tm = System.currentTimeMillis();
        GinaLdapQuery qry = getLdapContext().newQuery()
                    .setBaseDn(getUserBaseDn())
                    .setFilter(GinaLdapUtils.getLdapFilterUser(user))
                    .setScope(GinaLdapQuery.Scope.ONELEVEL);
        Boolean result = qry.unique(new GinaLdapQuery.Consumer<Boolean>() {
                @Override
                public Boolean consume(String dn, Attributes attrs) throws NamingException {
                    return true;
                }
            });
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
        return qry.unique(new GinaLdapQuery.Consumer<Map<String, String>>() {
            @Override
            public Map<String, String> consume(String dn, Attributes names) {
                LOGGER.debug("consume dn = {}, names = {}  ->  {}", dn, names, GinaLdapUtils.attributesToUser(dn, names, attrs));
                return GinaLdapUtils.attributesToUser(dn, names, attrs);
            }
        });
    }

    /*
    @Override
    public List<String> getUserRoles(String user, String application) {
        final String encodedUser = GinaLdapEncoder.filterEncode(user);
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);

        List<String> roles = new ArrayList<String>();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> answerAtt = null;

        try {
            String ginaDomain = GinaLdapUtils.extractDomain(encodedApplication);
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_MEMBEROF });
            String searchFilter = GinaLdapUtils.getLdapFilterUser(encodedUser);
            answer = getLdapContext().search("", searchFilter, searchControls);

            if (answer != null) {
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    LOGGER.debug("sr = {}", sr);
                    if (sr != null) {
                        final Attributes attrs = sr.getAttributes();
                        LOGGER.debug("attrs = {}", attrs);
                        if (attrs != null && attrs.get(GinaLdapUtils.ATTRIBUTE_MEMBEROF) != null) {
                            try {
                                answerAtt = attrs.get(GinaLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
                                while (answerAtt.hasMoreElements()) {
                                    String att = (String) answerAtt.next();
                                    LOGGER.debug(att);
                                    String pattern =
                                            ",ou=Groups,ou=" + ginaApplication + ",ou=" + ginaDomain + ",o=gina";
                                    if (StringUtils.isNotBlank(att) && att.contains(pattern)) {
                                        String roleClean = StringUtils.replaceOnce(att, "cn=", "");
                                        roleClean = StringUtils.replaceOnce(roleClean, pattern, "");
                                        roles.add(roleClean);
                                    }
                                }
                            } finally {
                                GinaLdapUtils.closeQuietly(answerAtt);
                            }
                        }
                    }
                }
            }
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
        }

        LOGGER.debug("roles = {}", roles);

        return roles;
    }
     */

    @Override
    public List<String> getUserRoles(String user, String domApp) {
        long tm = System.currentTimeMillis();
        List<String> result = getRoles(user, domApp);
        logExecutionTime("getUserRoles(" + user + "," + domApp + ")", tm);
        return result;
    }

    private List<String> getRoles(String user, String domApp) {
        final String suffix = "," + getRoleBaseDn(domApp);
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(getUserDn(user))
                .setFilter(GinaLdapUtils.getLdapFilterUser()).setScope(GinaLdapQuery.Scope.OBJECT)
                .setAttributes(GinaLdapUtils.ATTRIBUTE_MEMBEROF);
        List<String> roles = qry.unique(new GinaLdapQuery.Consumer<List<String>>() {
            @Override
            public List<String> consume(String dn, Attributes attrs) {
                List<String> result = new ArrayList<>();
                List<String> groups = GinaLdapUtils.allValues(attrs, GinaLdapUtils.ATTRIBUTE_MEMBEROF);
                for (String group : groups) {
                    if (!group.endsWith(suffix)) {
                        continue;
                    }
                    DnPart[] parts = DnPart.parse(group.substring(0, group.length() - suffix.length()));
                    if (parts == null
                            || parts.length != 1
                            || !parts[0].getAttr().equalsIgnoreCase(GinaLdapUtils.ATTRIBUTE_CN)) {
                        continue;
                    }
                    result.add(parts[0].getValue());
                }
                return result;
            }
        });
        return roles == null ? Collections.<String>emptyList() : roles;
    }

    @Override
    public List<String> getUserRoles(String user) {
        long tm = System.currentTimeMillis();
        GinaLdapQuery qry = getLdapContext().newQuery()
                .setBaseDn(getUserDn(user))
                .setFilter(GinaLdapUtils.getLdapFilterUser()).setScope(GinaLdapQuery.Scope.OBJECT)
                .setAttributes(GinaLdapUtils.ATTRIBUTE_MEMBEROF);
        List<String> roles = qry.unique(new GinaLdapQuery.Consumer<List<String>>() {
            @Override
            public List<String> consume(String dn, Attributes attrs) {
                List<String> result = new ArrayList<String>();
                List<String> groups = GinaLdapUtils.allValues(attrs, GinaLdapUtils.ATTRIBUTE_MEMBEROF);
                for (String group : groups) {
                    result.add(GinaLdapUtils.roleDnToString(group));
                }
                return result;
            }
        });
        logExecutionTime("getUserRoles(" + user + ")", tm);
        return roles == null ? Collections.<String>emptyList() : roles;
    }

    @Override
    public boolean hasUserRole(String username, String role) {
        return getUserRoles(username).contains(role);
        /*
        long tm = System.currentTimeMillis();
        int ix = role.lastIndexOf('.');
        if (ix < 0) {
            return false;
        }
        String application = role.substring(0, ix);
        String approle = role.substring(ix + 1);
        Boolean result = hasUserDnRole(getUserDn(username), application, approle);
        logExecutionTime("hasUserRole(" + username + "," + role + ")", tm);
        return result != null ? result : false;
         */
    }

    /*
    @Override
    public boolean hasUserRole(String user, String application, String role) {
        final String encodedUser = GinaLdapEncoder.filterEncode(user);
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);
        final String encodedRole = GinaLdapEncoder.filterEncode(role);

        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> answerAtt = null;

        try {
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_MEMBER });
            answer = getLdapContext().search(GinaLdapUtils.getLdapFilterOu(ginaApplication),
                    GinaLdapUtils.getLdapFilterCn(encodedRole), searchControls);

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("sr = {}", sr);
                Attributes attrs = sr.getAttributes();
                if (attrs != null && attrs.get(GinaLdapUtils.ATTRIBUTE_MEMBER) != null) {
                    try {
                        answerAtt = sr.getAttributes().get(GinaLdapUtils.ATTRIBUTE_MEMBER).getAll();
                        while (answerAtt.hasMoreElements()) {
                            String att = (String) answerAtt.next();
                            if (att.toUpperCase().contains(encodedUser.toUpperCase())) {
                                return true;
                            }
                        }
                    } finally {
                        GinaLdapUtils.closeQuietly(answerAtt);
                    }
                }
            }
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
        }

        return false;
    }
     */

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
        Boolean result = qry.first(new GinaLdapQuery.Consumer<Boolean>() {
            @Override
            public Boolean consume(String dn, Attributes attrs) {
                return true;
            }
        });
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
        List<String> result = qry.forEach(new GinaLdapQuery.Consumer<String>() {
            @Override
            public String consume(String dn, Attributes attrs) {
                return GinaLdapUtils.firstValue(attrs, GinaLdapUtils.ATTRIBUTE_CN);
            }
        });
        logExecutionTime("getAppRoles(" + domApp + ")", tm);
        return result;
    }

    @Override
    public List<String> getBusinessRoles(String application) throws RemoteException {
        String encodedApplication = GinaLdapEncoder.filterEncode(application);

        List<String> roles = getAppRoles(encodedApplication);

        List<String> result = new ArrayList<String>();

        if (roles != null) {
            for (String role : roles) {
                if (role.startsWith("RM-")) {
                    result.add(role);
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String[] attrs) {
        return getUsers(application, ROLE_USER, attrs);
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
        return qry.forEach(new GinaLdapQuery.Consumer<Map<String, String>>() {
            @Override
            public Map<String, String> consume(String dn, Attributes names) {
                return GinaLdapUtils.attributesToUser(dn, names, attrs);
            }
        });
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
