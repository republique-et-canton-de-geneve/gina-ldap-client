package gina.api;

import java.rmi.RemoteException;
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

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapEncoder;
import gina.api.util.GinaApiLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaApiLdapBaseAbleApplicationImpl extends GinaApiLdapBaseAbleCommon {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaApiLdapBaseAbleApplicationImpl.class);

    public GinaApiLdapBaseAbleApplicationImpl(GinaApiLdapConfiguration ldapConf) {
        Validate.notNull(ldapConf);
        this.ldapConf = ldapConf;
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur donné à le role donné pour
     * l'application donnée
     *
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String role) {
        final String encodedUser = GinaApiLdapEncoder.filterEncode(user);
        final String encodedRole = GinaApiLdapEncoder.filterEncode(role);

        LdapContext ctxtDir = null;
        NamingEnumeration<?> answer = null;

        try {
            ctxtDir = getLdapContext();
            SearchControls searchControls = getSearchControls();
            String searchFilter =
                    "(&(objectClass=users)(cn=" + encodedUser + ")&(objectClass=memberOf)(cn=" + encodedRole + "))";
            answer = ctxtDir.search("", searchFilter, searchControls);
            return answer != null && answer.hasMoreElements();
        } catch (NamingException e) {
            LOGGER.error("Erreur : ",e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaApiLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }
    }

    /*
     * (non-Javadoc) Donne tous les rôles de l'utilisateur passé en paramètre
     *
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user) throws RemoteException {
        final String encodedUser = GinaApiLdapEncoder.filterEncode(user);

        LdapContext ctxtDir = null;
        List<String> roles = new ArrayList<String>();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> nameEnum = null;
        try {
            SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBEROF });
            String searchFilter = GinaApiLdapUtils.getLdapFilterUser(encodedUser);
            ctxtDir = getLdapContext();
            answer = ctxtDir.search("", searchFilter, searchControls);

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("sr=" + sr);
                Attributes attributes = sr.getAttributes();
                try {
                    nameEnum = attributes.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
                    while (nameEnum.hasMoreElements()) {
                        String role = (String) nameEnum.next();
                        if (StringUtils.isNotBlank(role)) {
                            String roleClean = StringUtils.replaceOnce(role, "cn=", "");
                            String[] roleCleanString = StringUtils.split(roleClean, ",", 2);
                            roles.add(roleCleanString[0]);
                        }
                    }
                } finally {
                    GinaApiLdapUtils.closeQuietly(nameEnum);
                }
            }
        } catch (NamingException e) {
            LOGGER.error("Erreur : ", e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaApiLdapUtils.closeQuietly(answer);
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
    public List<String> getAppRoles(String appli) throws RemoteException {
        final String encodedAppli = GinaApiLdapEncoder.filterEncode(appli);

        LdapContext ctxtDir = null;
        List<String> roles = new ArrayList<String>();
        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> nameEnum = null;

        try {
            SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_CN });
            ctxtDir = getLdapContext();
            answer = ctxtDir
                    .search(GinaApiLdapUtils.getLdapFilterGroup(encodedAppli), GinaApiLdapUtils.getLdapFilterCn("*"),
                            searchControls);
            if (answer != null) {
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    LOGGER.debug("sr=" + sr);
                    try {
                        nameEnum = sr.getAttributes().get(GinaApiLdapUtils.ATTRIBUTE_CN).getAll();
                        if (nameEnum != null) {
                            while (nameEnum.hasMoreElements()) {
                                String role = (String) nameEnum.next();
                                LOGGER.debug("role=" + role);
                                roles.add(role);
                            }
                        }
                    } finally {
                        GinaApiLdapUtils.closeQuietly(nameEnum);
                    }
                }
            }
        } catch (NamingException e) {
            LOGGER.error("Erreur : ", e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaApiLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return roles;
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String[] paramArrayOfString)
            throws RemoteException {
        final String encodedApplication = GinaApiLdapEncoder.filterEncode(application);

        LdapContext ctxtDir = null;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaApiLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
            ctxtDir = getLdapContext();
            answer = ctxtDir
                    .search(GinaApiLdapUtils.getLdapFilterGroup(ginaApplication), GinaApiLdapUtils.getLdapFilterCn("*"),
                            searchControls);

            list = parseAnswer(answer, paramArrayOfString);
        } catch (NamingException e) {
            LOGGER.error("Erreur : ", e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaApiLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return list;
    }

    /*
     * (non-Javadoc) Donne la liste des utilisateurs ayant accès à l'application
     * passée en paramètre pour le rôle donné, avec les attributs demandés
     *
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public List<Map<String, String>> getUsers(String application, String role, String[] paramArrayOfString)
            throws RemoteException {
        final String encodedApplication = GinaApiLdapEncoder.filterEncode(application);
        final String encodedRole = GinaApiLdapEncoder.filterEncode(role);

        LdapContext ctxtDir = null;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaApiLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
            ctxtDir = getLdapContext();
            answer = ctxtDir.search(GinaApiLdapUtils.getLdapFilterOu(ginaApplication),
                    GinaApiLdapUtils.getLdapFilterCn(encodedRole), searchControls);

            list = parseAnswer(answer, paramArrayOfString);
        } catch (NamingException e) {
            LOGGER.error("Erreur : ", e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaApiLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return list;
    }

    private List<Map<String, String>> parseAnswer(final NamingEnumeration<?> answer, final String[] paramArrayOfString)
            throws NamingException, RemoteException {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        if (answer != null) {
            List<String> users = new ArrayList<String>();

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("name : " + sr.getName());

                Attributes attrs = sr.getAttributes();
                LOGGER.debug("sr=" + sr);
                if (attrs != null) {
                    Attribute attmember = attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBER);
                    LOGGER.debug("attmember=" + attmember);

                    if (attmember != null) {
                        for (int j = 0; j < attmember.size(); j++) {
                            String member = (String) attmember.get(j);

                            if (member != null) {
                                String username = member.substring(0, member.indexOf(',')).replace("cn=", "")
                                        .toLowerCase();
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
