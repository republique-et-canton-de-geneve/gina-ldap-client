package gina.api;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapEncoder;
import gina.api.util.GinaApiLdapUtils;
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
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaApiLdapBaseAbleDomainImpl extends GinaApiLdapBaseAbleCommon {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaApiLdapBaseAbleDomainImpl.class);

    public GinaApiLdapBaseAbleDomainImpl(GinaApiLdapConfiguration ldapConf) {
        Validate.notNull(ldapConf);
        this.ldapConf = ldapConf;
    }

    /*
     * (non-Javadoc) retoune les roles d'une application
     *
     * @see gina.api.GinaApiLdapBaseAble#getAppRoles(java.lang.String)
     */
    @Override
    public List<String> getAppRoles(String application) {
        final String encodedApplication = GinaApiLdapEncoder.filterEncode(application);

        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> att = null;

        LdapContext ctxtDir = null;
        List<String> roles = new ArrayList<String>();
        try {
            String ginaApplication = GinaApiLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_CN });
            ctxtDir = getLdapContext();
            answer = ctxtDir
                    .search(GinaApiLdapUtils.getLdapFilterOu(ginaApplication), GinaApiLdapUtils.getLdapFilterCn("*"),
                            searchControls);

            if (answer != null) {
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    try {
                        att = sr.getAttributes().get(GinaApiLdapUtils.ATTRIBUTE_CN).getAll();
                        while (att.hasMoreElements()) {
                            String cn = (String) att.next();
                            LOGGER.debug("cn=" + cn);
                            roles.add(cn);
                        }
                    } finally {
                        GinaApiLdapUtils.closeQuietly(att);
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

    /*
     * (non-Javadoc) Donne la liste des utilisateurs ayant accès à l'application
     * passée en paramètre, avec les attributs demandés
     *
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String)
     */
    @Override
    public List<Map<String, String>> getUsers(String application, String[] attrs) {
        final String encodedApplication = GinaApiLdapEncoder.filterEncode(application);

        LdapContext ctxtDir = null;
        List<Map<String, String>> list;
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaApiLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls();
            ctxtDir = getLdapContext();
            answer = ctxtDir
                    .search(GinaApiLdapUtils.getLdapFilterOu(ginaApplication), GinaApiLdapUtils.getLdapFilterCn("*"),
                            searchControls);

            list = parseAnswer(answer, attrs);

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
    public List<Map<String, String>> getUsers(String application, String role, String[] attrs) {
        final String encodedApplication = GinaApiLdapEncoder.filterEncode(application);
        final String encodedRole = GinaApiLdapEncoder.filterEncode(role);

        LdapContext ctxtDir = null;
        List<Map<String, String>> list;
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaApiLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls();
            ctxtDir = getLdapContext();
            answer = ctxtDir.search(GinaApiLdapUtils.getLdapFilterOu(ginaApplication),
                    GinaApiLdapUtils.getLdapFilterCn(encodedRole), searchControls);

            list = parseAnswer(answer, attrs);
        } catch (NamingException e) {
            LOGGER.error("Erreur : ", e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaApiLdapUtils.closeQuietly(answer);
            closeDirContext(ctxtDir);
        }

        return list;
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Override
    public boolean hasUserRole(String user, String role) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getUserRoles(String user) {
        throw new GinaException(NOT_IMPLEMENTED);
    }

    // -----------------------------------------------------------------------------------------
    // METHODES UTILITAIRES
    // -----------------------------------------------------------------------------------------

    private List<Map<String, String>> parseAnswer(final NamingEnumeration<?> answer, final String[] attrs)
            throws NamingException {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        if (answer != null) {
            List<String> users = new ArrayList<String>();
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                LOGGER.debug("name : " + sr.getName().substring(0, sr.getName().indexOf(','))
                                           .replace("cn=", ""));

                Attributes attrsResult = sr.getAttributes();
                LOGGER.debug("sr=" + sr);
                if (attrsResult != null) {
                    Attribute attmember = attrsResult.get(GinaApiLdapUtils.ATTRIBUTE_MEMBER);

                    if (attmember != null) {
                        for (int j = 0; j < attmember.size(); j++) {
                            String member = (String) attmember.get(j);

                            if (member != null) {
                                String username = member.substring(0, member.indexOf(',')).replace("cn=", "")
                                        .toLowerCase();
                                if (!users.contains(username)) {
                                    users.add(username);
                                    Map<String, String> map = getUserAttrs(username, attrs, false);
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
