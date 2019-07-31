/*
 * GINA LDAP client
 *
 * Copyright 2016-2019 Republique et canton de Gen�ve
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

public class GinaLdapDomainTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapDomainTest.class);

    // LDAP : domaine Gina
    private static final String DOMAIN = "CSBUGTRACK";

    // LDAP : application Gina
    private static final String APPLICATION = "ACCESS-CONTROL";

    // LDAP : domaine Gina + application Gina
    private static final String DOMAINE_APPLICATION = DOMAIN + "." + APPLICATION;

    // LDAP : role de test
    private static final String ROLE = "ACCESS-CONTROL-USERS";

    private static GinaApiLdapBaseAble api;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Trace de debut d'execution et de fin d'execution de chaque methode de test.
     */
    @Rule
    public TestWatcher watcher = new TestLoggingWatcher();

    @BeforeClass
    public static void initApi() {
//        String base = "ou=CSBUGTRACK,o=gina";
        String server = System.getProperty("test.domain.server");
        String user = System.getProperty("test.domain.user");
        String password = System.getProperty("test.domain.password");

        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(server, user, password, DOMAIN, APPLICATION);
        api = GinaLdapFactory.getNewInstance(ldapConf);
    }

    @AfterClass
    public static void releaseResources() {
        IOUtils.closeQuietly(api);
    }

    @Test
    public void isValidUserTest() throws RemoteException {
        boolean result = api.isValidUser(TestConstants.GENERIC_USERNAME);
        assertThat(result)
                .as("L'utilisateur " + TestConstants.GENERIC_USERNAME + " est cense etre valide !")
                .isTrue();
    }

    @Test
    public void getUserAttrsWithUserAndAttrsTest() throws RemoteException {
        Map<String, String> result = api.getUserAttrs(TestConstants.GENERIC_USERNAME, TestConstants.TEST_ATTRS);

        assertThat(result).isNotNull();
        assertThat(result.get("uid")).isEqualTo(TestConstants.GENERIC_USERNAME);
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
        List<String> roles = api.getUserRoles(TestConstants.GENERIC_USERNAME, DOMAINE_APPLICATION);
        assertThat(roles).isNotEmpty();
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(roles).contains(ROLE);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
    }

    @Test
    public void getUsersWithApplicationAndAttrsTest() throws RemoteException {
        patience();
        List<Map<String, String>> users = api
                .getUsers(DOMAINE_APPLICATION, TestConstants.TEST_ATTRS);
        assertThat(users).isNotEmpty();
        LOGGER.info("users.size() = {}", users.size());

        long nbUsers = users
                .stream()
                .map(user -> user.get("uid"))
                .filter(Objects::nonNull)
                .filter(uid -> uid.contains(TestConstants.GENERIC_USERNAME))
                .count();
        assertThat(nbUsers)
                .as("Le user " + TestConstants.GENERIC_USERNAME + " devrait faire partie de la liste")
                .isEqualTo(1);
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() throws RemoteException {
        boolean ret = api.hasUserRole(TestConstants.GENERIC_USERNAME, DOMAINE_APPLICATION,
                ROLE);
        assertThat(ret)
                .as(TestConstants.GENERIC_USERNAME + " devrait avoir le role " + ROLE
                          + " pour l'application " + DOMAINE_APPLICATION)
                .isTrue();
    }

    @Test
    public void getUsersTest() throws RemoteException {
        patience();
        List<Map<String, String>> users = api.getUsers(DOMAINE_APPLICATION, ROLE,
                                                       TestConstants.TEST_ATTRS);
        assertThat(users).isNotEmpty();
        LOGGER.info("users.size () = {}", users.size());

        long nbUsers = users
                .stream()
                .map(user -> user.get("uid"))
                .filter(uid -> uid.contains(TestConstants.GENERIC_USERNAME))
                .count();
        assertThat(nbUsers)
                .as("L'utilisateur " + TestConstants.GENERIC_USERNAME + " devrait faire partie de la liste")
                .isEqualTo(1);
    }

    @Test
    public void getAppRolesTest() throws RemoteException {
        List<String> roles = api.getAppRoles(DOMAINE_APPLICATION);
        assertThat(roles).isNotEmpty();
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles).contains(ROLE);
    }

    @Test
    public void getBusinessRolesTest() throws RemoteException {
        List<String> roles = api.getBusinessRoles(DOMAINE_APPLICATION);
        assertThat(roles).isNotNull();
        assertThat(roles).isEmpty();
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Test
    public void hasRoleWithApplicationAndRoleTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.hasRole(DOMAINE_APPLICATION, ROLE);
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

        api.hasUserRole(TestConstants.GENERIC_USERNAME, ROLE);
    }

    @Test
    public void getUserRolesWithUserTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getUserRoles(TestConstants.GENERIC_USERNAME);
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

        api.getIntegrationUserRoles(DOMAIN, "ABC");
    }

    @Test
    public void getIntegrationUserAttributesTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getIntegrationUserAttributes(DOMAIN, DOMAINE_APPLICATION);
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

        api.getInheritingRoles(DOMAINE_APPLICATION, ROLE);
    }

    @Test
    public void getPMProprieteMetierTest() throws RemoteException {
        expectNotImplemented(thrown);

        api.getPMProprieteMetier(DOMAINE_APPLICATION);
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

    private void patience() {
        LOGGER.info("Patience... Gina est parfois lent a repondre");
    }

}
