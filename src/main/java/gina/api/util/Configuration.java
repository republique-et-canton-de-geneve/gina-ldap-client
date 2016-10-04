package gina.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;


public class Configuration
{
    private static final String DISTRIBUTION_PROPERTIES_FILENAME = "Distribution.properties";
    private static final String PROPERTY_DISTRIBUTION_BASE = "distribution.dir";
    private static final String PROPERTY_JONAS_BASE = "jonas.base";
    private static final Logger LOG = Logger.getLogger(Configuration.class);

    //Groupe Gina de base
    private static String GINA_APPLICATION_PRINCIPAL = "GLOBAL-ACCESS";
    // LDAP
    private static String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static String LDAP_AUTHENTICATION_MODE = "simple";
    private static String LDAP_REFERRAL_MODE = "follow";
    private String LDAP_SERVER_URL = null;
    private String LDAP_BASE_DN = null;
    private String LDAP_USER = null;
    private String LDAP_PASSWORD = null;
    //
    private List<String> applications = new ArrayList<String>();
    private List<String> allGinaRoles = new ArrayList<String>();
    private List<String> allGinaUsers = new ArrayList<String>();
    private Map<String, List<String>> allGinaUsersByGroup = new HashMap<String, List<String>>();
    //
    private DirContext ctxtDir = null;
    //
    private boolean initialized = false;
    private boolean syncUsersSucceeded = false;

    private static final Properties PROPS = loadProps();
  
    public void init() {
	LOG.info("Start");

	try {
	    	
		/*LDAP_SERVER_URL = "ldap://vldap-dev.ceti.etat-ge.ch:636";  
		LDAP_BASE_DN = "ou=CSBUGTRACK,o=gina" ; 
		LDAP_USER = "cn=tcnvldap6470devaag,ou=Users,ou=CSBUGTRACK,o=gina"; 
		LDAP_PASSWORD = "Xhngmfxp9"; 
		System.out.println("SERVER_URL = " + PROPS.getProperty("ct-gina-ldap-client.LDAP_SERVER_URL")); */
		
		LDAP_SERVER_URL = "ldap://vldap-dev.ceti.etat-ge.ch:636";  
		LDAP_BASE_DN = "ou=OAC,o=gina" ; 
		LDAP_USER = "cn=TCNVLDAP9523DEVAAG,ou=Users,ou=CAMAC-GENEVE,ou=OAC,o=gina"; 
		LDAP_PASSWORD = "Uddyzfsp4"; 
		System.out.println("SERVER_URL = " + PROPS.getProperty("ct-gina-ldap-client.LDAP_SERVER_URL")); 
	
		/*LDAP_SERVER_URL = PROPS.getProperty("ct-gina-ldap-client.LDAP_SERVER_URL");  
		LDAP_BASE_DN = PROPS.getProperty("ct-gina-ldap-client.LDAP_BASE_DN"); 
		LDAP_USER = PROPS.getProperty("ct-gina-ldap-client.LDAP_USER"); 
		LDAP_PASSWORD = PROPS.getProperty("ct-gina-ldap-client.LDAP_PASSWORD"); */
		
		System.out.println("SERVER_URL = " + LDAP_SERVER_URL); 
		System.out.println("LDAP_USER = " + LDAP_USER); 
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
		initialized = true;
		LOG.debug("InitialDirContext ok");
		
	}
	catch (NamingException e) {
	    	LOG.error("InitialDirContext failed", e);
		initialized = false;
	}
	catch (Exception e) {
	    	LOG.error(e);
		initialized = false;
	}

	LOG.info("End");
    }

  public DirContext getCtxtDir() {
        return ctxtDir;
    }

 public void setCtxtDir(DirContext ctxtDir) {
        this.ctxtDir = ctxtDir;
    }

 public static Properties getProps()
  {
    Properties props = new Properties();
    //props.putAll(PROPS);
    return props;
  }

  public static String getString(String name, String def) {
    //return PROPS.getProperty(name, def);
      return null;
  }

  public static int getInt(String name, int def) {
    String s = getString(name, null);
    if (s == null) {
      return def;
    }
    return Integer.parseInt(s);
  }

  public static long getLong(String name, long def)
  {
    String s = getString(name, null);
    if (s == null) {
      return def;
    }
    return Long.parseLong(s);
  }

  private static final Properties loadProps()
  {
    try {
      Properties props = new Properties();
      File file = getPropsFile(System.getProperty("distribution.dir"));
      //System.out.println("dir" + System.getProperties().get("user.dir") );
      if (file == null)
      {
        file = getPropsFile(System.getProperty("jonas.base"));
        if (file == null)
        {
          file = getPropsFile(".");
          
          if (file == null) {
              file = getPropsFile("../");
              	if (file == null) {
              
              		file = getPropsFile("./src/main/resources/");
              
              		if (file == null) {
              		    throw new ExceptionInInitializerError("Distribution.properties not found, invalid or incorrectly defined");
              		}
              	}
              		
          }
          
          
        }
      }
      LOG.info("Loading Distribution.properties from location [" + file.getAbsolutePath() + "]");
      InputStream in = new FileInputStream(file);
      try {
        props.load(in);
      } catch (IOException ioe) {
        LOG.error("Error while loading properties from file [" + file.getAbsolutePath() + "]", ioe);
        throw new ExceptionInInitializerError(ioe.getMessage());
      } finally {
        in.close();
      }
      return props;
    } catch (IOException e) {
      LOG.error("Error while loading properties");
      throw new ExceptionInInitializerError(e.getMessage());
    }
  }

  private static final File getPropsFile(String location)
  {
    if ((location != null) && (location.trim().length() > 0)) {
      File file = new File(location, "Distribution.properties");
      if ((file != null) && (file.exists()) && (file.isFile())) {
        return file;
      }
    }
    return null;
  }
}