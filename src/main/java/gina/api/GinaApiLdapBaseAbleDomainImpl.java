package gina.api;

import java.io.IOException;
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
import ch.ge.cti.ct.act.configuration.DistributionFactory;

public class GinaApiLdapBaseAbleDomainImpl implements GinaApiLdapBaseAble, GinaApiLdapConfig {

    private DirContext ctxtDir = null;
    // LOGGER

    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleDomainImpl.class);

    int maxTimeLimit;

    private void init() throws GinaException {
	if (ctxtDir == null) {
	    logger.info("init()");
	    DistributionFactory.setDisableJNDI(true);
	    try {
		maxTimeLimit = DistributionFactory.getConfiguration().getInt("timeout-search-ldap");
	    } catch (IOException e) {
		logger.warn("Impossible de setter maxTimeLimit dafult(3000)");
		maxTimeLimit = 3000;
	    }
	    Configuration conf = new Configuration();
	    conf.init("domain");
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
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users", "(&(cn=*))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.info("sr" + sr);
		    String name = sr.getName().replace("cn=", ""); // .substring(0,
								   // sr.getName().indexOf(":")).replace("cn=",
								   // "");
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

    @Override
    public List<Map<String, String>> getAllUsers(String paramString, String[] paramArrayOfString)
	    throws GinaException, RemoteException {

	return null;
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
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users", "(&(cn=" + user + "))", searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();

		    Attributes attrs = sr.getAttributes();
		    logger.debug("sr : " + sr);
		    if (attrs != null) {
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
			    logger.debug("value: " + value);
			    myMap.put(paramArrayOfString[i], value);

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
     * (non-Javadoc) Donne les valeurs des attributs passé en paramètre pour
     * l'utilisateur courant
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String[] paramArrayOfString) throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
    }

    @Override
    public String getLanguage() throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
    }

    @Override
    @Deprecated
    public String getEnvironment() throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur courant à le role donné pour
     * l'application donnée
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasRole(String appli, String role) throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
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
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);

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
     * (non-Javadoc) Donne tous les rôles de l'utilisateur courant pour
     * l'application passée en paramètre
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    @Override
    public List<String> getRoles(String appli) throws GinaException, RemoteException {
	
	throw new GinaException("Not implemented");
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

	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);
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

    @Override
    public List<String> getIntegrationUserRoles(String paramString1, String paramString2)
	    throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getIntegrationUserAttributes(String paramString1, String paramString2)
	    throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
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

	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);
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

    @Override
    public List<String> getBusinessRoles(String paramString) throws GinaException, RemoteException {

	throw new GinaException("Not implemented");
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

	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli, "(&(cn=*))", searchControls);

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

	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(maxTimeLimit);
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
    public List<Map<String, String>> getUsersByPhone(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<Map<String, String>> getUsersBySIRHNumber(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<Map<String, String>> getUsersByName(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getInheritingRoles(String paramString1, String paramString2) {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getPMProprieteMetier(String paramString) {
	throw new GinaException("Not implemented");
    }

    @Override
    public String getOwnIDUniqueForPPorPseudo() {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getOwnPMProprieteMetier(String paramString) {

	return null;
    }

    @Override
    public List<String> getPPProprieteMetier(String paramString) {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getOwnPPProprieteMetier(String paramString) {
	throw new GinaException("Not implemented");
    }

    @Override
    public boolean hasRole(String paramString1) throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public boolean hasUserRole(String user, String role) throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getRoles() throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getUserRoles(String user) throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
    }

    @Override
    public List<String> getAppRoles() throws GinaException, RemoteException {
	throw new GinaException("Not implemented");
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
