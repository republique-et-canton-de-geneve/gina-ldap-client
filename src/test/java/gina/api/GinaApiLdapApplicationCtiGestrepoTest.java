package gina.api;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

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

public class GinaApiLdapApplicationCtiGestrepoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaApiLdapApplicationCtiGestrepoTest.class);

    // LDAP au niveau de l'application - Domaine Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE = "CTI";

    // LDAP au niveau de l'application - Application Gina
    private static final String LDAP_APPLICATION_TEST_APPLICATION = "GESTREPO";

    // LDAP au niveau de l'application - Domaine + Application Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE_APPLICATION =
            LDAP_APPLICATION_TEST_DOMAINE + "." + LDAP_APPLICATION_TEST_APPLICATION;

    // LDAP au niveau du domaine - User de test
    private static final String LDAP_APPLICATION_TEST_USER = "LAROCHEP";

    // LDAP au niveau du domaine - Rôle de test
    private static final String LDAP_APPLICATION_TEST_ROLE = "UTILISATEUR";

    private static GinaApiLdapBaseAble api;

    @Rule public ExpectedException thrown = ExpectedException.none();

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
        LOGGER.info(GinaApiLdapConstants.START_METHOD);

        thrown.expect(GinaException.class);
        thrown.expectMessage(JUnitMatchers.containsString(GinaApiLdapBaseAbleCommon.NOT_IMPLEMENTED));

        try {
            api.getAllUsers("FILTER", GinaApiLdapConstants.TEST_ATTRS);
            assertTrue(false);
        } catch (RemoteException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUserRolesTest() {
        LOGGER.info(GinaApiLdapConstants.START_METHOD);

        try {
            List<String> roles = api.getUserRoles(LDAP_APPLICATION_TEST_USER);
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.size() > 0);
            LOGGER.info("roles.size()=" + roles.size());
            LOGGER.info("roles=" + roles);
            Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
            Assert.assertTrue(roles.contains(LDAP_APPLICATION_TEST_ROLE));
            Assert.assertTrue(roles.contains("DEVELOPPEUR-CTI"));
        } catch (GinaException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } catch (RemoteException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void isValidUserTest() {
        LOGGER.info(GinaApiLdapConstants.START_METHOD);

        try {
            // Utilisateur valide
            boolean result = api.isValidUser(LDAP_APPLICATION_TEST_USER);
            LOGGER.info("result=" + result);
            assertTrue(result);

            // Utilisateur non valide
            result = api.isValidUser("TAGADA");
            LOGGER.info("result=" + result);
            assertTrue(!result);
        } catch (GinaException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } catch (RemoteException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getUserAttrsTest() {
        LOGGER.info(GinaApiLdapConstants.START_METHOD);

        try {
            Map<String, String> user = api.getUserAttrs(LDAP_APPLICATION_TEST_USER, GinaApiLdapConstants.TEST_ATTRS);

            for (Map.Entry<String, String> e : user.entrySet()) {
                LOGGER.info(e.getKey() + "=" + e.getValue());
                if ("uid".equalsIgnoreCase(e.getKey())) {
                    assertTrue(e.getValue().equalsIgnoreCase(LDAP_APPLICATION_TEST_USER));
                    break;
                }
            }
            LOGGER.info("user=" + user);
        } catch (GinaException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } catch (RemoteException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void hasRoleUserTest() {
        LOGGER.info(GinaApiLdapConstants.START_METHOD);

        try {
            boolean ret = api.hasUserRole(LDAP_APPLICATION_TEST_USER, LDAP_APPLICATION_TEST_ROLE);
            assertSame(true, ret);
        } catch (GinaException e) {
            assertTrue(false);
        } catch (RemoteException e) {
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() {
        LOGGER.info(GinaApiLdapConstants.START_METHOD);
        try {
            boolean ret = api.hasUserRole(LDAP_APPLICATION_TEST_USER, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION,
                    LDAP_APPLICATION_TEST_ROLE);
            Assert.assertTrue(
                    GinaApiLdapConstants.DRIVONOL_USERNAME + " devrait avoir le role " + LDAP_APPLICATION_TEST_ROLE
                            + " pour l'application " + LDAP_APPLICATION_TEST_DOMAINE_APPLICATION, ret);
        } catch (GinaException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } catch (RemoteException e) {
            LOGGER.error("Erreur : ", e);
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }

    @Test
    public void getAppRolesTest() {
        LOGGER.info(GinaApiLdapConstants.START_METHOD);

        try {
            List<String> roles = api.getAppRoles(LDAP_APPLICATION_TEST_APPLICATION);
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.size() > 0);
            LOGGER.info("roles.size()=" + roles.size());
            LOGGER.info("roles=" + roles);
            Assert.assertTrue(GinaApiLdapTools.checkRolesAreCleaned(roles));
            Assert.assertTrue(roles.contains("ADMINISTRATEUR"));
            Assert.assertTrue(roles.contains("SVN-READONLY-ALL"));
        } catch (GinaException e) {
            assertTrue(false);
        } catch (RemoteException e) {
            assertTrue(false);
        } finally {
            LOGGER.info(GinaApiLdapConstants.END_METHOD);
        }
    }
}
