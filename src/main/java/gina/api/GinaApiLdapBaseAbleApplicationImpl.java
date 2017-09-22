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

import gina.api.util.GinaApiLdapUtils;

public class GinaApiLdapBaseAbleApplicationImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleApplicationImpl.class);
    
    // Variable system user name
    private static final String USER_NAME = "user.name";

    // Constructeur
    public GinaApiLdapBaseAbleApplicationImpl(DirContext ctxtDir) {
	this.ctxtDir = ctxtDir;
    }
    
    /*
     * (non-Javadoc) Donne les valeurs des attributs passé en paramètre pour
     * l'utilisateur courant
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String[] attrs) throws GinaException, RemoteException {
	Arrays.asList(attrs).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	String user = System.getProperty(USER_NAME);

	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		Attributes attributes = sr.getAttributes();
		for (int i = 0; i < attrs.length; i++) {
		    NamingEnumeration<?> nameEnum = attributes.get(attrs[i]).getAll();
		    String value = "";
		    while (nameEnum.hasMoreElements()) {
			if (value.isEmpty()) {
			    value = (String) nameEnum.next();
			} else {
			    value = value + ":" + (String) nameEnum.next();
			}
		    }
		    logger.debug("value=" + value);
		    myMap.put(attrs[i], value);
		}
	    }
	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return myMap;
    }


    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur courant à le role donné pour
     * l'application donnée
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */

    public boolean hasRole(String role) throws GinaException, RemoteException {
	init();

	String user = System.getProperty(USER_NAME);
	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=groups)(cn=" + role + ")(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		return true;
	    }

	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return false;
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur donné à le role donné pour
     * l'application donnée
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String role) throws GinaException, RemoteException {
	// new version
	init();

	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + ")&(objectClass=memberOf)(cn=" + role + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		return true;
	    }

	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return false;
    }

    /*
     * (non-Javadoc) Donne tous les rôles de l'utilisateur courant
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    @Override
    public List<String> getRoles() throws GinaException, RemoteException {
	init();
	List<String> roles = new ArrayList<String>();
	String user = System.getProperty(USER_NAME);
	logger.debug("user=" + user);
	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		Attributes attributes = sr.getAttributes();
		if (attributes != null) {
		    NamingEnumeration<?> nameEnum = attributes.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
		    String value = "";
		    while (nameEnum.hasMoreElements()) {
			String role = (String) nameEnum.next();
			roles.add(role);
		    }
		    logger.debug("value=" + value);
		}
	    }
	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return roles;
    }

    /*
     * (non-Javadoc) Donne tous les rôles de l'utilisateur passé en paramètre
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user) throws GinaException, RemoteException {
	init();
	List<String> roles = new ArrayList<String>();
	try {

	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		Attributes attributes = sr.getAttributes();
		NamingEnumeration<?> nameEnum = attributes.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
		while (nameEnum.hasMoreElements()) {
		    String role = (String) nameEnum.next();
		    if (StringUtils.isNotBlank(role)) {
			String roleClean = StringUtils.replaceOnce(role, "cn=", "");
			String[] roleCleanString = StringUtils.split(roleClean, ",", 2);
			roles.add(roleCleanString[0]);
		    }
		}
	    }
	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return roles;
    }

    /*
     * (non-Javadoc) Retoune les roles d'une application
     * 
     * @see gina.api.GinaApiLdapBaseAble#getAppRoles(java.lang.String)
     */
    @Override
    public List<String> getAppRoles(String appli) throws GinaException, RemoteException {

	// new version
	init();
	List<String> roles = new ArrayList<String>();

	try {
	    SearchControls searchControls = getSearchControls();

	    String searchFilter = "cn=*";
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Groups,ou=" + appli + "", "(&(cn=*))", searchControls);
	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		NamingEnumeration<?> nameEnum = sr.getAttributes().get("cn").getAll();
		if (nameEnum != null) {
		    while (nameEnum.hasMoreElements()) {
			String role = (String) nameEnum.next();
			logger.debug("role=" + role);
			roles.add(role);
		    }

		}

	    }

	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return roles;
    }

    @Override
    public List<Map<String, String>> getUsers(String application, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Groups,ou=" + ginaApplication, "(&(cn=*))", searchControls);

	    if (answer != null) {
		logger.debug("answer=" + answer);
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("sr=" + sr);
		    Attributes attrs = sr.getAttributes();

		    if (attrs != null) {
			Attribute attmember = attrs.get("member");
			    logger.debug("attmember=" + attmember);

			if (attmember != null) {
			    for (int j = 0; j < attmember.size(); j++) {
				String member = (String) attmember.get(j);

				if (member != null) {
				    logger.debug("member=" + member);

				    String username = member.substring(0, member.indexOf(",")).replace("cn=", "")
					    .toLowerCase();
				    logger.debug("username=" + username);
				    if (StringUtils.isNotBlank(username) && !users.contains(username)) {
					Map<String, String> map = new HashMap<String, String>();
					users.add(username);
					map = this.getUserAttrs(username, paramArrayOfString);
					list.add(map);
				    }
				}
			    }
			}
		    }
		}
	    }
	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
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
	    throws GinaException, RemoteException {
	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=" + role + "))",
		    searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("name : " + sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));

		    Attributes attrs = sr.getAttributes();
		    logger.debug("sr=" + sr);
		    if (attrs != null) {
			Attribute attmember = attrs.get("member");
			logger.debug("attmember=" + attmember);

			if (attmember != null) {
			    for (int j = 0; j < attmember.size(); j++) {
				String member = (String) attmember.get(j);

				if (member != null) {
				    String username = member.substring(0, member.indexOf(",")).replace("cn=", "")
					    .toLowerCase();
				    if (!users.contains(username)) {
					Map<String, String> map = new HashMap<String, String>();
					users.add(username);
					map = this.getUserAttrs(username, paramArrayOfString);
					list.add(map);
				    }
				}
			    }
			}
		    }
		}
	    }
	} catch (NamingException e) {
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	return list;
    }

    @Override
    public boolean hasRole(String application, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasUserRole(String user, String application, String role)
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getRoles(String application) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getAllUsers(String filter, String attrs[])
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

}
