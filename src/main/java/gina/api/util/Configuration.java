package gina.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

import ch.ge.cti.ct.act.configuration.DistributionFactory;


public class Configuration
{

    private static final Logger LOG = Logger.getLogger(Configuration.class);
    
    static final String Domain = "domain"; 
    static final String Application = "application";
    
    // LDAP
    private static String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static String LDAP_AUTHENTICATION_MODE = "simple";
    private static String LDAP_REFERRAL_MODE = "follow";
    private String LDAP_SERVER_URL = null;
    private String LDAP_BASE_DN = null;
    private String LDAP_USER = null;
    private String LDAP_PASSWORD = null;
    //
    private DirContext ctxtDir = null;

    private static final Properties PROPS = loadProps();
  
    public void init(String type) {
	LOG.info("Start");

	try {
	    	DistributionFactory.setDisableJNDI(true);
	    	String url = DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_SERVER_URL_DOMAIN");
		
	        if(type.equalsIgnoreCase(Domain)) {
			LDAP_SERVER_URL = DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_SERVER_URL_DOMAIN"); 
			LDAP_BASE_DN =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_BASE_DN_DOMAIN");  
			LDAP_USER =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_USER_DOMAIN"); 
			LDAP_PASSWORD =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_PASSWORD_DOMAIN"); 
			LOG.info("LDAP_SERVER_URL = " + LDAP_SERVER_URL); 
			//ct-gina-ldap-client.LDAP_SERVER_URL_DOMAIN=ldap://vldap-dev.ceti.etat-ge.ch:636
			//    ct-gina-ldap-client.LDAP_BASE_DN_DOMAIN=ou=CSBUGTRACK,o=gina
			//    ct-gina-ldap-client.LDAP_USER_DOMAIN=cn=tcnvldap6470devaag,ou=Users,ou=CSBUGTRACK,o=gina
			//    ct-gina-ldap-client.LDAP_PASSWORD_DOMAIN=Xhngmfxp9
			
			
		}
	        else if(type.equalsIgnoreCase(Application)) {
			LDAP_SERVER_URL =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_SERVER_URL");  
			LDAP_BASE_DN =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_BASE_DN");  
			LDAP_USER =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_USER"); 
			LDAP_PASSWORD =  DistributionFactory.getConfiguration().getString("ct-gina-ldap-client.LDAP_PASSWORD"); 
			LOG.info("LDAP_SERVER_URL = " + LDAP_SERVER_URL); 
			
		}
 
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
		
	}
	catch (NamingException e) {
	    	LOG.error("InitialDirContext failed", e);
	}
	catch (Exception e) {
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


  private static final Properties loadProps()
  {
      try {
	  Properties props = new Properties();
	  File file = getPropsFile(System.getProperty("distribution.dir"));;
	  if (file == null)
	  {
	      file = getPropsFile(System.getProperty("jonas.base"));
	      if (file == null)
	      {
		  file = getPropsFile(".");

		  if (file == null) {

		      file = getPropsFile("./src/main/resources/");

		      if (file == null) {
			  throw new ExceptionInInitializerError("Distribution.properties not found, invalid or incorrectly defined");
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