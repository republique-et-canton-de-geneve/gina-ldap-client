package gina.api;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class GinaApiLdapApplicationTest {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapApplicationTest.class);

    @Test
    public void getUserRolesTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceApplication().getUserRoles(GinaApiLdapContantsTest.DRIVONOL_USERNAME);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(roles.contains("UTILISATEUR"));
	    Assert.assertTrue(roles.contains("CONTEXTE-APA"));
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getIsValidUserTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    // Utilisateur valide
	    boolean result = GinaApiLdapBaseFactory.getInstanceApplication().isValidUser(GinaApiLdapContantsTest.DRIVONOL_USERNAME);
	    LOG.info("result=" + result);
	    assertTrue(result);
	    
	    // Utilisateur non valide
	    result = GinaApiLdapBaseFactory.getInstanceApplication().isValidUser(GinaApiLdapContantsTest.PINAUDJ_USERNAME);
	    LOG.info("result=" + result);
	    assertTrue(!result);
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserAttrsTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    Map<String, String> user = GinaApiLdapBaseFactory.getInstanceApplication().getUserAttrs(GinaApiLdapContantsTest.DRIVONOL_USERNAME,
		    GinaApiLdapContantsTest.TEST_ATTRS);
	    for (Map.Entry<String, String> e : user.entrySet()) {
		LOG.info(e.getKey() + "=" + e.getValue());
		if ("uid".equalsIgnoreCase(e.getKey())) {
		    assertTrue(e.getValue().equalsIgnoreCase(GinaApiLdapContantsTest.DRIVONOL_USERNAME));
		    break;
		}
	    }
	    LOG.info("user=" + user);
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void hasRoleTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceApplication().hasRole("ADMIN");
	    assertSame(false, ret);
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void hasRoleUserTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceApplication().hasUserRole(GinaApiLdapContantsTest.DRIVONOL_USERNAME, "ADMIN");
	    assertSame(true, ret);
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getRolesTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    List<String> ret = GinaApiLdapBaseFactory.getInstanceApplication().getRoles();
	    assertSame(0, ret.size());
	    // La recherche de roles se fait avec l'utilisateur specifié dans le fichier properties (TCNXXX) donc ça doit retourner 0 
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getAppRolesTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);

	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceApplication().getAppRoles("CAMAC-GENEVE");
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(roles.contains("ADMIN"));
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

}
