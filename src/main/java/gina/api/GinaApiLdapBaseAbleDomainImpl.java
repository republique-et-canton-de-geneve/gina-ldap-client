package gina.api;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class GinaApiLdapBaseAbleDomainImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleDomainImpl.class);

    // Constructeur
    public GinaApiLdapBaseAbleDomainImpl(DirContext ctxtDir) {
	this.ctxtDir = ctxtDir;
    }

    /*
     * (non-Javadoc) Donne les valeurs des attributs pass� en param�tre pour
     * l'utilisateur pass� en param�tre
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String,
     * java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String user, String[] paramArrayOfString)
	    throws GinaException, RemoteException {

	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();

	init();
	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();

		    Attributes attrs = sr.getAttributes();
		    logger.debug("sr=" + sr);
		    if (attrs != null) {
			for (int i = 0; i < paramArrayOfString.length; i++) {
			    Attribute attribute = sr.getAttributes().get(paramArrayOfString[i]);
			    if (attribute != null) {
				NamingEnumeration<?> nameEnum = attribute.getAll();
				String value = "";
				while (nameEnum.hasMoreElements()) {
				    if (value.isEmpty()) {
					value = (String) nameEnum.next();
				    } else {
					value = value + ":" + (String) nameEnum.next();
				    }
				}
				logger.debug("value=" + value);
				myMap.put(paramArrayOfString[i], value);
			    }
			}
		    }
		}
	    }
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return myMap;
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur donn� � le role donn� pour
     * l'application donn�e
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String application, String role) throws GinaException, RemoteException {
	init();

	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=" + role + "))", searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		if (sr != null) {
		    logger.debug("sr=" + sr);
		    Attributes attrs = sr.getAttributes();
		    if (attrs != null && attrs.get("member") != null) {

			NamingEnumeration<?> answerAtt = sr.getAttributes().get("member").getAll();
			while (answerAtt.hasMoreElements()) {
			    String att = (String) answerAtt.next();
			    if (att.toUpperCase().contains(user.toUpperCase())) {
				return true;
			    }
			}
		    }
		}
	    }
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return false;
    }

    /*
     * (non-Javadoc) Donne tous les r�les de l'utilisateur pass� en param�tre
     * pour l'application pass�e en param�tre.
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user, String application) throws GinaException, RemoteException {
	init();
	List<String> roles = new ArrayList<String>();
	try {
	    String ginaDomain = GinaApiLdapUtils.extractDomain(application);
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    if (sr != null) {
			final Attributes attrs = sr.getAttributes();
			logger.debug(attrs);
			if (attrs != null && attrs.get("memberOf") != null) {
			    NamingEnumeration<?> answerAtt = sr.getAttributes().get("memberOf").getAll();
			    while (answerAtt.hasMoreElements()) {
				String att = (String) answerAtt.next();
				logger.debug(att);
				String pattern = ",ou=Groups,ou=" + ginaApplication + ",ou=" + ginaDomain + ",o=gina";
				if (StringUtils.isNotBlank(att) && att.contains(pattern)) {
				    String roleClean = StringUtils.replaceOnce(att, "cn=", "");
				    roleClean = StringUtils.replaceOnce(roleClean, pattern, "");
				    roles.add(roleClean);
				}
			    }
			}
		    }
		}
	    }
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	logger.debug("roles=" + roles);

	return roles;
    }

    /*
     * (non-Javadoc) retoune les roles d'une application
     * 
     * @see gina.api.GinaApiLdapBaseAble#getAppRoles(java.lang.String)
     */
    @Override
    public List<String> getAppRoles(String application) throws GinaException, RemoteException {

	init();
	List<String> roles = new ArrayList<String>();
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    NamingEnumeration<?> att = sr.getAttributes().get("cn").getAll();
		    while (att.hasMoreElements()) {
			String cn = (String) att.next();
			logger.debug("cn=" + cn);
			roles.add(cn);
		    }

		}
	    }
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return roles;
    }

    /*
     * (non-Javadoc) Donne la liste des utilisateurs ayant acc�s � l'application
     * pass�e en param�tre, avec les attributs demand�s
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String)
     */
    @Override
    public List<Map<String, String>> getUsers(String application, String attrs[])
	    throws GinaException, RemoteException {
	init();
	List<Map<String, String>> list;
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=*))", searchControls);

	    list = parseAnswer(answer, attrs);
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return list;
    }

    /*
     * (non-Javadoc) Donne la liste des utilisateurs ayant acc�s � l'application
     * pass�e en param�tre pour le r�le donn�, avec les attributs demand�s
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public List<Map<String, String>> getUsers(String application, String role, String attrs[])
	    throws GinaException, RemoteException {
	init();
	List<Map<String, String>> list;
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=" + role + "))",
		    searchControls);

	    list = parseAnswer(answer, attrs);
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return list;
    }
    
    
    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Override
    public List<Map<String, String>> getAllUsers(String filter, String attrs[])
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    /*
     * (non-Javadoc) Donne les valeurs des attributs pass� en param�tre pour
     * l'utilisateur courant
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String attrs[]) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasRole(String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur courant � le role donn� pour
     * l'application donn�e
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasRole(String application, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    /*
     * (non-Javadoc) Donne tous les r�les de l'utilisateur courant pour
     * l'application pass�e en param�tre
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    @Override
    public List<String> getRoles(String application) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasUserRole(String user, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getRoles() throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getUserRoles(String user) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    // -----------------------------------------------------------------------------------------
    // METHODES UTILITAIRES
    // -----------------------------------------------------------------------------------------

    private List<Map<String, String>> parseAnswer(final NamingEnumeration<?> answer, String attrs[])
	    throws NamingException, GinaException, RemoteException {
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();

	if (answer != null) {
	    List<String> users = new ArrayList<String>();
	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("name : " + sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));

		Attributes attrsResult = sr.getAttributes();
		logger.debug("sr=" + sr);
		if (attrsResult != null) {
		    Attribute attmember = attrsResult.get("member");

		    if (attmember != null) {
			for (int j = 0; j < attmember.size(); j++) {
			    String member = (String) attmember.get(j);

			    if (member != null) {
				String username = member.substring(0, member.indexOf(",")).replace("cn=", "")
					.toLowerCase();
				if (!users.contains(username)) {
				    Map<String, String> map = new HashMap<String, String>();
				    users.add(username);
				    map = this.getUserAttrs(username, attrs);
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

    @Override
    public void setInitTest(DirContext ctxtDir) throws GinaException {
	if (this.ctxtDir == null) {
	    this.ctxtDir = ctxtDir;
	    if (this.ctxtDir == null) {
		throw new GinaException("initialisation impossible");
	    }
	}
    }

}
