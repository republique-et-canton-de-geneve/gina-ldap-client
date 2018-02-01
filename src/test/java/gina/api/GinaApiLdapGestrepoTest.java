package gina.api;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;

public class GinaApiLdapGestrepoTest {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapGestrepoTest.class);

    // LDAP au niveau de l'application - Domaine Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE = "CTI";

    // LDAP au niveau de l'application - Application Gina
    private static final String LDAP_APPLICATION_TEST_APPLICATION = "GESTREPO";

    // LDAP au niveau de l'application - Domaine + Application Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE_APPLICATION = LDAP_APPLICATION_TEST_DOMAINE + "."
	    + LDAP_APPLICATION_TEST_APPLICATION;

    // LDAP au niveau du domaine - User de test
    private static final String LDAP_APPLICATION_TEST_USER = "LAROCHEP";

    // LDAP au niveau du domaine - Rôle de test
    private static final String LDAP_APPLICATION_TEST_ROLE = "UTILISATEUR";

    private static GinaApiLdapBaseAble api;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initApi() {
	String base = "ou=CTI,o=gina";

	String server = "ldaps://vldap-dev.ceti.etat-ge.ch:636";
	String user = "cn=TCNVLDAP4363DEVAAG,ou=Users,ou=gestrepo,ou=cti,o=gina";
	String password = "uopt7690";

	GinaApiLdapConfiguration ldapConf = new GinaApiLdapConfiguration(server, base, user, password,
		GinaApiLdapUtils.LDAP_DEFAULT_TIMEOUT);
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
	    List<String> roles = api.getUserRoles(LDAP_APPLICATION_TEST_USER);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
	    Assert.assertTrue(roles.contains(LDAP_APPLICATION_TEST_ROLE));
	    Assert.assertTrue(roles.contains("DEVELOPPEUR-CTI"));
	} catch (GinaException e) {
	    LOG.error(e);
	    assertTrue(false);
	} catch (RemoteException e) {
	    LOG.error(e);
	    assertTrue(false);
	}
    }

    @Test
    public void isValidUserTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    // Utilisateur valide
	    boolean result = api.isValidUser(LDAP_APPLICATION_TEST_USER);
	    LOG.info("result=" + result);
	    assertTrue(result);

	    // Utilisateur non valide
	    result = api.isValidUser("TAGADA");
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
	    Map<String, String> user = api.getUserAttrs(LDAP_APPLICATION_TEST_USER, GinaApiLdapContants.TEST_ATTRS);

	    for (Map.Entry<String, String> e : user.entrySet()) {
		LOG.info(e.getKey() + "=" + e.getValue());
		if ("uid".equalsIgnoreCase(e.getKey())) {
		    assertTrue(e.getValue().equalsIgnoreCase(LDAP_APPLICATION_TEST_USER));
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
    public void hasRoleUserTest() {
	LOG.info(GinaApiLdapContants.START_METHOD);

	try {
	    boolean ret = api.hasUserRole(LDAP_APPLICATION_TEST_USER, LDAP_APPLICATION_TEST_ROLE);
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
	    boolean ret = api.hasUserRole(LDAP_APPLICATION_TEST_USER, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION,
		    LDAP_APPLICATION_TEST_ROLE);
	    Assert.assertTrue(GinaApiLdapContants.DRIVONOL_USERNAME + " devrait avoir le role "
		    + LDAP_APPLICATION_TEST_ROLE + " pour l'application " + LDAP_APPLICATION_TEST_DOMAINE_APPLICATION,
		    ret);
	} catch (GinaException e) {
	    LOG.error(e);
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
	    List<String> roles = api.getAppRoles(LDAP_APPLICATION_TEST_APPLICATION);
	    Assert.assertNotNull(roles);
	    Assert.assertTrue(roles.size() > 0);
	    LOG.info("roles.size()=" + roles.size());
	    LOG.info("roles=" + roles);
	    Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
	    Assert.assertTrue(roles.contains("ADMINISTRATEUR"));
	    Assert.assertTrue(roles.contains("SVN-READONLY-ALL"));
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
}
