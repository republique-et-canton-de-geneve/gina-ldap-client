package gina.api;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapDirContext;

public abstract class GinaApiLdapBaseAbleCommon implements GinaApiLdapBaseAble, GinaApiLdapConfig {

    // Logger
    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleCommon.class);

    // 
    protected static final String NOT_IMPLEMENTED = "Not implemented";
    
    // 
    protected DirContext ctxtDir = null;

    protected SearchControls getSearchControls() {
	int maxTimeLimit = ch.ge.cti.configuration.Configuration.getParameterAsInt("ct-gina-ldap-client.LDAP_TIMEOUT_SEARCH", 3000);

	SearchControls searchControls = new SearchControls();
	searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	searchControls.setTimeLimit(maxTimeLimit);

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
