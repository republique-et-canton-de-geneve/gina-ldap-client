/*
 * GINA LDAP client
 *
 * Copyright 2016-2019 Republique et canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gina.api;

import gina.api.utils.TestConstants;
import gina.api.utils.TestLoggingWatcher;
import gina.api.utils.TestTools;
import gina.impl.GinaLdapFactory;
import gina.impl.util.GinaLdapConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static gina.api.utils.TestTools.expectNotImplemented;
import static gina.api.utils.TestTools.getGinaLdapConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class GinaLdapApplicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapApplicationTest.class);

    // LDAP : domaine Gina
    protected static final String DOMAIN = "OAC";

    // LDAP : application Gina
    protected static final String APPLICATION = "CAMAC-GENEVE";

    // LDAP : domaine Gina + application Gina
    private static final String DOMAIN_APPLICATION = DOMAIN + "." + APPLICATION;

    // LDAP : role de test
//    private static final String ROLE = DOMAIN_APPLICATION + "." + "UTILISATEUR";
    private static final String ROLE = "UTILISATEUR";

    static GinaApiLdapBaseAble api;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Trace de debut d'execution et de fin d'execution de chaque methode de test.
     */
    @Rule
    public TestWatcher watcher = new TestLoggingWatcher();

    @BeforeClass
    public static void initApi() {
        String server = System.getProperty("test.application.server");
        String user = System.getProperty("test.application.user");
        String password = System.getProperty("test.application.password");

        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(server, user, password, DOMAIN, APPLICATION);
        api = GinaLdapFactory.getNewInstance(ldapConf);
    }

    @AfterClass
    public static void releaseResources() {
        IOUtils.closeQuietly(api);
    }

    @Test
    public void getAllUsersTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getAllUsers("FILTER", TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUserRolesTest() throws RemoteException {
        List<String> roles = api.getUserRoles(TestConstants.DRIVONOL_USERNAME);

        assertThat(roles).isNotEmpty();
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains("UTILISATEUR")).isTrue();
        assertThat(roles.contains("CONTEXTE-APA")).isTrue();
    }

    @Test
    public void getIsValidUserTest() throws RemoteException {
        // Utilisateur valide
        boolean result = api.isValidUser(TestConstants.DRIVONOL_USERNAME);
        assertThat(result).isTrue();

        // Utilisateur non valide
        result = api.isValidUser(TestConstants.PINAUDJ_USERNAME);
        assertThat(result).isFalse();
    }

    @Test
    public void getUserAttrsTest() throws RemoteException {
        Map<String, String> user = api.getUserAttrs(TestConstants.DRIVONOL_USERNAME, TestConstants.TEST_ATTRS);

        for (Map.Entry<String, String> e : user.entrySet()) {
            LOGGER.info(e.getKey() + " = {}", e.getValue());
            if ("uid".equalsIgnoreCase(e.getKey())) {
                assertThat(e.getValue().equalsIgnoreCase(TestConstants.DRIVONOL_USERNAME)).isTrue();
    //            break;
            }
        }
        LOGGER.info("user = {}", user);
    }

    @Test
    public void hasRoleUserTest() throws RemoteException {
        boolean ret = api.hasUserRole(TestConstants.DRIVONOL_USERNAME, ROLE);

        List<String> roles = api.getUserRoles(TestConstants.DRIVONOL_USERNAME, ROLE);
        LOGGER.info("roles = {}", roles);

        assertThat(ret)
                .as("User " + TestConstants.DRIVONOL_USERNAME + " is expected to have role " + ROLE)
                .isTrue();
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() throws RemoteException {
        boolean ret = api.hasUserRole(TestConstants.DRIVONOL_USERNAME, DOMAIN_APPLICATION, ROLE);
//        boolean ret = api.hasUserRole(TestConstants.DRIVONOL_USERNAME, DOMAIN_APPLICATION, "UTILISATEUR");
        assertThat(ret)
                .as(TestConstants.DRIVONOL_USERNAME + " devrait avoir le role " + ROLE + " pour l'application " + DOMAIN_APPLICATION)
                .isTrue();
    }

    @Test
    public void getAppRolesTest() throws RemoteException {
        List<String> roles = api.getAppRoles("CAMAC-GENEVE");

        assertThat(roles).isNotEmpty();
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains("ADMIN")).isTrue();
    }

    @Test
    public void getUserTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUser();
    }

    @Test
    public void getUserAttrsWithAttrsTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUserAttrs(TestConstants.TEST_ATTRS);
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
    public void hasRoleWithApplicationAndRoleTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.hasRole(DOMAIN_APPLICATION, ROLE);
    }

    @Test
    public void getRolesWithApplicationTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getRoles(DOMAIN_APPLICATION);
    }

    @Test
    public void getUserRolesWithUserAndApplicationTest() throws RemoteException {
        List<String> roles = api.getUserRoles(TestConstants.DRIVONOL_USERNAME, DOMAIN_APPLICATION);

        assertThat(roles).isNotEmpty();
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains(ROLE)).isTrue();
    }

    @Test
    public void getUsersWithApplicationAndAttrsTest() throws RemoteException {
        List<Map<String, String>> users = api.getUsers(DOMAIN_APPLICATION, TestConstants.TEST_ATTRS);

        assertThat(users).isNotEmpty();
        LOGGER.info("users.size() = {}", users.size());
        LOGGER.info("users = {}", users);

        long nbUsers = users
                .stream()
                .map(user -> user.get("uid"))
                .filter(Objects::nonNull)
                .filter(uid -> uid.contains(TestConstants.DRIVONOL_USERNAME))
                .count();
        assertThat(nbUsers)
                .as("L'utilisateur " + TestConstants.DRIVONOL_USERNAME + " devrait faire partie de la liste")
                .isEqualTo(1);
    }

    @Test
    public void getUsersTest() throws RemoteException {
        List<Map<String, String>> users = api.getUsers(DOMAIN_APPLICATION, ROLE, null /*TestConstants.TEST_ATTRS*/);

        assertThat(users).isNotEmpty();
        LOGGER.info("users.size() = {}", users.size());
        LOGGER.info("users = {}", users);

        long nbUsers = users
                .stream()
                .map(user -> user.get("uid"))    // NE MARCHE PLUS
//                .map(user -> user.get("dn"))    // MARCHE
                .peek(u -> LOGGER.info("user = {}", u))
                .filter(uid -> uid.contains(TestConstants.DRIVONOL_USERNAME))
                .count();
        assertThat(nbUsers).isEqualTo(1);
    }

    @Test
    public void getBusinessRolesTest() throws RemoteException {
        List<String> roles = api.getBusinessRoles(DOMAIN_APPLICATION);
        assertThat(roles).isNotNull();
        assertThat(roles).isEmpty();
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Test
    public void getIntegrationUserRolesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getIntegrationUserRoles(DOMAIN_APPLICATION, "ABC");
    }

    @Test
    public void getIntegrationUserAttributesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getIntegrationUserAttributes(DOMAIN, DOMAIN_APPLICATION);
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

        api.getInheritingRoles(DOMAIN_APPLICATION, ROLE);
    }

    @Test
    public void getPMProprieteMetierTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getPMProprieteMetier(DOMAIN_APPLICATION);
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
