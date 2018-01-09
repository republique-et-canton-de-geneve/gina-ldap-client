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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;

public class GinaApiLdapBaseAbleApplicationImpl extends GinaApiLdapBaseAbleCommon {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleApplicationImpl.class);

    // Constructeur
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
    public boolean hasUserRole(String user, String role) throws RemoteException {
	init();
	NamingEnumeration<?> answer = null;

	try {
	    SearchControls searchControls = getSearchControls();
	    String searchFilter = "(&(objectClass=users)(cn=" + user + ")&(objectClass=memberOf)(cn=" + role + "))";
	    answer = ctxtDir.search("", searchFilter, searchControls);
	    return (answer != null && answer.hasMoreElements());
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
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
	init();
	List<String> roles = new ArrayList<String>();
	NamingEnumeration<?> answer = null;
	NamingEnumeration<?> nameEnum = null;
	try {

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBEROF });
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		Attributes attributes = sr.getAttributes();
		nameEnum = attributes.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
		while (nameEnum.hasMoreElements()) {
		    String role = (String) nameEnum.next();
		    if (StringUtils.isNotBlank(role)) {
			String roleClean = StringUtils.replaceOnce(role, "cn=", "");
			String[] roleCleanString = StringUtils.split(roleClean, ",", 2);
			roles.add(roleCleanString[0]);
		    }
		}
		GinaApiLdapUtils.closeQuietly(nameEnum);
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(nameEnum);
	    GinaApiLdapUtils.closeQuietly(answer);
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
	NamingEnumeration<?> answer = null;
	NamingEnumeration<?> nameEnum = null;

	try {
	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_CN });

	    answer = ctxtDir.search(GinaApiLdapUtils.getLdapFilterGroup(appli), GinaApiLdapUtils.getLdapFilterCn("*"), searchControls);
	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("sr=" + sr);
		    nameEnum = sr.getAttributes().get(GinaApiLdapUtils.ATTRIBUTE_CN).getAll();
		    if (nameEnum != null) {
			while (nameEnum.hasMoreElements()) {
			    String role = (String) nameEnum.next();
			    logger.debug("role=" + role);
			    roles.add(role);
			}
			GinaApiLdapUtils.closeQuietly(nameEnum);
		    }
		}
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(nameEnum);
	    GinaApiLdapUtils.closeQuietly(answer);
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
	NamingEnumeration<?> answer = null;
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
	    answer = ctxtDir.search(GinaApiLdapUtils.getLdapFilterGroup(ginaApplication), GinaApiLdapUtils.getLdapFilterCn("*"), searchControls);

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
	    }
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
    public List<Map<String, String>> getUsers(String application, String role, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	NamingEnumeration<?> answer = null;
	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
	    answer = ctxtDir.search(GinaApiLdapUtils.getLdapFilterOu(ginaApplication), GinaApiLdapUtils.getLdapFilterCn(role), searchControls);

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
					Map<String, String> map = this.getUserAttrs(username, paramArrayOfString,
						false);
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
	} finally {
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
	}

	return list;
    }

}
