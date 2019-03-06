package gina.impl;

import gina.api.GinaApiLdapBaseAble;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang3.StringUtils;

import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapEncoder;
import gina.impl.util.GinaLdapUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GinaLdapCommon implements GinaApiLdapBaseAble {

    /**
     * Message d'erreur pour les methodes non implementees.
     */
    public static final String NOT_IMPLEMENTED = "Not implemented";

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapCommon.class);

    protected GinaLdapConfiguration ldapConf;

    private LdapContext ldapContext;

    public GinaLdapCommon(GinaLdapConfiguration ldapConf) {
        Validate.notNull(ldapConf);
        this.ldapConf = ldapConf;
        this.ldapContext = createDirContext();
    }

    @Override
    public void close() {
        closeDirContext();
    }

    private void closeDirContext() {
        if (ldapContext != null) {
            try {
                LOGGER.info("Fermeture du contexte LDAP");
                ldapContext.close();
            } catch (NamingException e) {
                logException(e);
            }
        }
    }

    private InitialLdapContext createDirContext() {
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, GinaLdapConfiguration.LDAP_CONTEXT_FACTORY);
        env.put(Context.SECURITY_AUTHENTICATION, GinaLdapConfiguration.LDAP_AUTHENTICATION_MODE);
        env.put(Context.REFERRAL, GinaLdapConfiguration.LDAP_REFERRAL_MODE);
        env.put("java.naming.ldap.version", "3");

        env.put("com.sun.jndi.ldap.connect.pool", "true");

        env.put(Context.PROVIDER_URL, ldapConf.getLdapServerUrl() + "/" + ldapConf.getLdapBaseDn());

        if (StringUtils.isNotEmpty(ldapConf.getLdapUser())) {
            env.put(Context.SECURITY_PRINCIPAL, ldapConf.getLdapUser());
        }

        if (StringUtils.isNotEmpty(ldapConf.getLdapPassword())) {
            env.put(Context.SECURITY_CREDENTIALS, ldapConf.getLdapPassword());
        }

        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(ldapConf.getLdapConnectionTimeout()));

        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(ldapConf.getLdapReadTimeout()));

        if (ldapConf.getLdapServerUrl().startsWith("ldaps")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        InitialLdapContext result;
        try {
            LOGGER.info("Creation du contexte LDAP");
            result = new InitialLdapContext(env, null);
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        }

        return result;
    }

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

    protected LdapContext getLdapContext() {
        if (ldapContext == null) {
            throw new GinaException("Pas de context LDAP. Il a probablement deja ete detruit par un appel a close()");
        }
        return ldapContext;
    }

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

    @Override
    public Map<String, String> getUserAttrs(String user, String[] paramArrayOfString) {
        final String encodedUser = GinaLdapEncoder.filterEncode(user);

//        Arrays.asList(paramArrayOfString).contains("param");
        Map<String, String> myMap = new HashMap<String, String>();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> nameEnum = null;

        try {
            SearchControls searchControls = getSearchControls(paramArrayOfString);
            LOGGER.debug("searchControls = {}", searchControls);
            String searchFilter = GinaLdapUtils.getLdapFilterUser(encodedUser);
            LOGGER.debug("searchFilter = {}", searchFilter);
            answer = getLdapContext().search("", searchFilter, searchControls);

            if (answer != null) {
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    Attributes attrs = sr.getAttributes();
                    LOGGER.debug("sr = {}", sr);
                    if (attrs != null) {
                        for (int i = 0; i < paramArrayOfString.length; i++) {
                            String attr = paramArrayOfString[i];
                            LOGGER.debug("attr = {}", attr);
                            Attribute attribute = attrs.get(attr);
                            if (attribute != null) {
                                try {
                                    nameEnum = attribute.getAll();
                                    if (nameEnum != null) {
                                        String value = "";
                                        while (nameEnum.hasMoreElements()) {
                                            if (value.isEmpty()) {
                                                value = (String) nameEnum.next();
                                            } else {
                                                StringBuilder sb = new StringBuilder();
                                                sb.append(value);
                                                sb.append(":");
                                                sb.append((String) nameEnum.next());
                                                value = sb.toString();
                                            }
                                        }
                                        LOGGER.debug("value = {}", value);
                                        myMap.put(paramArrayOfString[i], value);
                                    }
                                } finally {
                                    GinaLdapUtils.closeQuietly(nameEnum);
                                }
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

        return myMap;
    }

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

    @Override
    public List<String> getBusinessRoles(String application) throws RemoteException {
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);

        List<String> roles = this.getAppRoles(encodedApplication);

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

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public void sendMail(String from, String[] to, String[] cc, String subject, String text,
            String mimeType) {
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
    public List<String> getIntegrationUserAttributes(String paramString1, String paramString2) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getInheritingRoles(String paramString1, String paramString2) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getPMProprieteMetier(String paramString) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
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
