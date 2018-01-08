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

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;

public class GinaApiLdapBaseAbleDomainImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleDomainImpl.class);

    // Constructeur
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
    public List<String> getAppRoles(String application) throws GinaException, RemoteException {
	NamingEnumeration<?> answer = null;
	NamingEnumeration<?> att = null;

	init();
	List<String> roles = new ArrayList<String>();
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_CN });
	    answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    att = sr.getAttributes().get(GinaApiLdapUtils.ATTRIBUTE_CN).getAll();
		    while (att.hasMoreElements()) {
			String cn = (String) att.next();
			logger.debug("cn=" + cn);
			roles.add(cn);
		    }
		    GinaApiLdapUtils.closeQuietly(att);
		}
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(att);
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
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
    public List<Map<String, String>> getUsers(String application, String attrs[])
	    throws GinaException, RemoteException {
	init();
	List<Map<String, String>> list;
	NamingEnumeration<?> answer = null;
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=*))", searchControls);

	    list = parseAnswer(answer, attrs);

	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
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
    public List<Map<String, String>> getUsers(String application, String role, String attrs[])
	    throws GinaException, RemoteException {
	init();
	List<Map<String, String>> list;
	NamingEnumeration<?> answer = null;
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=" + role + "))", searchControls);

	    list = parseAnswer(answer, attrs);
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
	}

	return list;
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Override
    public boolean hasRole(String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasUserRole(String user, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getUserRoles(String user) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    // -----------------------------------------------------------------------------------------
    // METHODES UTILITAIRES
    // -----------------------------------------------------------------------------------------

    private List<Map<String, String>> parseAnswer(final NamingEnumeration<?> answer, final String[] attrs)
	    throws NamingException, GinaException, RemoteException {
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();

	if (answer != null) {
	    List<String> users = new ArrayList<String>();
	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("name : " + sr.getName().substring(0, sr.getName().indexOf(',')).replace("cn=", ""));

		Attributes attrsResult = sr.getAttributes();
		logger.debug("sr=" + sr);
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
				    Map<String, String> map = this.getUserAttrs(username, attrs, false);
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
