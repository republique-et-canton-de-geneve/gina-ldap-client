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
import gina.impl.GinaException;
import gina.impl.GinaLdapAccess;
import gina.impl.util.GinaLdapConfiguration;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste les methodes d'acces aux utilisateurs, a leurs roles et a leurs attributs.
 */
public class UserRoleAttributeTest extends AbstractUserRoleAttributeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleAttributeTest.class);

    // LDAP : utilisateur de test
    private static final String USER = "LAROCHEP";

    // LDAP : role de test
    private static final String ROLE = "UTILISATEUR";

    protected static GinaApiLdapBaseAble gina;

    private static GinaLdapConfiguration ldapConf;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Trace de debut d'execution et de fin d'execution de chaque methode de test.
     */
    @Rule
    public TestWatcher watcher = new TestLoggingWatcher();

    @BeforeClass
    public static void initApi() {
        LOGGER.info("initApi");

        ldapConf = getGinaLdapConfiguration();
        gina = new GinaLdapAccess(ldapConf);
    }

    @AfterClass
    public static void releaseResources() {
        IOUtils.closeQuietly(gina);
    }

    @Test
    public void getAllUsers() throws RemoteException {
        thrown.expect(GinaException.class);
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapAccess.NOT_IMPLEMENTED));

         gina.getAllUsers("FILTER", TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUserRoles() throws RemoteException {
        List<String> roles = gina.getUserRoles(USER);

        assertThat(roles).isNotNull();
        assertThat(roles.size()).isGreaterThan(0);
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains(ROLE)).isTrue();
        assertThat(roles.contains("DEVELOPPEUR-CTI")).isTrue();
    }

    @Test
    public void getUserRolesForApplication() throws RemoteException {
        List<String> roles = gina.getUserRoles(USER, DOMAIN_APPLICATION);

        assertThat(roles).isNotEmpty();
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains(ROLE)).isTrue();
    }

    @Test
    public void getUsersWithAttrs() throws RemoteException {
        List<Map<String, String>> users = gina.getUsers(DOMAIN_APPLICATION, ROLE, TestConstants.TEST_ATTRS);

        assertThat(users).isNotEmpty();
        LOGGER.info("users.size() = {}", users.size());
        LOGGER.debug("users = {}", users);

        long nbUsers = users
                .stream()
                .map(user -> user.get("uid"))
                .filter(Objects::nonNull)
                .filter(uid -> uid.contains(USER))
                .count();
        assertThat(nbUsers)
                .as("L'utilisateur " + USER + " devrait faire partie de la liste")
                .isEqualTo(1);
    }

    @Test
    public void getUsersWithNullAttrs() throws RemoteException {
        List<Map<String, String>> users = gina.getUsers(DOMAIN_APPLICATION, ROLE, null);

        assertThat(users).isNotEmpty();
        LOGGER.info("users.size() = {}", users.size());
        LOGGER.debug("users = {}", users);

        long nbUsers = users
                .stream()
                .map(user -> user.get("uid"))
                .filter(Objects::nonNull)
                .filter(uid -> uid.contains(USER))
                .count();
        assertThat(nbUsers)
                .as("L'utilisateur " + USER + " devrait faire partie de la liste")
                .isEqualTo(1);
    }

    @Test
    public void isValidUser() throws RemoteException {
        // Utilisateur valide
        boolean result = gina.isValidUser(USER);
        assertThat(result).isTrue();

        // Utilisateur non valide
        result = gina.isValidUser("TAGADA");
        assertThat(result).isFalse();
    }

    @Test
    public void getUserAttrs() throws RemoteException {
        String[] requestedAttributes = TestConstants.TEST_ATTRS;
        Map<String, String> attributes = gina.getUserAttrs(USER, requestedAttributes);

        LOGGER.info("Attributs obtenus : {}", attributes.keySet());

        assertThat(attributes.size())
                .as("Le nombre attendu d'attributs n'a pas a ete obtenu")
                .isEqualTo(requestedAttributes.length + 1);    // l'appel rend l'attribut "dn" en plus

        checkAttribute(attributes, LdapAttribute.DN, "cn=LAROCHEP,ou=Users,ou=GESTREPO,ou=CTI,o=gina");
        checkAttribute(attributes, LdapAttribute.CN, "LAROCHEP");
        checkAttribute(attributes, LdapAttribute.GIVEN_NAME, "Pierre");
        checkAttribute(attributes, LdapAttribute.SN, "Laroche");
        checkAttribute(attributes, LdapAttribute.UID, "LAROCHEP");
    }

    @Test
    public void getUserAllAttrs() throws RemoteException {
        Map<String, String> attributes = gina.getUserAttrs(USER, null);

        LOGGER.info("Attributs obtenus : {}", attributes.keySet());

        // on ne teste pas le nombre d'attributs obtenus, car il differe selon que l'on utilise Gina ou UnboundID ;
        // dans le second cas, des attributs supplementaires sont presents (creatorsName, entryUUID, modifyTimestamp,
        // subschemaSubentry, etc.)
        /*
        assertThat(attributes.size())
                .as("Le nombre attendu d'attributs n'a pas a ete obtenu")
                .isEqualTo(17);
         */

        checkAttribute(attributes, LdapAttribute.DN, "cn=LAROCHEP,ou=Users,ou=GESTREPO,ou=CTI,o=gina");
        checkAttribute(attributes, LdapAttribute.DEPARTMENT_NUMBER, "UO5751");
        checkAttribute(attributes, LdapAttribute.DISPLAY_NAME, "Laroche Pierre (DI)");
        checkAttribute(attributes, LdapAttribute.GIVEN_NAME, "Pierre");
        checkAttribute(attributes, LdapAttribute.SN, "Laroche");
    }

    @Test
    public void hasRoleUser() throws RemoteException {
        String user = USER;
        String role = ROLE;
        boolean ret = gina.hasUserRole(user, role);
        LOGGER.warn("ldapConf = {}", ldapConf);
        assertThat(ret)
                .as("L'usager " + USER + " devrait avoir le role " + role)
                .isTrue();
    }

    @Test
    public void hasRoleUserForUnexistingUser() throws RemoteException {
        String user = USER;
        String role = "ROLE_BIDON_QUI_N_EXISTE_PAS_DANS_GINA";
        boolean ret = gina.hasUserRole(user, role);
        assertThat(ret)
                .as(user + " ne devrait avoir le role " + role)
                .isFalse();
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRole() throws RemoteException {
        String user = USER;
        String application = DOMAIN_APPLICATION;
        String role = ROLE;
        boolean ret = gina.hasUserRole(user, application, role);
        assertThat(ret).as(user + " devrait avoir le role " + role + " pour l'application " + application)
                       .isTrue();
    }

    @Test
    public void getAppRoles() throws RemoteException {
        List<String> roles = gina.getAppRoles(DOMAIN_APPLICATION);
        assertThat(roles).isNotNull();
        assertThat(roles.size()).isGreaterThan(0);
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains("ADMINISTRATEUR")).isTrue();
        assertThat(roles.contains("DEVELOPPEUR-EDG")).isTrue();

        // Test supplementaire (commente, car execution assez longue)
        /*
        for (String role : roles) {
            List<Map<String, String>> users = api.getUsers("GESTREPO", role, new String[] {"cn"});
            LOGGER.info("users for role {}: list of size {}", role, users.size());
        }
        */
    }

    @Test
    public void getBusinessRoles() throws RemoteException {
        List<String> roles = gina.getBusinessRoles(DOMAIN_APPLICATION);
        assertThat(roles).isNotNull();
        assertThat(roles).isEmpty();
    }

    // -----------------------------------------------------------------------------------------
    // METHODES NON IMPLEMENTEES
    // -----------------------------------------------------------------------------------------

    @Test
    public void hasRoleWithApplicationAndRole() throws RemoteException {
        expectNotImplemented(thrown);

        gina.hasRole(DOMAIN_APPLICATION, ROLE);
    }

    @Test
    public void getUserAttrsWithAttrs() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getUserAttrs(TestConstants.TEST_ATTRS);
    }

    @Test
    public void getRoles() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getRoles(DOMAIN_APPLICATION);
    }

    @Test
    public void getUser() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getUser();
    }

    @Test
    public void getLanguage() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getLanguage();
    }

    @Test
    public void getEnvironment() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getEnvironment();
    }

    @Test
    public void getIntegrationUserRoles() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getIntegrationUserRoles(DOMAIN, "ABC");
    }

    @Test
    public void getIntegrationUserAttributes() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getIntegrationUserAttributes(DOMAIN, DOMAIN_APPLICATION);
    }

    @Test
    public void getUsersByPhone() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getUsersByPhone("ABC", true, TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUsersBySIRHNumber() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getUsersBySIRHNumber("ABC", true, TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUsersByName() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getUsersByName("ABC", true, TestConstants.TEST_ATTRS);
    }

    @Test
    public void getInheritingRoles() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getInheritingRoles(DOMAIN_APPLICATION, ROLE);
    }

    @Test
    public void getPMProprieteMetier() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getPMProprieteMetier(DOMAIN_APPLICATION);
    }

    @Test
    public void getOwnIDUniqueForPPorPseudo() throws RemoteException, NamingException {
        expectNotImplemented(thrown);

        gina.getOwnIDUniqueForPPorPseudo();
    }

    @Test
    public void getOwnPMProprieteMetier() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getOwnPMProprieteMetier("");
    }

    @Test
    public void getPPProprieteMetier() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getPPProprieteMetier("");
    }

    @Test
    public void getOwnPPProprieteMetier() throws RemoteException {
        expectNotImplemented(thrown);

        gina.getOwnPPProprieteMetier("");
    }

    @Test
    public void sendMail() throws RemoteException {
        expectNotImplemented(thrown);

        String[] foo = { "" };
        gina.sendMail("", foo, foo, "", "", "");
    }

    private void expectNotImplemented(ExpectedException thrown) {
        thrown.expect(GinaException.class);
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapAccess.NOT_IMPLEMENTED));
    }

    private boolean rolesAreCleaned(final List<String> roles) {
        boolean result = true;
        for (String role : roles) {
            if (role.contains("cn=")) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Verifie que "attributes" contient une entree avec "name" pour nom d'attribut et "value" comme valeur
     * d'attribut.
     */
    private void checkAttribute(Map<String, String> attributes, LdapAttribute attr, String value) {
        String name = attr.value;
        Optional<Map.Entry<String, String>> entry = attributes.entrySet().stream()
                .filter(e -> name.equalsIgnoreCase(e.getKey()))
                .findAny();
        assertThat(entry)
                .as("L'attribut '" + name + "' n'a pas ete trouve dans le VLDAP")
                .isPresent()
                .hasValueSatisfying(e ->
                        assertThat(e.getValue())
                                .as("La valeur de l'attribut '" + name + "' est incorrecte")
                                .isEqualTo(value));
    }

}
