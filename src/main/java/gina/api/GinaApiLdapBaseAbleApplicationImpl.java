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
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import gina.api.util.Configuration;

public class GinaApiLdapBaseAbleApplicationImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleApplicationImpl.class);

    private DirContext ctxtDir = null;

    private void init() throws GinaException {
	if (ctxtDir == null) {
	    logger.info("init()");

	    Configuration conf = new Configuration();
	    conf.init(Configuration.APPLICATION);

	    ctxtDir = conf.getCtxtDir();
	    if (ctxtDir == null) {
		throw new GinaException("initialisation impossible");
	    }
	}
    }

    /*
     * (non-Javadoc) retourne boolean pour savoir si le user est valide
     * 
     * @see gina.api.GinaApiLdapBaseAble#isValidUser(java.lang.String)
     */
    @Override
    public boolean isValidUser(String user) throws GinaException, RemoteException {
	// new version
	init();
	try {
	    SearchControls searchControls = getSearchControls();
	    Attributes matchAttrs = new BasicAttributes(true);
	    matchAttrs.put(new BasicAttribute("cn", user));
	    String searchFilter = "(&(objectClass=user)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    logger.info("answer : ");
	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.info("sr : " + sr);
		Attributes attrs = sr.getAttributes();
		NamingEnumeration<? extends Attribute> attributEnum = attrs.getAll();
		while (attributEnum.hasMoreElements()) {
		    Attribute at = attributEnum.next();
		    NamingEnumeration<?> nameEnum = sr.getAttributes().get(at.getID()).getAll();
		    while (nameEnum.hasMoreElements()) {
			String s = (String) nameEnum.next();
			logger.info("value: " + s);
		    }
		}
		if (attrs != null) {
		    Attribute attmember = attrs.get("cn");
		    logger.info(attmember.getID());
		}
		String name = (String) sr.getAttributes().get("cn").get();
		logger.info("name:" + name);
		if (user.equalsIgnoreCase(name)) {
		    return true;
		}

	    }
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return false;

    }

    /*
     * (non-Javadoc) Donne les valeurs des attributs passé en paramètre pour
     * l'utilisateur passé en paramètre
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String,
     * java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String user, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	// new version
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();

	init();
	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=user)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		Attributes attrs = sr.getAttributes();
		for (int i = 0; i < paramArrayOfString.length; i++) {
		    NamingEnumeration<?> nameEnum = sr.getAttributes().get(paramArrayOfString[i]).getAll();
		    String value = "";
		    while (nameEnum.hasMoreElements()) {
			if (value.isEmpty()) {
			    value = (String) nameEnum.next();
			} else {
			    value = value + ":" + (String) nameEnum.next();
			}
		    }
		    logger.info("value: " + value);
		    myMap.put(paramArrayOfString[i], value);

		}

	    }

	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return myMap;

    }

    /*
     * (non-Javadoc) Donne les valeurs des attributs passé en paramètre pour
     * l'utilisateur courant
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String[] paramArrayOfString) throws GinaException, RemoteException {
	// new version
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	String user = System.getProperty("user.name");

	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    String searchFilter = "(&(objectClass=user)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		Attributes attrs = sr.getAttributes();
		for (int i = 0; i < paramArrayOfString.length; i++) {
		    NamingEnumeration<?> nameEnum = sr.getAttributes().get(paramArrayOfString[i]).getAll();
		    String value = "";
		    while (nameEnum.hasMoreElements()) {
			if (value.isEmpty()) {
			    value = (String) nameEnum.next();
			} else {
			    value = value + ":" + (String) nameEnum.next();
			}
		    }
		    logger.info("value: " + value);
		    myMap.put(paramArrayOfString[i], value);

		}
	    }
	} catch (NamingException e) {
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
	// new version
	init();

	String user = System.getProperty("user.name");
	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=groups)(cn=" + role + ")(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.info("sr : " + sr);
		return true;
	    }

	} catch (NamingException e) {
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
		logger.info("sr : " + sr);
		return true;
	    }

	} catch (NamingException e) {
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
	// new version
	init();
	List<String> roles = new ArrayList<String>();
	String user = System.getProperty("user.name");
	logger.info("user: " + user);
	String role = "";
	try {

	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		Attributes a = sr.getAttributes();
		NamingEnumeration<?> nameEnum = sr.getAttributes().get("memberOf").getAll();
		String value = "";
		while (nameEnum.hasMoreElements()) {
		    role = (String) nameEnum.next();
		    roles.add(role);

		}
		logger.info("value: " + value);
	    }
	} catch (NamingException e) {
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
	// new version
	init();
	List<String> roles = new ArrayList<String>();
	String role = "";
	try {

	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.info("sr : " + sr);
		Attributes a = sr.getAttributes();
		NamingEnumeration<?> nameEnum = sr.getAttributes().get("memberOf").getAll();
		String value = "";
		while (nameEnum.hasMoreElements()) {
		    role = (String) nameEnum.next();
		    roles.add(role);

		}
		logger.info("value: " + value);
	    }

	} catch (NamingException e) {
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
		logger.info("sr: " + sr);
		NamingEnumeration<?> nameEnum = sr.getAttributes().get("cn").getAll();
		if (nameEnum != null) {
		    while (nameEnum.hasMoreElements()) {
			String role = (String) nameEnum.next();
			logger.info("role: " + role);
			roles.add(role);
		    }

		}

	    }

	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
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
    public List<Map<String, String>> getUsers(String appli, String[] paramArrayOfString)
	    throws GinaException, RemoteException {

	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli, "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    Attributes attrs = sr.getAttributes();

		    if (attrs != null) {
			Attribute attmember = attrs.get("member");

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
    public List<Map<String, String>> getUsers(String appli, String role, String[] paramArrayOfString)
	    throws GinaException, RemoteException {

	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {
	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli, "(&(cn=" + role + "))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.info("name : " + sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));

		    Attributes attrs = sr.getAttributes();
		    logger.info("sr : " + sr);
		    if (attrs != null) {
			Attribute attmember = attrs.get("member");

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
    public List<String> getUserRoles(String user, String application) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getAllUsers(String filter, String attrs[])
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
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
