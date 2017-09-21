package gina.api.util;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gina.api.GinaApiLdapUtils;

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
    private String LDAP_SERVER_URL = null;
    private String LDAP_BASE_DN = null;
    private String LDAP_USER = null;
    private String LDAP_PASSWORD = null;

    //
    private DirContext ctxtDir = null;

    // Type d'accès au ldap : détecté par rapport au user fourni
    private String type;
    
    public void init() {
	LOG.info("Start");

	try {
	    ch.ge.cti.configuration.Configuration.addRelativeToStandardConfFolder(CONFIGURATION_FILE);
	    ch.ge.cti.configuration.Configuration.addClasspath(CONFIGURATION_FILE);

	    LDAP_BASE_DN = GinaApiLdapUtils
		    .createPropertie(ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_BASE_DN"));
	    LDAP_USER = GinaApiLdapUtils
		    .createPropertie(ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_USER"));
	    LDAP_PASSWORD = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_PASSWORD");
	    
	    init(LDAP_BASE_DN, LDAP_USER, LDAP_PASSWORD);
	    
	    LOG.debug("init ok");
	} catch (Exception e) {
	    LOG.error(e);
	}

	LOG.info("End");
    }

    public void init(final String base, final String user, final String password) {
	LOG.info("Start");

	try {
	    ch.ge.cti.configuration.Configuration.addRelativeToStandardConfFolder("ct-gina-ldap-client.properties");
	    ch.ge.cti.configuration.Configuration.addClasspath("ct-gina-ldap-client.properties");

	    LDAP_SERVER_URL = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_SERVER_URL");
	    LDAP_BASE_DN = base;
	    LDAP_USER = user;
	    LDAP_PASSWORD = password;

	    LOG.info("LDAP_SERVER_URL = " + LDAP_SERVER_URL);
	    LOG.info("LDAP_BASE_DN = " + LDAP_BASE_DN);
	    LOG.info("LDAP_USER = " + LDAP_USER);
	    LOG.info("LDAP_PASSWORD = " + LDAP_PASSWORD);

	    int count = StringUtils.countMatches(LDAP_USER, ",ou=");
	    LOG.debug("count=" + count);
	    if (count > 2) {
		type = APPLICATION;
	    } else {
		type = DOMAIN;
	    }

	    createCtxDir(base, user, password);

	    LOG.debug("init ok");
	} catch (NamingException e) {
	    LOG.error("InitialDirContext failed", e);
	} catch (Exception e) {
	    LOG.error(e);
	}

	LOG.info("End");
    }

    public void createCtxDir() throws NamingException {
	    createCtxDir(LDAP_BASE_DN, LDAP_USER, LDAP_PASSWORD);
    }

    public void createCtxDir(String base, String user, String password) throws NamingException {
	    Hashtable<String, String> env = new Hashtable<String, String>();
	    env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
	    env.put(Context.PROVIDER_URL, LDAP_SERVER_URL + "/" + base);
	    env.put(Context.SECURITY_AUTHENTICATION, LDAP_AUTHENTICATION_MODE);
	    env.put(Context.SECURITY_PROTOCOL, "ssl");
	    env.put(Context.SECURITY_PRINCIPAL, user);
	    env.put(Context.SECURITY_CREDENTIALS, password);
	    env.put(Context.REFERRAL, LDAP_REFERRAL_MODE);
	    env.put("java.naming.ldap.version", "3");

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