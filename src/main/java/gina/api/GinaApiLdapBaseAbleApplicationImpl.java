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
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;

public class GinaApiLdapBaseAbleApplicationImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleApplicationImpl.class);

    // Variable system user name
    private static final String USER_NAME = "user.name";

    // Constructeur
    public GinaApiLdapBaseAbleApplicationImpl(GinaApiLdapConfiguration ldapConf) {
	Validate.notNull(ldapConf);
	this.ldapConf = ldapConf;
    }

    /*
     * (non-Javadoc) Donne les valeurs des attributs pass� en param�tre pour
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
	    searchControls.setReturningAttributes(attrs);
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);
	    if (answer != null) {
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
			GinaApiLdapUtils.closeQuietly(nameEnum);
			logger.debug("value=" + value);
			myMap.put(attrs[i], value);
		    }
		}

		GinaApiLdapUtils.closeQuietly(answer);
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
	}

	return myMap;
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur courant � le role donn� pour
     * l'application donn�e
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */

    public boolean hasRole(String role) throws GinaException, RemoteException {
	init();
	String user = System.getProperty(USER_NAME);
	return hasUserRole(user, role);
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur donn� � le role donn� pour
     * l'application donn�e
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String role) throws GinaException, RemoteException {
	init();

	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + ")&(objectClass=memberOf)(cn=" + role + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);
	    boolean result = (answer != null && answer.hasMoreElements());

	    GinaApiLdapUtils.closeQuietly(answer);

	    return result;
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
	}
    }

    /*
     * (non-Javadoc) Donne tous les r�les de l'utilisateur courant
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    @Override
    public List<String> getRoles() throws RemoteException {
	init();
	List<String> roles = new ArrayList<String>();
	String user = System.getProperty(USER_NAME);
	logger.debug("user=" + user);
	try {
	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBEROF });
	    String searchFilter = "(&(objectClass=users)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);
	    if (answer != null) {
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

			GinaApiLdapUtils.closeQuietly(nameEnum);
		    }
		}
	    }
	    GinaApiLdapUtils.closeQuietly(answer);
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
	}

	return roles;
    }

    /*
     * (non-Javadoc) Donne tous les r�les de l'utilisateur pass� en param�tre
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user) throws GinaException, RemoteException {
	init();
	List<String> roles = new ArrayList<String>();
	try {

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBEROF });
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
		nameEnum.close();
	    }

	    GinaApiLdapUtils.closeQuietly(answer);
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
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
	init();
	List<String> roles = new ArrayList<String>();

	try {
	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_CN });

	    NamingEnumeration<?> answer = ctxtDir.search("ou=Groups,ou=" + appli + "", "(&(cn=*))", searchControls);
	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("sr=" + sr);
		    NamingEnumeration<?> nameEnum = sr.getAttributes().get(GinaApiLdapUtils.ATTRIBUTE_CN).getAll();
		    if (nameEnum != null) {
			while (nameEnum.hasMoreElements()) {
			    String role = (String) nameEnum.next();
			    logger.debug("role=" + role);
			    roles.add(role);
			}
			GinaApiLdapUtils.closeQuietly(nameEnum);
		    }
		}

		GinaApiLdapUtils.closeQuietly(answer);
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
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

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Groups,ou=" + ginaApplication, "(&(cn=*))",
		    searchControls);

	    if (answer != null) {
		logger.debug("answer=" + answer);
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("sr=" + sr);
		    Attributes attrs = sr.getAttributes();

		    if (attrs != null) {
			Attribute attmember = attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBER);
			logger.debug("attmember=" + attmember);

			if (attmember != null) {
			    for (int j = 0; j < attmember.size(); j++) {
				String member = (String) attmember.get(j);

				if (member != null) {
				    logger.debug("member=" + member);

				    String username = member.substring(0, member.indexOf(',')).replace("cn=", "")
					    .toLowerCase();
				    logger.debug("username=" + username);
				    if (StringUtils.isNotBlank(username) && !users.contains(username)) {
					users.add(username);
					Map<String, String> map = this.getUserAttrs(username, paramArrayOfString,
						false);
					list.add(map);
				    }
				}
			    }
			}
		    }
		}
		GinaApiLdapUtils.closeQuietly(answer);
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
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
    public List<Map<String, String>> getUsers(String application, String role, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + ginaApplication, "(&(cn=" + role + "))",
		    searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("name : " + sr.getName().substring(0, sr.getName().indexOf(',')).replace("cn=", ""));

		    Attributes attrs = sr.getAttributes();
		    logger.debug("sr=" + sr);
		    if (attrs != null) {
			Attribute attmember = attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBER);
			logger.debug("attmember=" + attmember);

			if (attmember != null) {
			    for (int j = 0; j < attmember.size(); j++) {
				String member = (String) attmember.get(j);

				if (member != null) {
				    String username = member.substring(0, member.indexOf(',')).replace("cn=", "")
					    .toLowerCase();
				    if (!users.contains(username)) {
					users.add(username);
					Map<String, String> map = this.getUserAttrs(username, paramArrayOfString, false);
					list.add(map);
				    }
				}
			    }
			}
		    }
		}

		GinaApiLdapUtils.closeQuietly(answer);
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    closeDirContext();
	}

	return list;
    }

    @Override
    public boolean hasRole(String application, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasUserRole(String user, String application, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getRoles(String application) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getAllUsers(String filter, String attrs[]) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

}
