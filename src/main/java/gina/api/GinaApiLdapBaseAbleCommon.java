package gina.api;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;

public abstract class GinaApiLdapBaseAbleCommon implements GinaApiLdapBaseAble {

    // Logger
    private static final Logger logger = Logger.getLogger(GinaApiLdapBaseAbleCommon.class);

    // Message d'erreur pour les méthodes non implémentées
    protected static final String NOT_IMPLEMENTED = "Not implemented";

    //
    protected LdapContext ctxtDir = null;

    protected GinaApiLdapConfiguration ldapConf = null;

    protected void closeDirContext(boolean closeConnection) {
	if (closeConnection) {
	    closeDirContext();
	}
    }

    protected void closeDirContext() {
	if (ctxtDir != null) {
	    try {
		ctxtDir.close();
	    } catch (NamingException e) {
		logger.error(e);
	    }
	    ctxtDir = null;
	}
    }

    protected InitialLdapContext createDirContext() throws GinaException {
	Hashtable<String, String> env = new Hashtable<String, String>();

	env.put(Context.INITIAL_CONTEXT_FACTORY, GinaApiLdapConfiguration.LDAP_CONTEXT_FACTORY);
	env.put(Context.SECURITY_AUTHENTICATION, GinaApiLdapConfiguration.LDAP_AUTHENTICATION_MODE);
	env.put(Context.REFERRAL, GinaApiLdapConfiguration.LDAP_REFERRAL_MODE);
	env.put("java.naming.ldap.version", "3");

	env.put("com.sun.jndi.ldap.connect.pool", "true");

	env.put(Context.PROVIDER_URL, ldapConf.getLdapServerUrl() + "/" + ldapConf.getLdapBaseDn());

	if(StringUtils.isNotEmpty(ldapConf.getLdapUser())) {
	    env.put(Context.SECURITY_PRINCIPAL, ldapConf.getLdapUser());
	}

	if(StringUtils.isNotEmpty(ldapConf.getLdapPassword())) {
	    env.put(Context.SECURITY_CREDENTIALS, ldapConf.getLdapPassword());
	}

	env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(ldapConf.getLdapTimeLimit()));

	if(ldapConf.getLdapServerUrl().startsWith("ldaps")) {
	    env.put(Context.SECURITY_PROTOCOL, "ssl");
	}

	InitialLdapContext result;
	try {
	    result = new InitialLdapContext(env, null);
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	}

	return result;
    }

    protected SearchControls getSearchControls() {
	SearchControls searchControls = new SearchControls();
	searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	searchControls.setReturningObjFlag(false);

	return searchControls;
    }

    protected SearchControls getSearchControls(final String[] attributes) {
	final SearchControls searchControls = getSearchControls();

	if (attributes != null && attributes.length > 0) {
	    searchControls.setReturningAttributes(attributes);
	}

	return searchControls;
    }

    protected void init() throws GinaException {
	if (ctxtDir == null) {
	    this.ctxtDir = createDirContext();
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
	GinaApiLdapUtils.checkParam(user);
	
	init();
	NamingEnumeration<?> answer = null;
	try {
	    SearchControls searchControls = getSearchControls();
	    Attributes matchAttrs = new BasicAttributes(true);
	    matchAttrs.put(new BasicAttribute("cn", user));
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		Attributes attrs = sr.getAttributes();
		if (attrs != null) {
		    Attribute cn = attrs.get("cn");
		    if (cn != null) {
			String cnString = (String) cn.get();
			Attribute departmentNumber = attrs.get(GinaApiLdapUtils.ATTRIBUTE_DEPARTMENT_NUMBER);
			if (user.equalsIgnoreCase(cnString) && departmentNumber != null
				&& StringUtils.isNotBlank((String) departmentNumber.get())) {
			    return true;
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
	return this.getUserAttrs(user, paramArrayOfString, true);
    }

    public Map<String, String> getUserAttrs(String user, String[] paramArrayOfString, boolean closeConnection)
	    throws GinaException, RemoteException {
	GinaApiLdapUtils.checkParam(user);
	
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	NamingEnumeration<?> answer = null;
	NamingEnumeration<?> nameEnum = null;

	init();
	try {
	    SearchControls searchControls = getSearchControls(paramArrayOfString);
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    answer = ctxtDir.search("", searchFilter, searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    Attributes attrs = sr.getAttributes();
		    logger.debug("sr=" + sr);
		    if (attrs != null) {
			for (int i = 0; i < paramArrayOfString.length; i++) {
			    String attr = paramArrayOfString[i];
			    logger.debug("attr=" + attr);
			    Attribute attribute = attrs.get(attr);
			    if (attribute != null) {
				nameEnum = attribute.getAll();
				if (nameEnum != null) {
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
				    GinaApiLdapUtils.closeQuietly(nameEnum);
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
	    GinaApiLdapUtils.closeQuietly(nameEnum);
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext(closeConnection);
	}

	return myMap;
    }

    /*
     * (non-Javadoc) Donne tous les rôles de l'utilisateur passé en paramètre
     * pour l'application passée en paramètre.
     * 
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user, String application) throws GinaException, RemoteException {
	GinaApiLdapUtils.checkParam(user);
	GinaApiLdapUtils.checkParam(application);
	
	init();
	List<String> roles = new ArrayList<String>();
	NamingEnumeration<?> answer = null;
	NamingEnumeration<?> answerAtt = null;

	try {
	    String ginaDomain = GinaApiLdapUtils.extractDomain(application);
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBEROF });
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    answer = ctxtDir.search("", searchFilter, searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("sr=" + sr);
		    if (sr != null) {
			final Attributes attrs = sr.getAttributes();
			logger.debug("attrs=" + attrs);
			if (attrs != null && attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF) != null) {
			    answerAtt = attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
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
			    GinaApiLdapUtils.closeQuietly(answerAtt);
			}
		    }
		}
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(answerAtt);
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
	}

	logger.debug("roles=" + roles);

	return roles;
    }

    /*
     * (non-Javadoc) Retourne vrai si l'utilisateur donné à le role donné pour
     * l'application donnée
     * 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String application, String role) throws GinaException, RemoteException {
	GinaApiLdapUtils.checkParam(user);
	GinaApiLdapUtils.checkParam(application);
	GinaApiLdapUtils.checkParam(role);

	init();
	NamingEnumeration<?> answer = null;
	NamingEnumeration<?> answerAtt = null;

	try {
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] { GinaApiLdapUtils.ATTRIBUTE_MEMBER });
	    answer = ctxtDir.search(GinaApiLdapUtils.getLdapFilterOu(ginaApplication), GinaApiLdapUtils.getLdapFilterCn(role), searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		Attributes attrs = sr.getAttributes();
		if (attrs != null && attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBER) != null) {
		    answerAtt = sr.getAttributes().get(GinaApiLdapUtils.ATTRIBUTE_MEMBER).getAll();
		    while (answerAtt.hasMoreElements()) {
			String att = (String) answerAtt.next();
			if (att.toUpperCase().contains(user.toUpperCase())) {
			    return true;
			}
		    }
		    GinaApiLdapUtils.closeQuietly(answerAtt);
		}
	    }
	} catch (NamingException e) {
	    logger.error(e);
	    throw new GinaException(e.getMessage());
	} finally {
	    GinaApiLdapUtils.closeQuietly(answerAtt);
	    GinaApiLdapUtils.closeQuietly(answer);
	    closeDirContext();
	}

	return false;
    }

    @Override
    public List<String> getBusinessRoles(String application) throws GinaException, RemoteException {
	GinaApiLdapUtils.checkParam(application);
	
	List<String> roles = this.getAppRoles(application);

	List<String> result = new ArrayList<String>();

	if (roles != null) {
	    for (String role : roles) {
		if (role.startsWith("RM-")) {
		    result.add(role);
		}
	    }
	}
	return result;
    }

    @Override
    @Deprecated
    public void sendMail(String from, String[] to, String[] cc, String subject, String text, String mimeType)
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getUser() throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getLanguage() throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getEnvironment() throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getIntegrationUserRoles(String paramString1, String paramString2)
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getIntegrationUserAttributes(String paramString1, String paramString2)
	    throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getInheritingRoles(String paramString1, String paramString2) {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getPMProprieteMetier(String paramString) {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public String getOwnIDUniqueForPPorPseudo() throws GinaException, RemoteException, NamingException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getOwnPMProprieteMetier(String paramString) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getPPProprieteMetier(String paramString) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getOwnPPProprieteMetier(String paramString) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getUsersByPhone(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getUsersBySIRHNumber(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getUsersByName(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasRole(String application, String role) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getRoles(String application) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public List<Map<String, String>> getAllUsers(String filter, String[] attrs) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    public Map<String, String> getUserAttrs(String[] attrs) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

}
