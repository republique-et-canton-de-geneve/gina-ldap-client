package gina.impl;

import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapEncoder;
import gina.impl.util.GinaLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GinaLdapDomain extends GinaLdapCommon {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapDomain.class);

    public GinaLdapDomain(GinaLdapConfiguration ldapConf) {
        super(ldapConf);
    }

    @Override
    public List<String> getAppRoles(String application) {
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);

        NamingEnumeration<?> answer = null;
        NamingEnumeration<?> att = null;
        List<String> roles = new ArrayList<String>();
        try {
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls(new String[] { GinaLdapUtils.ATTRIBUTE_CN });
            answer = getLdapContext()
                    .search(GinaLdapUtils.getLdapFilterOu(ginaApplication), GinaLdapUtils.getLdapFilterCn("*"),
                            searchControls);

            if (answer != null) {
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    try {
                        att = sr.getAttributes().get(GinaLdapUtils.ATTRIBUTE_CN).getAll();
                        while (att.hasMoreElements()) {
                            String cn = (String) att.next();
                            LOGGER.debug("cn = {}", cn);
                            roles.add(cn);
                        }
                    } finally {
                        GinaLdapUtils.closeQuietly(att);
                    }
                }
            }
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
        }

        return roles;
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String[] attrs) {
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);

        List<Map<String, String>> list;
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);

            SearchControls searchControls = getSearchControls();
            answer = getLdapContext()
                    .search(GinaLdapUtils.getLdapFilterOu(ginaApplication), GinaLdapUtils.getLdapFilterCn("*"),
                            searchControls);

            list = parseAnswer(answer, attrs);

        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
        }

        return list;
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String role, String[] attrs) {
        final String encodedApplication = GinaLdapEncoder.filterEncode(application);
        final String encodedRole = GinaLdapEncoder.filterEncode(role);

        List<Map<String, String>> list;
        NamingEnumeration<?> answer = null;
        try {
            String ginaApplication = GinaLdapUtils.extractApplication(encodedApplication);
            SearchControls searchControls = getSearchControls();
            answer = getLdapContext().search(
                    GinaLdapUtils.getLdapFilterOu(ginaApplication),
                    GinaLdapUtils.getLdapFilterCn(encodedRole),
                    searchControls);
            list = parseAnswer(answer, attrs);
        } catch (NamingException e) {
            logException(e);
            throw new GinaException(e.getMessage());
        } finally {
            GinaLdapUtils.closeQuietly(answer);
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
                String name = sr.getName().substring(0, sr.getName().indexOf(',')).replace("cn=", "");
                LOGGER.debug("name = {}", name);

                Attributes attrsResult = sr.getAttributes();
                LOGGER.debug("sr = {}", sr);
                if (attrsResult != null) {
                    Attribute attmember = attrsResult.get(GinaLdapUtils.ATTRIBUTE_MEMBER);

                    if (attmember != null) {
                        for (int j = 0; j < attmember.size(); j++) {
                            String member = (String) attmember.get(j);

                            if (member != null) {
                                String username = member.substring(0, member.indexOf(',')).replace("cn=", "")
                                        .toLowerCase();
                                if (!users.contains(username)) {
                                    users.add(username);
                                    Map<String, String> map = getUserAttrs(username, attrs);
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
