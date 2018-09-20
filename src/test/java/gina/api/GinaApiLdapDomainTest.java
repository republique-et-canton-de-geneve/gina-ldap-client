package gina.api;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaApiLdapDomainTest {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(GinaApiLdapDomainTest.class);

    // LDAP au niveau du domaine - Domaine Gina
    private static final String LDAP_DOMAIN_TEST_DOMAINE = "CSBUGTRACK";

    // LDAP au niveau du domaine - Application Gina
    private static final String LDAP_DOMAIN_TEST_APPLICATION = "ACCESS-CONTROL";

    // LDAP au niveau du domaine - Domaine + Application Gina
    private static final String LDAP_DOMAIN_TEST_DOMAINE_APPLICATION =
            LDAP_DOMAIN_TEST_DOMAINE + "." + LDAP_DOMAIN_TEST_APPLICATION;

    // LDAP au niveau du domaine - R�le de test
    private static final String LDAP_DOMAIN_TEST_ROLE = "ACCESS-CONTROL-USERS";

    private static GinaApiLdapBaseAble api;

    @Rule public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initApi() {
        String base = "ou=CSBUGTRACK,o=gina";

        String server = "ldaps://vldap-dev.ceti.etat-ge.ch:636";
        String user = "cn=tcnvldap6470devaag,ou=Users,ou=CSBUGTRACK,o=gina";
        String password = "Xhngmfxp9";

        // String server = "ldap://127.0.0.1:30636";
        // String user = "";
        // String password = "";

        int timeout = GinaApiLdapUtils.LDAP_DEFAULT_TIMEOUT;

        GinaApiLdapConfiguration ldapConf = new GinaApiLdapConfiguration(server, base, user, password, timeout);
        api = GinaApiLdapBaseFactory.getInstance(ldapConf);
    }

    @Test
    public void isValidUserTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            boolean result = api.isValidUser(GinaApiLdapConstants.DTDCOURS01_USERNAME);
            if (!result) {
                Assert.assertTrue(
                        "L'utilisateur " + GinaApiLdapConstants.DTDCOURS01_USERNAME + " est cens� �tre valide !",
                        result);
            }
        } catch (GinaException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
        assertTrue(true);
    }

    @Test
    public void getUserAttrsWithUserAndAttrsTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            Map<String, String> result = api
                    .getUserAttrs(GinaApiLdapConstants.DTDCOURS01_USERNAME, GinaApiLdapConstants.TEST_ATTRS);
            Assert.assertNotNull(result);
            Assert.assertEquals(GinaApiLdapConstants.DTDCOURS01_USERNAME, result.get("sn"));
            Assert.assertNotNull(result.get("initials"));
            Assert.assertNotNull(result.get("givenName"));
        } catch (GinaException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
        assertTrue(true);
    }

    @Test
    public void getUserAttrsWithUserAndAttrs_wildcard_Test() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        try {
            Map<String, String> users = api.getUserAttrs("*", GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(users == null || users.size() == 0);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUserRolesWithUserAndApplicationTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            List<String> roles = api
                    .getUserRoles(GinaApiLdapConstants.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.size() > 0);
            LOG.info("roles.size()=" + roles.size());
            LOG.info("roles=" + roles);
            Assert.assertTrue(roles.contains(LDAP_DOMAIN_TEST_ROLE));
            Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
        } catch (GinaException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
        assertTrue(true);
    }

    @Test
    public void getUsersWithApplicationAndAttrsTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            List<Map<String, String>> users = api
                    .getUsers(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, GinaApiLdapConstants.TEST_ATTRS);
            Assert.assertNotNull(users);
            Assert.assertFalse(users.isEmpty());
            LOG.info("users.size()=" + users.size());
            LOG.debug("users=" + users);

            boolean containsUserTest = false;
            for (Map<String, String> user : users) {
                String sn = user.get("sn");
                if (StringUtils.isNotBlank(sn) && sn.contains(GinaApiLdapConstants.DTDCOURS01_USERNAME)) {
                    containsUserTest = true;
                }
            }
            Assert.assertTrue(containsUserTest);
        } catch (GinaException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
        assertTrue(true);
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            boolean ret = api
                    .hasUserRole(GinaApiLdapConstants.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION,
                            LDAP_DOMAIN_TEST_ROLE);
            Assert.assertTrue(
                    GinaApiLdapConstants.DTDCOURS01_USERNAME + " devrait avoir le role " + LDAP_DOMAIN_TEST_ROLE
                            + " pour l'application " + LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, ret);
        } catch (GinaException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUsersTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            List<Map<String, String>> users = api.getUsers(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE,
                    GinaApiLdapConstants.TEST_ATTRS);
            Assert.assertNotNull(users);
            Assert.assertTrue(users.size() > 0);
            LOG.debug("users.size()=" + users.size());

            boolean found = false;
            for (Map<String, String> user : users) {
                String uid = user.get("uid");
                if (GinaApiLdapConstants.DTDCOURS01_USERNAME.equals(uid)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                assertTrue("Le user " + GinaApiLdapConstants.DTDCOURS01_USERNAME + " devrait faire partie de la liste",
                        false);
            }
        } catch (GinaException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
        assertTrue(true);
    }

    @Test
    public void getAppRolesTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            List<String> roles = api.getAppRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.size() > 0);
            LOG.debug("roles.size()=" + roles.size());
            LOG.debug("roles=" + roles);
            Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
            Assert.assertTrue(roles.contains(LDAP_DOMAIN_TEST_ROLE));
        } catch (GinaException e) {
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
        assertTrue(true);
    }

    @Test
    public void getBusinessRolesTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);
        try {
            List<String> roles = api.getBusinessRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.size() == 0);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Test
    public void hasRoleWithApplicationAndRoleTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.hasRole(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getAllUsersTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getAllUsers("FILTER", GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUserAttrsWithAttrsTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUserAttrs(GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getRolesTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getRoles("APPLICATION");
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void hasUserRoleTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.hasUserRole(GinaApiLdapConstants.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_ROLE);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUserRolesWithUserTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUserRoles(GinaApiLdapConstants.DTDCOURS01_USERNAME);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUserTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUser();
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getLanguageTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getLanguage();
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getEnvironmentTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getEnvironment();
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getIntegrationUserRolesTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getIntegrationUserRoles(LDAP_DOMAIN_TEST_DOMAINE, "ABC");
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getIntegrationUserAttributesTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getIntegrationUserAttributes(LDAP_DOMAIN_TEST_DOMAINE, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUsersByPhoneTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUsersByPhone("ABC", true, GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUsersBySIRHNumberTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUsersBySIRHNumber("ABC", true, GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUsersByNameTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getUsersByName("ABC", true, GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getInheritingRolesTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getInheritingRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getPMProprieteMetierTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getPMProprieteMetier(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getOwnIDUniqueForPPorPseudoTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getOwnIDUniqueForPPorPseudo();
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } catch (NamingException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getOwnPMProprieteMetierTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getOwnPMProprieteMetier("");
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getPPProprieteMetierTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getPPProprieteMetier("");
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getOwnPPProprieteMetierTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getOwnPPProprieteMetier("");
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void sendMailTest() {
        LOG.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        String[] foo = { "" };

        try {
            api.sendMail("", foo, foo, "", "", "");
            assertTrue(false);
        } catch (RemoteException e) {
            LOG.error("Erreur : ", e);;
            assertTrue(false);
        } finally {
            LOG.info(GinaApiLdapConstants.END_METHOD);
        }
    }

}
