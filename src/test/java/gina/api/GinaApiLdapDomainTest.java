package gina.api;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

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

    // Domaine Gina
    private static final String TEST_DOMAIN = "CSBUGTRACK";

    // Application Gina
    private static final String TEST_APPLICATION = "ACCESS-CONTROL";

    // Domaine + Application Gina
    private static final String TEST_DOMAIN_APPLICATION = TEST_DOMAIN + "." + TEST_APPLICATION;

    // R�le de test
    private static final String TEST_ROLE = "ACCESS-CONTROL-USERS";

    // Propri�t�s de l'utilisateur
    private String[] TEST_ATTRS = { "initials", "givenName", "sn", "username", "uid" };

    // String indiquant le d�but d'un test
    private static final String START_METHOD = "START";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void isValidUserTest() {
	LOG.info(START_METHOD);
	try {
	    boolean result = GinaApiLdapBaseFactory.getInstanceDomain().isValidUser(DTDCOURS01_USERNAME);
	    if (!result) {
		Assert.assertTrue("L'utilisateur " + DTDCOURS01_USERNAME + " est cens� �tre valide !", result); 
	    }
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
	assertTrue(true);
    }

    @Test
    public void getUserAttrsWithUserAndAttrsTest() {
	LOG.info(START_METHOD);
	try {
	    Map<String, String> result = GinaApiLdapBaseFactory.getInstanceDomain().getUserAttrs(DTDCOURS01_USERNAME, TEST_ATTRS);
	    Assert.assertNotNull(result);
	    Assert.assertEquals(DTDCOURS01_USERNAME, result.get("sn"));
	    Assert.assertNotNull(result.get("initials"));
	    Assert.assertNotNull(result.get("givenName"));
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
	assertTrue(true);
    }
    
    @Test
    public void getUserRolesWithUserAndApplicationTest() {
	LOG.info(START_METHOD);
	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles(DTDCOURS01_USERNAME, TEST_DOMAIN_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
	assertTrue(true);
    }
   

    @Test
    public void getUsersWithApplicationAndAttrsTest() {
	LOG.info(START_METHOD);
	try {
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceDomain().getUsers(TEST_DOMAIN_APPLICATION, TEST_ATTRS);
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
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
	assertTrue(true);
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() {
	LOG.info(START_METHOD);
	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole(DTDCOURS01_USERNAME, TEST_DOMAIN_APPLICATION,
		    TEST_ROLE);
	    Assert.assertTrue( DTDCOURS01_USERNAME + " devrait avoir le role " + TEST_ROLE + " pour l'application " + TEST_DOMAIN_APPLICATION, ret );
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUsersTest() {
	LOG.info(START_METHOD);
	try {
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceDomain().getUsers(TEST_DOMAIN_APPLICATION, TEST_ROLE, TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertTrue(users.size() > 0);
	    LOG.debug("users.size()=" + users.size());
	    
	    boolean found = false;
	    for(Map<String, String> user : users) {
		    String uid = user.get("uid");
		    if(DTDCOURS01_USERNAME.equals(uid)) {
			found = true;
			break;
		    }
	    }
	    if(!found) {
		assertTrue("Le user " + DTDCOURS01_USERNAME + " devrait faire partie de la liste", false);
	    }
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
	assertTrue(true);
    }

    @Test
    public void getAppRolesTest() {
	LOG.info(START_METHOD);
	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceDomain().getAppRoles(TEST_DOMAIN_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.debug("roles.size()=" + roles.size());
	    LOG.debug("roles=" + roles);

	    if (!roles.contains(TEST_ROLE)) {
		assertTrue(false);
	    }
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
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
	    LOG.error(e);
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
	    LOG.error(e);
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
	    LOG.error(e);
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
	    LOG.error(e);
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
	    LOG.error(e);
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
	    LOG.error(e);
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
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getUser();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getLanguageTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getLanguage();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getEnvironmentTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getEnvironment();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getIntegrationUserRolesTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getIntegrationUserRoles(TEST_DOMAIN, "ABC");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getIntegrationUserAttributesTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getIntegrationUserAttributes(TEST_DOMAIN, TEST_DOMAIN_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getBusinessRolesTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getBusinessRoles(TEST_DOMAIN_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersByPhoneTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        String[] attrs = { "initials", "givenName", "sn" };

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getUsersByPhone("ABC", true, attrs);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersBySIRHNumberTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        String[] attrs = { "initials", "givenName", "sn" };

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getUsersBySIRHNumber("ABC", true, attrs);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersByNameTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        String[] attrs = { "initials", "givenName", "sn" };

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getUsersByName("ABC", true, attrs);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getInheritingRolesTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getInheritingRoles(TEST_DOMAIN_APPLICATION, TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getPMProprieteMetierTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getPMProprieteMetier(TEST_DOMAIN_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getOwnIDUniqueForPPorPseudoTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

            try {
		GinaApiLdapBaseFactory.getInstanceDomain().getOwnIDUniqueForPPorPseudo();
	    } catch (RemoteException e) {
		LOG.error(e);
		assertTrue(false);
	    } catch (NamingException e) {
		LOG.error(e);
		assertTrue(false);
	    }
    }
    
    @Test
    public void getOwnPMProprieteMetierTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getOwnPMProprieteMetier("");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getPPProprieteMetierTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getPPProprieteMetier("");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getOwnPPProprieteMetierTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getOwnPPProprieteMetier("");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void sendMailTest() {
	LOG.info(START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        String[] foo = { "" };
        
        try {
            GinaApiLdapBaseFactory.getInstanceDomain().sendMail("", foo, foo, "", "", "");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

}
