package gina.api;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;

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

    private static GinaApiLdapBaseAble api;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initApi() {
	String base = GinaApiLdapUtils.createPropertie(Arrays.asList("ou=OAC,o=gina"));
	
	String server = "ldaps://vldap-dev.ceti.etat-ge.ch:636";
	String user = GinaApiLdapUtils
		.createPropertie(Arrays.asList("cn=TCNVLDAP9523DEVAAG,ou=Users,ou=CAMAC-GENEVE,ou=OAC,o=gina"));
	String password = "Uddyzfsp4";
	
//	 String server = "ldap://127.0.0.1:30636";
//	 String user = GinaApiLdapUtils.createPropertie(Arrays.asList(""));
//	 String password = "";
	
	int timeout = GinaApiLdapUtils.LDAP_DEFAULT_TIMEOUT;

	GinaApiLdapConfiguration ldapConf = new GinaApiLdapConfiguration(server, base, user, password, timeout);
	api = GinaApiLdapBaseFactory.getInstance(ldapConf);
    }

    @Test
    public void getAllUsersTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getAllUsers("FILTER", GinaApiLdapContants.TEST_ATTRS);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserRolesTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    List<String> roles = api.getUserRoles(GinaApiLdapContants.DRIVONOL_USERNAME);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
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
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    // Utilisateur valide
	    boolean result = api.isValidUser(GinaApiLdapContants.DRIVONOL_USERNAME);
	    LOG.info("result=" + result);
	    assertTrue(result);
	    
	    // Utilisateur non valide
	    result = api.isValidUser(GinaApiLdapContants.PINAUDJ_USERNAME);
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
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    Map<String, String> user = api.getUserAttrs(GinaApiLdapContants.DRIVONOL_USERNAME,
		    GinaApiLdapContants.TEST_ATTRS);
	    for (Map.Entry<String, String> e : user.entrySet()) {
		LOG.info(e.getKey() + "=" + e.getValue());
		if ("uid".equalsIgnoreCase(e.getKey())) {
		    assertTrue(e.getValue().equalsIgnoreCase(GinaApiLdapContants.DRIVONOL_USERNAME));
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
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    boolean ret = api.hasRole("ADMIN");
	    assertSame(false, ret);
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void hasRoleUserTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    boolean ret = api.hasUserRole(GinaApiLdapContants.DRIVONOL_USERNAME, LDAP_APPLICATION_TEST_ROLE);
	    assertSame(true, ret);
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	try {
	    boolean ret = api.hasUserRole(GinaApiLdapContants.DRIVONOL_USERNAME, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION,
		    LDAP_APPLICATION_TEST_ROLE);
	    Assert.assertTrue( GinaApiLdapContants.DRIVONOL_USERNAME + " devrait avoir le role " + LDAP_APPLICATION_TEST_ROLE + " pour l'application " + LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, ret );
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getRolesTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);

	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getRoles();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getAppRolesTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    List<String> roles = api.getAppRoles("CAMAC-GENEVE");
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
	    Assert.assertTrue(roles.contains("ADMIN"));
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getUserTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUser();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserAttrsWithAttrsTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUserAttrs(GinaApiLdapContants.TEST_ATTRS);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getLanguageTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getLanguage();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getEnvironmentTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getEnvironment();
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void hasRoleWithApplicationAndRoleTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.hasRole(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, LDAP_APPLICATION_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getRolesWithApplicationTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getUserRolesWithUserAndApplicationTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	try {
	    List<String> roles = api.getUserRoles(GinaApiLdapContants.DRIVONOL_USERNAME, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
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
	LOG.info(GinaApiLdapContants.START_METHOD);
	try {
	    List<Map<String, String>> users = api.getUsers(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, GinaApiLdapContants.TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertFalse(users.isEmpty());
	    LOG.info("users.size()=" + users.size());
	    LOG.info("users=" + users);

	    boolean containsUserTest = false;
	    for( Map<String, String> user : users) {
		String uid = user.get("uid");
		if(StringUtils.isNotBlank(uid) && uid.contains(GinaApiLdapContants.DRIVONOL_USERNAME)) {
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
	LOG.info(GinaApiLdapContants.START_METHOD);
	try {
	    List<Map<String, String>> users = api.getUsers(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, LDAP_APPLICATION_TEST_ROLE, GinaApiLdapContants.TEST_ATTRS);
	    Assert.assertNotNull(users);
	    Assert.assertTrue(users.size() > 0);
	    LOG.debug("users.size()=" + users.size());
	    LOG.debug("users=" + users);
	    
	    boolean found = false;
	    for(Map<String, String> user : users) {
		    String uid = user.get("uid");
		    if(GinaApiLdapContants.DRIVONOL_USERNAME.equals(uid)) {
			found = true;
			break;
		    }
	    }
	    assertTrue("Le user " + GinaApiLdapContants.DRIVONOL_USERNAME + " devrait faire partie de la liste", found);
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
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getIntegrationUserRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, "ABC");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getIntegrationUserAttributesTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getIntegrationUserAttributes(LDAP_APPLICATION_TEST_DOMAINE, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void getBusinessRolesTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getBusinessRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersByPhoneTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUsersByPhone("ABC", true, GinaApiLdapContants.TEST_ATTRS);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersBySIRHNumberTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUsersBySIRHNumber("ABC", true, GinaApiLdapContants.TEST_ATTRS);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getUsersByNameTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUsersByName("ABC", true, GinaApiLdapContants.TEST_ATTRS);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getInheritingRolesTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getInheritingRoles(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, LDAP_APPLICATION_TEST_ROLE);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getPMProprieteMetierTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getPMProprieteMetier(LDAP_APPLICATION_TEST_DOMAINE_APPLICATION);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getOwnIDUniqueForPPorPseudoTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

            try {
        	api.getOwnIDUniqueForPPorPseudo();
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
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getOwnPMProprieteMetier("");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getPPProprieteMetierTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getPPProprieteMetier("");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void getOwnPPProprieteMetierTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getOwnPPProprieteMetier("");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
    
    @Test
    public void sendMailTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);
	
	thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        String[] foo = { "" };
        
        try {
            api.sendMail("", foo, foo, "", "", "");
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }
}
