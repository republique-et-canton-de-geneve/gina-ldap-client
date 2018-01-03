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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapDirContext;
import gina.api.util.GinaApiLdapUtils;

public abstract class GinaApiLdapBaseAbleCommon implements GinaApiLdapBaseAble {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleCommon.class);

    // Message d'erreur pour les méthodes non implémentées
    protected static final String NOT_IMPLEMENTED = "Not implemented";
    
    // 
    protected DirContext ctxtDir = null;

    protected SearchControls getSearchControls() {
	SearchControls searchControls = new SearchControls();
	searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

	return searchControls;
    }

    protected SearchControls getSearchControls(final String[] attributes) {
	final SearchControls searchControls = getSearchControls();

	if(attributes != null && attributes.length > 0) {
	    searchControls.setReturningAttributes(attributes);
	}

	return searchControls;
    }

    protected void init() throws GinaException {
	if (ctxtDir == null) {
	    logger.info("init()");

	    GinaApiLdapDirContext galdc = new GinaApiLdapDirContext();
	    galdc.init();

	    ctxtDir = galdc.getCtxtDir();
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
	    Attributes matchAttrs = new BasicAttributes(true);
	    matchAttrs.put(new BasicAttribute("cn", user));
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.debug("sr=" + sr);
		Attributes attrs = sr.getAttributes();
		if (attrs != null) {
		    Attribute cn = attrs.get("cn");
		    if (cn != null) {
			String cnString = (String) cn.get();
			Attribute departmentNumber = attrs.get("departmentNumber");
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
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();

	init();
	try {
	    SearchControls searchControls = getSearchControls(paramArrayOfString);
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

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
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
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
	init();
	List<String> roles = new ArrayList<String>();
	try {
	    String ginaDomain = GinaApiLdapUtils.extractDomain(application);
	    String ginaApplication = GinaApiLdapUtils.extractApplication(application);

	    SearchControls searchControls = getSearchControls(new String[] {GinaApiLdapUtils.ATTRIBUTE_MEMBEROF});
	    String searchFilter = GinaApiLdapUtils.getLdapFilterUser(user);
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter, searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    logger.debug("sr=" + sr);
		    if (sr != null) {
			final Attributes attrs = sr.getAttributes();
			logger.debug("attrs=" + attrs);
			if (attrs != null && attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF) != null) {
			    NamingEnumeration<?> answerAtt = attrs.get(GinaApiLdapUtils.ATTRIBUTE_MEMBEROF).getAll();
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
	    logger.error(e); 
	    throw new GinaException(e.getMessage());
	}

	logger.debug("roles=" + roles);

	return roles;
    }

    @Override
    @Deprecated
    public void sendMail(String from, String to[], String cc[], String subject,
            String text, String mimeType) throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    @Deprecated
    public String getUser() throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    @Deprecated
    public String getLanguage() throws GinaException, RemoteException {
	throw new GinaException(NOT_IMPLEMENTED);
    }

    @Override
    @Deprecated
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
    public List<String> getBusinessRoles(String paramString) throws GinaException, RemoteException {
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

}
