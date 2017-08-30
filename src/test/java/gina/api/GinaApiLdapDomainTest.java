package gina.api;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

public class GinaApiLdapDomainTest {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapDomainTest.class);
    
    // Utilisateur DTDCOURS01
    private static final String DTDCOURS01_USERNAME = "DTDCOURS01";

    // 
    private static final String TEST_APPLICATION = "ACCESS-CONTROL";

    // 
    private static final String TEST_DOMAIN = "CSBUGTRACK";

    // 
    private static final String TEST_ROLE = "ACCESS-CONTROL-USERS";

    // String indiquant le début d'un test
    private static final String START_METHOD = "START";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void isValidUserTest() {
	LOG.info(START_METHOD);
	try {
	    boolean result = GinaApiLdapBaseFactory.getInstanceDomain().isValidUser(DTDCOURS01_USERNAME);
	    if (!result) {
		Assert.assertTrue("L'utilisateur " + DTDCOURS01_USERNAME + " est censé être valide !", result); 
	    }
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
    }

    @Test
    public void getUserAttrsWithUserAndAttrsTest() {
	LOG.info(START_METHOD);
	try {
	    String[] attrs = { "initials", "givenName", "sn" };
	    Map<String, String> result = GinaApiLdapBaseFactory.getInstanceDomain().getUserAttrs(DTDCOURS01_USERNAME, attrs);
	    Assert.assertEquals(DTDCOURS01_USERNAME, result.get("sn"));
	    Assert.assertNotNull(result.get("initials"));
	    Assert.assertNotNull(result.get("givenName"));
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
    }
    
    @Test
    public void getUserRolesWithUserAndApplicationTest() {
	LOG.info(START_METHOD);
	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles(DTDCOURS01_USERNAME, TEST_DOMAIN + "." + TEST_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
    }
   

    @Test
    public void getUsersWithApplicationAndAttrsTest() {
	LOG.info(START_METHOD);
	try {
	    String[] param = { "initials", "givenName", "sn" };
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceDomain().getUsers(TEST_APPLICATION, param);
	    Assert.assertNotNull(users);
	    Assert.assertFalse(users.isEmpty());
	    
	    boolean containsUserTest = false;
	    for( Map<String, String> user : users) {
		String sn = user.get("sn");
		if(StringUtils.isNotBlank(sn) && sn.contains(DTDCOURS01_USERNAME)) {
		    containsUserTest = true;
		}
	    }
	    Assert.assertTrue(containsUserTest);
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
    }

    @Test
    public void hasRoleUserTest() {
	LOG.info(START_METHOD);
	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole("dtdcours01", "ACCESS-CONTROL",
		    "UTILISATEUR");
	    Assert.assertTrue( "dtdcours01 a bien le role UTILISATEUR pour l' appli ACCES-CONTROL" , ret );
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getUsersTest() {
	LOG.info(START_METHOD);
	try {

	    List<Map<String, String>> user = new ArrayList<Map<String, String>>();
	    String[] param = { "initials", "givenName", "sn" };
	    user = GinaApiLdapBaseFactory.getInstanceDomain().getUsers("GEN-ROLES", "GEN-ROLES-SMIL", param);
	    // LOG.info("nb user : " + user.size() +
	    // user.get(0).get("initials"));
	    Map<String, String> temp = user.get(0);
	    String s = temp.get(new String("initials"));
	    if (!s.equals("KA")) {
		assertTrue(false);
	    }

	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);

    }

    @Test
    public void getAppRolesTest() {
	LOG.info(START_METHOD);
	try {
	    LOG.info("getAppRolesTest");
	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getAppRoles("GEN-ROLES");
	    LOG.info("nb roles GEN-ROLES : " + roles.size());
	    LOG.info("roles=" + roles);

	    if (!roles.contains(new String("GEN-ROLES-SMIL"))) {
		assertTrue(false);
	    }

	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);

    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------
    
    @Test
    public void hasRoleWithRoleTest() {
	LOG.info(START_METHOD);
        
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
	    GinaApiLdapBaseFactory.getInstanceDomain().hasRole("ROLE");
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void hasRoleWithApplicationAndRoleTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
	    GinaApiLdapBaseFactory.getInstanceDomain().hasRole("APPLICATION", "ROLE");
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getAllUsersTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            String attrs[] = {"username"};
            GinaApiLdapBaseFactory.getInstanceDomain().getAllUsers("FILTER", attrs);
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getUserAttrsWithAttrsTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            String attrs[] = {"username"};
            GinaApiLdapBaseFactory.getInstanceDomain().getUserAttrs(attrs);
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getRolesTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getRoles("APPLICATION");
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void hasUserRoleTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole("user", "role");
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getUserRolesWithUserTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles("user");
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

}
