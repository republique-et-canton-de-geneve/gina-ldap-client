package gina.api;

import static gina.impl.util.GinaLdapConfiguration.Type.DOMAIN;
import static org.assertj.core.api.Assertions.assertThat;
import static gina.impl.util.GinaLdapConfiguration.Type.APPLICATION;

import gina.api.gina.api.utils.TestConstants;
import gina.api.gina.api.utils.TestLoggingWatcher;
import gina.api.gina.api.utils.TestTools;
import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapUtils;
import gina.impl.GinaLdapCommon;
import gina.impl.GinaLdapFactory;
import gina.impl.GinaException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaLdapApplicationCtiGestrepoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapApplicationCtiGestrepoTest.class);

    // LDAP au niveau de l'application - Domaine Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE = "CTI";

    // LDAP au niveau de l'application - Application Gina
    private static final String LDAP_APPLICATION_TEST_APPLICATION = "GESTREPO";

    // LDAP au niveau de l'application - Domaine + Application Gina
    private static final String LDAP_APPLICATION_TEST_DOMAINE_APPLICATION =
            LDAP_APPLICATION_TEST_DOMAINE + "." + LDAP_APPLICATION_TEST_APPLICATION;

    // LDAP au niveau du domaine - User de test
    private static final String LDAP_APPLICATION_TEST_USER = "LAROCHEP";

    // LDAP au niveau du domaine - R�le de test
    private static final String LDAP_APPLICATION_TEST_ROLE = "UTILISATEUR";

    private static GinaApiLdapBaseAble api;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Affichage du d�but et de la fin de chaque methode de test.
     */
    @Rule
    public TestWatcher watcher = new TestLoggingWatcher();

    @BeforeClass
    public static void initApi() {
        String base = "ou=CTI,o=gina";

        String server = System.getProperty("test.gestrepo.server");
        String user = System.getProperty("test.gestrepo.user");
        String password = System.getProperty("test.gestrepo.password");

        LOGGER.info("Connexion LDAP : server=[{}], user=[{}]", server, user);
        if (StringUtils.isBlank(password)) {
            LOGGER.info("le mot de passe au serveur LDAP (necessaire avec Gina, inutile avec UnboundID) est manquant");
        }

        GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(server, base, user, password, APPLICATION,
                GinaLdapUtils.LDAP_DEFAULT_TIMEOUT);
        api = GinaLdapFactory.getInstance(ldapConf);
    }

    @Test
    public void getAllUsersTest() throws RemoteException {
        thrown.expect(GinaException.class);
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapCommon.NOT_IMPLEMENTED));

         api.getAllUsers("FILTER", TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUserRolesTest() throws RemoteException {
        List<String> roles = api.getUserRoles(LDAP_APPLICATION_TEST_USER);

        assertThat(roles).isNotNull();
        assertThat(roles.size()).isGreaterThan(0);
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains(LDAP_APPLICATION_TEST_ROLE)).isTrue();
        assertThat(roles.contains("DEVELOPPEUR-CTI")).isTrue();
    }

    @Test
    public void isValidUserTest() throws RemoteException {
        // Utilisateur valide
        boolean result = api.isValidUser(LDAP_APPLICATION_TEST_USER);
        assertThat(result).isTrue();

        // Utilisateur non valide
        result = api.isValidUser("TAGADA");
        assertThat(result).isFalse();
    }

    @Test
    public void getUserAttrsTest() throws RemoteException {
        Map<String, String> attributes = api.getUserAttrs(LDAP_APPLICATION_TEST_USER, TestConstants.TEST_ATTRS);

        Optional<Map.Entry<String, String>> entry = attributes.entrySet()
                .stream()
                .filter(e -> "uid".equalsIgnoreCase(e.getKey()))
                .findAny();

        assertThat(entry)
                .isPresent()
                .hasValueSatisfying(e ->
                        assertThat(e.getValue())
                                .as("Le user " + LDAP_APPLICATION_TEST_USER + " devrait faire partie de la liste")
                                .isEqualToIgnoringCase(LDAP_APPLICATION_TEST_USER));

        LOGGER.info("attribute = {}", entry.get());
    }

    @Test
    public void hasRoleUserTest() throws RemoteException {
        boolean ret = api.hasUserRole(LDAP_APPLICATION_TEST_USER, LDAP_APPLICATION_TEST_ROLE);
        assertThat(ret).isTrue();
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() throws RemoteException {
        boolean ret = api.hasUserRole(LDAP_APPLICATION_TEST_USER, LDAP_APPLICATION_TEST_DOMAINE_APPLICATION,
                LDAP_APPLICATION_TEST_ROLE);
        assertThat(ret).as(TestConstants.DRIVONOL_USERNAME + " devrait avoir le role "
                         + LDAP_APPLICATION_TEST_ROLE + " pour l'application "
                         + LDAP_APPLICATION_TEST_DOMAINE_APPLICATION)
                       .isTrue();
    }

    @Test
    public void getAppRolesTest() throws RemoteException {
        List<String> roles = api.getAppRoles(LDAP_APPLICATION_TEST_APPLICATION);
        assertThat(roles).isNotNull();
        assertThat(roles.size()).isGreaterThan(0);
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains("ADMINISTRATEUR")).isTrue();
        assertThat(roles.contains("SVN-READONLY-ALL")).isTrue();

        for (String role : roles) {
            List<Map<String, String>> users = api.getUsers("GESTREPO", role, new String[] {"cn"});
            LOGGER.info("users for role {}: list of size {}", role, users.size());
        }
    }

}
