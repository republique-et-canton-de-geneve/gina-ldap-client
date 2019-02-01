package gina.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapEncoder;
import gina.impl.util.GinaLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaLdapApplication extends GinaLdapCommon {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapApplication.class);

    public GinaLdapApplication(GinaLdapConfiguration ldapConf) {
        Validate.notNull(ldapConf);
        this.ldapConf = ldapConf;
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur donne a le role donne pour
     * l'application donnee
     *
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String role) {
        final String encodedUser = GinaLdapEncoder.filterEncode(user);
        final String encodedRole = GinaLdapEncoder.filterEncode(role);

        LdapContext ctxtDir = null;
        NamingEnumeration<?> answer = null;

        try {
            ctxtDir = getLdapContext();
            SearchControls searchControls = getSearchControls();
            String searchFilter = "(&(objectClass=person)(cn=" + encodedUser + "))";
            LOGGER.debug("searchFilter = {}", searchFilter);
            answer = ctxtDir.search("", searchFilter, searchControls);
            return answer != null && answer.hasMoreElements();
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }
    }

    /*
     * (non-Javadoc) Donne tous les roles de l'utilisateur passe en parametre
     *
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user) {
        final String encodedUser = GinaLdapEncoder.filterEncode(user);

        LdapContext ctxtDir = null;
        List<String> roles = new ArrayList<String>();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> nameEnum = null;
        try {
            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_MEMBEROF });
            String searchFilter = GinaLdapUtils.getLdapFilterUser(encodedUser);
            ctxtDir = getLdapContext();
            answer = ctxtDir.search("", searchFilter, searchControls);

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("sr={}", sr);
                Attributes attributes = sr.getAttributes();
                try {
                    nameEnum = attributes.get(GinaLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
                    while (nameEnum.hasMoreElements()) {
                        String role = (String) nameEnum.next();
                        if (StringUtils.isNotBlank(role)) {
                            String roleClean = StringUtils.replaceOnce(role, "cn=", "");
                            String[] roleCleanString = StringUtils.split(roleClean, ",", 2);
                            roles.add(roleCleanString[0]);
                        }
                    }
                } finally {
                    GinaLdapUtils.closeQuietly(nameEnum);
                }
            }
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return roles;
    }

    /*
     * (non-Javadoc) Retoune les roles d'une application
     *
     * @see gina.api.GinaApiLdapBaseAble#getAppRoles(java.lang.String)
     */
    @Override
    public List<String> getAppRoles(String appli) {
        final String encodedAppli = GinaLdapEncoder.filterEncode(appli);

        LdapContext ctxtDir = null;
        List<String> roles = new ArrayList<String>();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> nameEnum = null;

        try {
            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_CN });
            ctxtDir = getLdapContext();
            answer = ctxtDir
                    .search(GinaLdapUtils.getLdapFilterGroup(encodedAppli), GinaLdapUtils.getLdapFilterCn("*"),
                            searchControls);
            if (answer != null) {
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    LOGGER.debug("sr={}", sr);
                    try {
                        nameEnum = sr.getAttributes().get(GinaLdapUtils.ATTRIBUTE_CN).getAll();
                        if (nameEnum != null) {
                            while (nameEnum.hasMoreElements()) {
                                String role = (String) nameEnum.next();
                                LOGGER.debug("role={}", role);
                                roles.add(role);
                            }
                        }
                    } finally {
                        GinaLdapUtils.closeQuietly(nameEnum);
                    }
                }
            }
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return roles;
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String[] paramArrayOfString) {
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);

        LdapContext ctxtDir = null;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_MEMBER });
            ctxtDir = getLdapContext();
            answer = ctxtDir
                    .search(GinaLdapUtils.getLdapFilterGroup(ginaApplication), GinaLdapUtils.getLdapFilterCn("*"),
                            searchControls);

            list = parseAnswer(answer, paramArrayOfString);
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return list;
    }

    /*
     * (non-Javadoc) Donne la liste des utilisateurs ayant acces a l'application
     * passee en parametre pour le role donne, avec les attributs demandes
     *
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public List<Map<String, String>> getUsers(String application, String role, String[] paramArrayOfString) {
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);
        final String encodedRole = GinaLdapEncoder.filterEncode(role);

        LdapContext ctxtDir = null;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_MEMBER });
            ctxtDir = getLdapContext();
            answer = ctxtDir.search(GinaLdapUtils.getLdapFilterOu(ginaApplication),
                    GinaLdapUtils.getLdapFilterCn(encodedRole), searchControls);

            LOGGER.debug("paramArrayOfString = {}", paramArrayOfString);
            list = parseAnswer(answer, paramArrayOfString);
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return list;
    }

    private List<Map<String, String>> parseAnswer(final NamingEnumeration<?> answer, final String[] paramArrayOfString)
            throws NamingException {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        if (answer != null) {
            List<String> users = new ArrayList<String>();

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("name={}", sr.getName());

                Attributes attrs = sr.getAttributes();
                LOGGER.debug("sr={}", sr);
                if (attrs != null) {
                    Attribute attmember = attrs.get(GinaLdapUtils.ATTRIBUTE_MEMBER);
                    LOGGER.debug("attmember={}", attmember);

                    if (attmember != null) {
                        for (int j = 0; j < attmember.size(); j++) {
                            String member = (String) attmember.get(j);

                            if (member != null) {
                                String username = member.substring(0, member.indexOf(',')).replace("cn=", "")
                                        .toLowerCase();
                                LOGGER.debug("username = {}", username);
                                if (StringUtils.isNotBlank(username) && !users.contains(username)) {
                                    users.add(username);
                                    Map<String, String> map = this.getUserAttrs(username, paramArrayOfString, false);
                                    list.add(map);
                                }
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

}
