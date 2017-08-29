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

import org.apache.log4j.Logger;

import gina.api.util.Configuration;

public class GinaApiLdapBaseAbleDomainImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleDomainImpl.class);

    private DirContext ctxtDir = null;

    private void init() throws GinaException {
	if (ctxtDir == null) {
	    logger.info("init()");

	    Configuration conf = new Configuration();
	    conf.init(Configuration.DOMAIN);
	    
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
	init();
	try {
	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users", "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.info("sr" + sr);
		    String name = sr.getName().replace("cn=", ""); 
		    if (user.equalsIgnoreCase(name)) {
			return true;
		    }
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

	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();

	init();
	try {
	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users", "(&(cn=" + user + "))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();

		    Attributes attrs = sr.getAttributes();
		    logger.debug("sr : " + sr);
		    if (attrs != null) {
			for (int i = 0; i < paramArrayOfString.length; i++) {

			    Attribute attribute = sr.getAttributes().get(paramArrayOfString[i]);
			    if(attribute != null) {
			    NamingEnumeration<?> nameEnum = attribute.getAll();
			    String value = "";
			    while (nameEnum.hasMoreElements()) {
				if (value.isEmpty()) {
				    value = (String) nameEnum.next();
				} else {
				    value = value + ":" + (String) nameEnum.next();
				}
			    }
			    logger.debug("value: " + value);
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
     * (non-Javadoc) Retourne vrai si l'utilisateur donné à le role donné pour
     * l'application donnée
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String appli, String role) throws GinaException, RemoteException {

	init();
	List<String> users = new ArrayList<String>();

	try {
	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli, "(&(cn=" + role + "))", searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();

		Attributes attrs = sr.getAttributes();
		if (sr.getAttributes().get("member") != null) {

		    NamingEnumeration<?> answerAtt = sr.getAttributes().get("member").getAll();
		    while (answerAtt.hasMoreElements()) {
			String att = (String) answerAtt.next();
			if (att.toUpperCase().contains(user.toUpperCase())) {
			    return true;
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
     * (non-Javadoc) Donne tous les rôles de l'utilisateur passé en paramètre
     * pour l'application passée en paramètre.
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user, String appli) throws GinaException, RemoteException {

	init();
	List<String> roles = new ArrayList<String>();
	String role = "";
	try {

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users", "(&(cn=" + user + "))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();

		    Attributes attrs = sr.getAttributes();
		    if (sr.getAttributes().get("memberOf") != null) {

			NamingEnumeration<?> answerAtt = sr.getAttributes().get("memberOf").getAll();
			while (answerAtt.hasMoreElements()) {
			    String att = (String) answerAtt.next();
			    logger.debug(att);
			    roles.add(att);
			}
		    }

		}
	    }
	} catch (NamingException e) {
	    throw new GinaException(e.getMessage());
	}

	return roles;
    }

    /*
     * (non-Javadoc) retoune les roles d'une application
     * 
     * @see gina.api.GinaApiLdapBaseAble#getAppRoles(java.lang.String)
     */
    @Override
    public List<String> getAppRoles(String appli) throws GinaException, RemoteException {

	init();
	List<String> roles = new ArrayList<String>();
	try {

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli, "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    NamingEnumeration<?> att = sr.getAttributes().get("cn").getAll();
		    while (att.hasMoreElements()) {
			String cn = (String) att.next();
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
     * (non-Javadoc) Donne la liste des utilisateurs ayant accès à l'application
     * passée en paramètre, avec les attributs demandés
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String)
     */
    @Override
    public List<Map<String, String>> getUsers(String application, String attrs[])
	    throws GinaException, RemoteException {

	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + application, "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.info("name : " + sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));

		    Attributes attrsResult = sr.getAttributes();
		    logger.info("sr : " + sr);
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
    public List<Map<String, String>> getUsers(String application, String role, String attrs[])
	    throws GinaException, RemoteException {

	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {

	    SearchControls searchControls = getSearchControls();
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + application, "(&(cn=" + role + "))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.info("name : " + sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));

		    Attributes attrsResult = sr.getAttributes();
		    logger.info("sr : " + sr);
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
     * (non-Javadoc) Donne les valeurs des attributs passé en paramètre pour
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
     * (non-Javadoc) Retourne vrai si l'utilisateur courant à le role donné pour
     * l'application donnée
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasRole(String application, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    /*
     * (non-Javadoc) Donne tous les rôles de l'utilisateur courant pour
     * l'application passée en paramètre
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
