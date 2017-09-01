package gina.api;

import static org.junit.Assert.assertSame;
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

public class GinaApiLdapApplicationTest {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapApplicationTest.class);

    // LDAP au niveau de l'application - Domaine Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE = "OAC";

    // LDAP au niveau de l'application  - Application Gina
    private static final String LDAP_APPLICATION_TEST_APPLICATION = "CAMAC-GENEVE";

    // LDAP au niveau de l'application - Domaine + Application Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE_APPLICATION = LDAP_APPLICATION_TEST_DOMAINE + "." + LDAP_APPLICATION_TEST_APPLICATION;

    // LDAP au niveau du domaine - Rôle de test
    private static final String LDAP_APPLICATION_TEST_ROLE = "UTILISATEUR";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getAllUsersTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            String attrs[] = {"username"};
            GinaApiLdapBaseFactory.getInstanceApplication().getAllUsers("FILTER", attrs);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

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
	    boolean ret = GinaApiLdapBaseFactory.getInstanceApplication().hasUserRole(GinaApiLdapContantsTest.DRIVONOL_USERNAME, LDAP_APPLICATION_TEST_ROLE);
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

    @Test
    public void getUserTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceApplication().getUser();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserAttrsWithAttrsTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
        try {
            Map<String,String> result = GinaApiLdapBaseFactory.getInstanceApplication().getUserAttrs(GinaApiLdapContantsTest.TEST_ATTRS);
	    Assert.assertNotNull(result);
	    assertSame(0, result.size());
	    LOG.info("result=" + result);
	} catch (GinaException e) {
	    LOG.error(e);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getLanguage();
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
            GinaApiLdapBaseFactory.getInstanceApplication().getEnvironment();
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
	    GinaApiLdapBaseFactory.getInstanceApplication().hasRole(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, LDAP_APPLICATION_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getRolesWithApplicationTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceApplication().getRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserRolesWithUserAndApplicationTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    List<String> roles = GinaApiLdapBaseFactory.getInstanceApplication().getUserRoles(GinaApiLdapContantsTest.DRIVONOL_USERNAME, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    Assert.assertTrue(roles.contains(LDAP_APPLICATION_TEST_ROLE));
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
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceApplication().getUsers(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, GinaApiLdapContantsTest.TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertFalse(users.isEmpty());
	    LOG.info("users=" + users);

	    boolean containsUserTest = false;
	    for( Map<String, String> user : users) {
		String uid = user.get("uid");
		if(StringUtils.isNotBlank(uid) && uid.contains(GinaApiLdapContantsTest.DRIVONOL_USERNAME)) {
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
    public void getUsersTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	try {
	    List<Map<String, String>> users = GinaApiLdapBaseFactory.getInstanceApplication().getUsers(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, LDAP_APPLICATION_TEST_ROLE, GinaApiLdapContantsTest.TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertTrue(users.size() > 0);
	    LOG.debug("users.size()=" + users.size());
	    LOG.debug("users=" + users);
	    
	    boolean found = false;
	    for(Map<String, String> user : users) {
		    String uid = user.get("uid");
		    if(GinaApiLdapContantsTest.DRIVONOL_USERNAME.equals(uid)) {
			found = true;
			break;
		    }
	    }
	    assertTrue("Le user " + GinaApiLdapContantsTest.DRIVONOL_USERNAME + " devrait faire partie de la liste", found);
	} catch (GinaException e) {
	    LOG.error(e);
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
    public void getIntegrationUserRolesTest() {
	LOG.info(GinaApiLdapContantsTest.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            GinaApiLdapBaseFactory.getInstanceApplication().getIntegrationUserRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, "ABC");
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
            GinaApiLdapBaseFactory.getInstanceApplication().getIntegrationUserAttributes(LDAP_APPLICATION_TEST_DOMAINE, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getBusinessRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getUsersByPhone("ABC", true, attrs);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getUsersBySIRHNumber("ABC", true, attrs);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getUsersByName("ABC", true, attrs);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getInheritingRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, LDAP_APPLICATION_TEST_ROLE);
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
            GinaApiLdapBaseFactory.getInstanceApplication().getPMProprieteMetier(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
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
		GinaApiLdapBaseFactory.getInstanceApplication().getOwnIDUniqueForPPorPseudo();
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
            GinaApiLdapBaseFactory.getInstanceApplication().getOwnPMProprieteMetier("");
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
            GinaApiLdapBaseFactory.getInstanceApplication().getPPProprieteMetier("");
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
            GinaApiLdapBaseFactory.getInstanceApplication().getOwnPPProprieteMetier("");
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
            GinaApiLdapBaseFactory.getInstanceApplication().sendMail("", foo, foo, "", "", "");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
}
