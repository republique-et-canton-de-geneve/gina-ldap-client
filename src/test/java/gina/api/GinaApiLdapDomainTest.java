package gina.api;

import static gina.api.gina.api.utils.TestTools.expectNotImplemented;
import static org.assertj.core.api.Assertions.assertThat;

import gina.api.gina.api.utils.TestConstants;
import gina.api.gina.api.utils.TestLoggingWatcher;
import gina.api.gina.api.utils.TestTools;
import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
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

    // LDAP au niveau du domaine - Rôle de test
    private static final String LDAP_DOMAIN_TEST_ROLE = "ACCESS-CONTROL-USERS";

    private static GinaApiLdapBaseAble api;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Affichage du début et de la fin de chaque methode de test.
     */
    @Rule
    public TestWatcher watcher = new TestLoggingWatcher();

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
    public void isValidUserTest() throws RemoteException {
        boolean result = api.isValidUser(TestConstants.DTDCOURS01_USERNAME);
        assertThat(result)
                .as("L'utilisateur " + TestConstants.DTDCOURS01_USERNAME + " est censé être valide !")
                .isTrue();
    }

    @Test
    public void getUserAttrsWithUserAndAttrsTest() throws RemoteException {
        Map<String, String> result = api.getUserAttrs(TestConstants.DTDCOURS01_USERNAME, TestConstants.TEST_ATTRS);

        assertThat(result).isNotNull();
        assertThat(result.get("sn")).isEqualTo(TestConstants.DTDCOURS01_USERNAME);
        assertThat(result.get("initials")).isNotNull();
        assertThat(result.get("givenName")).isNotNull();
    }

    @Test
    public void getUserAttrsWithUserAndAttrs_wildcard_Test() throws RemoteException {
        Map<String, String> users = api.getUserAttrs("*", TestConstants.TEST_ATTRS);
        assertThat(users).isEmpty();
    }

    @Test
    public void getUserRolesWithUserAndApplicationTest() throws RemoteException {
        List<String> roles = api.getUserRoles(TestConstants.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
        assertThat(roles).isNotEmpty();
        LOG.info("roles.size() = {}", roles.size());
        LOG.info("roles = {}", roles);
        assertThat(roles).contains(LDAP_DOMAIN_TEST_ROLE);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
    }

    @Test
    public void getUsersWithApplicationAndAttrsTest() throws RemoteException {
        List<Map<String, String>> users = api
                .getUsers(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, TestConstants.TEST_ATTRS);
        assertThat(users).isNotEmpty();
        LOG.info("users.size() = {}", users.size());
        LOG.debug("users = ", users);

        boolean containsUserTest = false;
        for (Map<String, String> user : users) {
            String sn = user.get("sn");
            if (StringUtils.isNotBlank(sn) && sn.contains(TestConstants.DTDCOURS01_USERNAME)) {
                containsUserTest = true;
            }
        }
        assertThat(containsUserTest).isTrue();
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() throws RemoteException {
        boolean ret = api.hasUserRole(TestConstants.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION,
                                      LDAP_DOMAIN_TEST_ROLE);
        assertThat(ret)
                .as(TestConstants.DTDCOURS01_USERNAME + " devrait avoir le role " + LDAP_DOMAIN_TEST_ROLE
                          + " pour l'application " + LDAP_DOMAIN_TEST_DOMAINE_APPLICATION)
                .isTrue();
    }

    @Test
    public void getUsersTest() throws RemoteException {
        List<Map<String, String>> users = api.getUsers(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE,
                                                       TestConstants.TEST_ATTRS);
        assertThat(users).isNotEmpty();
        LOG.debug("users.size () = ", users.size());

        boolean found = false;
        for (Map<String, String> user : users) {
            String uid = user.get("uid");
            if (TestConstants.DTDCOURS01_USERNAME.equals(uid)) {
                found = true;
                break;
            }
        }
        assertThat(found)
                .as("Le user " + TestConstants.DTDCOURS01_USERNAME + " devrait faire partie de la liste")
                .isTrue();
    }

    @Test
    public void getAppRolesTest() throws RemoteException {
        List<String> roles = api.getAppRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
        assertThat(roles).isNotEmpty();
        LOG.debug("roles.size() = {}", roles.size());
        LOG.debug("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles).contains(LDAP_DOMAIN_TEST_ROLE);
    }

    @Test
    public void getBusinessRolesTest() throws RemoteException {
        List<String> roles = api.getBusinessRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
        assertThat(roles).isNotEmpty();
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Test
    public void hasRoleWithApplicationAndRoleTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.hasRole(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE);
    }

    @Test
    public void getAllUsersTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getAllUsers("FILTER", TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUserAttrsWithAttrsTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUserAttrs(TestConstants.TEST_ATTRS);
    }

    @Test
    public void getRolesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getRoles("APPLICATION");
    }

    @Test
    public void hasUserRoleTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.hasUserRole(TestConstants.DTDCOURS01_USERNAME, LDAP_DOMAIN_TEST_ROLE);
    }

    @Test
    public void getUserRolesWithUserTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUserRoles(TestConstants.DTDCOURS01_USERNAME);
    }

    @Test
    public void getUserTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUser();
    }

    @Test
    public void getLanguageTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getLanguage();
    }

    @Test
    public void getEnvironmentTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getEnvironment();
    }

    @Test
    public void getIntegrationUserRolesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getIntegrationUserRoles(LDAP_DOMAIN_TEST_DOMAINE, "ABC");
    }

    @Test
    public void getIntegrationUserAttributesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getIntegrationUserAttributes(LDAP_DOMAIN_TEST_DOMAINE, LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
    }

    @Test
    public void getUsersByPhoneTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUsersByPhone("ABC", true, TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUsersBySIRHNumberTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUsersBySIRHNumber("ABC", true, TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUsersByNameTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUsersByName("ABC", true, TestConstants.TEST_ATTRS);
    }

    @Test
    public void getInheritingRolesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getInheritingRoles(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION, LDAP_DOMAIN_TEST_ROLE);
    }

    @Test
    public void getPMProprieteMetierTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getPMProprieteMetier(LDAP_DOMAIN_TEST_DOMAINE_APPLICATION);
    }

    @Test
    public void getOwnIDUniqueForPPorPseudoTest() throws RemoteException, NamingException {
        expectNotImplemented(thrown);

        api.getOwnIDUniqueForPPorPseudo();
    }

    @Test
    public void getOwnPMProprieteMetierTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getOwnPMProprieteMetier("");
    }

    @Test
    public void getPPProprieteMetierTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getPPProprieteMetier("");
    }

    @Test
    public void getOwnPPProprieteMetierTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getOwnPPProprieteMetier("");
    }

    @Test
    public void sendMailTest() throws RemoteException {
        expectNotImplemented(thrown);

        String[] foo = { "" };
        api.sendMail("", foo, foo, "", "", "");
    }

}
