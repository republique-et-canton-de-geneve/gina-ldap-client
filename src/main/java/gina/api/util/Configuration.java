package gina.api.util;

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

public class Configuration {

    // Logger
    private static final Logger LOG = Logger.getLogger(Configuration.class);

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


    public void init(String type) {
	LOG.info("Start");

	try {
	    ch.ge.cti.configuration.Configuration.addRelativeToStandardConfFolder(CONFIGURATION_FILE);
	    ch.ge.cti.configuration.Configuration.addClasspath(CONFIGURATION_FILE);
	    
	    if (type.equalsIgnoreCase(DOMAIN)) {
		LDAP_SERVER_URL = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_SERVER_URL_DOMAIN");
		LDAP_BASE_DN = createPropertie( ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_BASE_DN_DOMAIN"));
		LDAP_USER = createPropertie( ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_USER_DOMAIN"));
		LDAP_PASSWORD = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_PASSWORD_DOMAIN");
	    } else if (type.equalsIgnoreCase(APPLICATION)) {
		LDAP_SERVER_URL = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_SERVER_URL");
		LDAP_BASE_DN = createPropertie(ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_BASE_DN"));
		LDAP_USER = createPropertie(ch.ge.cti.configuration.Configuration.getList("ct-gina-ldap-client.LDAP_USER"));
		LDAP_PASSWORD = ch.ge.cti.configuration.Configuration.getParameter("ct-gina-ldap-client.LDAP_PASSWORD");
	    }
	    LOG.info("LDAP_SERVER_URL = " + LDAP_SERVER_URL);

	    Hashtable<String, String> env = new Hashtable<String, String>();
	    env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
	    env.put(Context.PROVIDER_URL, LDAP_SERVER_URL + "/" + LDAP_BASE_DN);
	    env.put(Context.SECURITY_AUTHENTICATION, LDAP_AUTHENTICATION_MODE);
	    env.put(Context.SECURITY_PROTOCOL, "ssl");
	    env.put(Context.SECURITY_PRINCIPAL, LDAP_USER);
	    env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
	    env.put(Context.REFERRAL, LDAP_REFERRAL_MODE);
	    env.put("java.naming.ldap.version", "3");

	    ctxtDir = new InitialDirContext(env);
	    LOG.debug("InitialDirContext ok");
	} catch (NamingException e) {
	    LOG.error("InitialDirContext failed", e);
	} catch (Exception e) {
	    LOG.error(e);
	}

	LOG.info("End");
    }

    public DirContext getCtxtDir() {
	return ctxtDir;
    }

    public void setCtxtDir(DirContext ctxtDir) {
	this.ctxtDir = ctxtDir;
    }

    public String createPropertie(List<String> list) {
	StringBuilder builder = new StringBuilder();
	int size = 0;
	for (String s : list) {
	    size++;
	    builder.append(s);
	    if (size < list.size()) {
		builder.append(',');
	    }
	}
	return builder.toString();
    }

}