package gina.api;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import gina.api.GinaApiLdapBaseFactory;
import gina.api.GinaException;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);
    private static final String TEST_SEPARATOR = "************************ ";

    public static void main(String[] args) {
	try {
	    GinaApiLdapBaseAble ldapApplication = GinaApiLdapBaseFactory.getInstance();

	    LOG.debug("LDAP APPLICATION");
	    
	    String userName = "DRIVONOL";
	    LOG.debug("userName=" + userName);

	    String roleName = "CAMAC-GENEVE";
	    LOG.debug("roleName=" + roleName);

	    List<String> roles;
	    
	    
	    LOG.debug(TEST_SEPARATOR + "getUserRoles(userName)");
	    roles = ldapApplication.getUserRoles(userName);
            LOG.debug("size=" + roles.size());
	    LOG.debug(roles);


	    LOG.debug(TEST_SEPARATOR + "getRoles");
            roles = ldapApplication.getRoles();
            LOG.debug("size=" + roles.size());
            for (String role : roles) {
        	LOG.debug("role=" + role);
	    }

            
	    LOG.debug(TEST_SEPARATOR + "getUserRoles(userName)");
            roles = ldapApplication.getUserRoles(userName);
            for (String role : roles) {
        	LOG.debug("role=" + role);
	    }
	    

	    LOG.debug(TEST_SEPARATOR + "getAppRoles(roleName)");
	    roles = ldapApplication.getAppRoles(roleName);
	    LOG.debug("size=" + roles.size());
	    for (String role : roles) {
		LOG.debug("role=" + role);
	    }
	} catch (GinaException e) {
	    e.printStackTrace();
	} catch (RemoteException e) {
	    e.printStackTrace();
	}
    }

}
