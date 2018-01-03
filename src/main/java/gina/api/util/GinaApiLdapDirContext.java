package gina.api.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class GinaApiLdapDirContext {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapDirContext.class);

    // Type d'accès au LDAP
    public static final String DOMAIN = "domain";
    public static final String APPLICATION = "application";

    // Nom du fichier de configuration du LDAP
    public static final String CONFIGURATION_FILE = "ct-gina-ldap-client.properties";

    // Configuration du LDAP
    private static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String LDAP_AUTHENTICATION_MODE = "simple";
    private static final String LDAP_REFERRAL_MODE = "follow";
    private String ldapServerUrl = null;
    private String ldapBaseDn = null;
    private String ldapUser = null;
    private String ldapPassword = null;
    private int ldapTimeout = 3000;

    //
    private DirContext ctxtDir = null;

    // Type d'accès au ldap : détecté par rapport au user fourni
    private String type;
    
    public void init() {
	LOG.info("Start");

	try {
	    ch.ge.cti.configuration.Configuration.addRelativeToStandardConfFolder(CONFIGURATION_FILE);
	    ch.ge.cti.configuration.Configuration.addClasspath(CONFIGURATION_FILE);

	    ldapServerUrl = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_SERVER_URL");
	    ldapBaseDn = GinaApiLdapUtils
		    .createPropertie(ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_BASE_DN"));
	    ldapUser = GinaApiLdapUtils
		    .createPropertie(ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_USER"));
	    ldapPassword = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_PASSWORD");
	    ldapTimeout = ch.ge.cti.configuration.Configuration.getParameterAsInt("ct-gina-ldap-client.LDAP_TIMEOUT_SEARCH", 3000);

	    init(ldapServerUrl, ldapBaseDn, ldapUser, ldapPassword, ldapTimeout);
	    
	    LOG.debug("init ok");
	} catch (Exception e) {
	    LOG.error(e);
	}

	LOG.info("End");
    }

    public void init(final String server, final String base, final String user, final String password, final int timeout) {
	LOG.info("Start");

	try {
	    ldapServerUrl = server;
	    ldapBaseDn = base;
	    ldapUser = user;
	    ldapPassword = password;
	    ldapTimeout = timeout;

	    LOG.info("LDAP_SERVER_URL = " + ldapServerUrl);
	    LOG.info("LDAP_BASE_DN = " + ldapBaseDn);
	    LOG.info("LDAP_USER = " + ldapUser);
	    LOG.info("LDAP_PASSWORD = " + ldapPassword);

	    int count = StringUtils.countMatches(ldapUser, ",ou=");
	    LOG.debug("count=" + count);
	    if (count > 2) {
		type = APPLICATION;
	    } else {
		type = DOMAIN;
	    }

	    createCtxDir(server, base, user, password, timeout);

	    LOG.debug("init ok");
	} catch (NamingException e) {
	    LOG.error("InitialDirContext failed", e);
	} catch (Exception e) {
	    LOG.error(e);
	}

	LOG.info("End");
    }

    public void createCtxDir() throws NamingException {
	    createCtxDir(ldapServerUrl, ldapBaseDn, ldapUser, ldapPassword, ldapTimeout);
    }

    public void createCtxDir(String server, String base, String user, String password, int timeout) throws NamingException {
	    Hashtable<String, String> env = new Hashtable<String, String>();
	    env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
	    env.put(Context.PROVIDER_URL, ldapServerUrl + "/" + base);
	    env.put(Context.SECURITY_AUTHENTICATION, LDAP_AUTHENTICATION_MODE);
	    env.put(Context.SECURITY_PROTOCOL, "ssl");
	    env.put(Context.SECURITY_PRINCIPAL, user);
	    env.put(Context.SECURITY_CREDENTIALS, password);
	    env.put(Context.REFERRAL, LDAP_REFERRAL_MODE);
	    env.put("java.naming.ldap.version", "3");
	    env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(timeout));
	    env.put("com.sun.jndi.ldap.connect.pool", "true");

	    ctxtDir = new InitialDirContext(env);
}

    public DirContext getCtxtDir() {
	return ctxtDir;
    }

    public void setCtxtDir(DirContext ctxtDir) {
	this.ctxtDir = ctxtDir;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}