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
    
    // LDAP au niveau du domaine - Domaine Gina
    private static final String LDAP_DOMAIN_TEST_DOMAINE = "CSBUGTRACK";

    // LDAP au niveau du domaine - Application Gina
    private static final String LDAP_DOMAIN_TEST_APPLICATION = "ACCESS-CONTROL";

    // LDAP au niveau du domaine - Domaine + Application Gina
    private static final String LDAP_DOMAIN_TEST_DOMAINE_APPLICATION = LDAP_DOMAIN_TEST_DOMAINE + "." + LDAP_DOMAIN_TEST_APPLICATION;

    // LDAP au niveau du domaine - Rôle de test
    private static final String LDAP_DOMAIN_TEST_ROLE = "ACCESS-CONTROL-USERS";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void isValidUserTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    boolean result = GinaApiLdapBaseFactory.getInstanceDomain().isValidUser(GinaApiLdapContantsTest.DTDCOURS01_USERNAME);
	    if (!result) {
		Assert.assertTrue("L'utilisateur " + GinaApiLdapContantsTest.DTDCOURS01_USERNAME + " est censé être valide !", result); 
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    Map<String, String> result = GinaApiLdapBaseFactory.getInstanceDomain().getUserAttrs(GinaApiLdapContantsTest.DTDCOURS01_USERNAME, GinaApiLdapContantsTest.TEST_ATTRS);
	    Assert.assertNotNull(result);
	    Assert.assertEquals(GinaApiLdapContantsTest.DTDCOURS01_USERNAME, result.get("sn"));
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles(GinaApiLdapContantsTest.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceDomain().getUsers(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, GinaApiLdapContantsTest.TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertFalse(users.isEmpty());
	    
	    boolean containsUserTest = false;
	    for( Map<String, String> user : users) {
		String sn = user.get("sn");
		if(StringUtils.isNotBlank(sn) && sn.contains(GinaApiLdapContantsTest.DTDCOURS01_USERNAME)) {
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole(GinaApiLdapContantsTest.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION,
		    LDAP_DOMAIN_TEST_ROLE);
	    Assert.assertTrue( GinaApiLdapContantsTest.DTDCOURS01_USERNAME + " devrait avoir le role " + LDAP_DOMAIN_TEST_ROLE + " pour l'application " + LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, ret );
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceDomain().getUsers(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE, GinaApiLdapContantsTest.TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertTrue(users.size() > 0);
	    LOG.debug("users.size()=" + users.size());
	    
	    boolean found = false;
	    for(Map<String, String> user : users) {
		    String uid = user.get("uid");
		    if(GinaApiLdapContantsTest.DTDCOURS01_USERNAME.equals(uid)) {
			found = true;
			break;
		    }
	    }
	    if(!found) {
		assertTrue("Le user " + GinaApiLdapContantsTest.DTDCOURS01_USERNAME + " devrait faire partie de la liste", false);
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceDomain().getAppRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.debug("roles.size()=" + roles.size());
	    LOG.debug("roles=" + roles);

	    if (!roles.contains(LDAP_DOMAIN_TEST_ROLE)) {
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
        
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
	    GinaApiLdapBaseFactory.getInstanceDomain().hasRole(LDAP_DOMAIN_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void hasRoleWithApplicationAndRoleTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
	    GinaApiLdapBaseFactory.getInstanceDomain().hasRole(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getAllUsersTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole(GinaApiLdapContantsTest.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserRolesWithUserTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles(GinaApiLdapContantsTest.DTDCOURS01_USERNAME);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getIntegrationUserRoles(LDAP_DOMAIN_TEST_DOMAINE, "ABC");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getIntegrationUserAttributesTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getIntegrationUserAttributes(LDAP_DOMAIN_TEST_DOMAINE, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getBusinessRolesTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getBusinessRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersByPhoneTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getInheritingRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getPMProprieteMetierTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceDomain().getPMProprieteMetier(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getOwnIDUniqueForPPorPseudoTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
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
