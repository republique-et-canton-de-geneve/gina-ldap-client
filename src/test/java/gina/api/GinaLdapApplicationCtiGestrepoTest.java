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
import gina.impl.GinaException;
import gina.impl.GinaLdapCommon;
import gina.impl.GinaLdapFactory;
import gina.impl.attribute.GinaAttribute;
import gina.impl.attribute.LdapAttribute;
import gina.impl.util.GinaLdapConfiguration;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static gina.api.utils.TestTools.getGinaLdapConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

public class GinaLdapApplicationCtiGestrepoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapApplicationCtiGestrepoTest.class);

    // LDAP : domaine Gina
    private static final String DOMAIN = "CTI";

    // LDAP : application Gina
    private static final String APPLICATION = "GESTREPO";

    // LDAP : domaine Gina + application Gina
    private static final String DOMAIN_APPLICATION = DOMAIN + "." + APPLICATION;

    // LDAP : utilisateur de test
    private static final String USER = "LAROCHEP";

    // LDAP : role de test
    private static final String ROLE = "UTILISATEUR";

    private static GinaApiLdapBaseAble api;

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
        String server = System.getProperty("test.gestrepo.server");
        String user = System.getProperty("test.gestrepo.user");
        String password = System.getProperty("test.gestrepo.password");

        ldapConf = getGinaLdapConfiguration(server, user, password, DOMAIN, APPLICATION);
        api = GinaLdapFactory.getNewInstance(ldapConf);
    }

    @AfterClass
    public static void releaseResources() {
        IOUtils.closeQuietly(api);
    }

    @Test
    public void getAllUsersTest() throws RemoteException {
        thrown.expect(GinaException.class);
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapCommon.NOT_IMPLEMENTED));

         api.getAllUsers("FILTER", TestConstants.TEST_ATTRS);
    }

    @Test
    public void getUserRolesTest() throws RemoteException {
        List<String> roles = api.getUserRoles(USER);

        assertThat(roles).isNotNull();
        assertThat(roles.size()).isGreaterThan(0);
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains(ROLE)).isTrue();
        assertThat(roles.contains("DEVELOPPEUR-CTI")).isTrue();
    }

    @Test
    public void isValidUserTest() throws RemoteException {
        // Utilisateur valide
        boolean result = api.isValidUser(USER);
        assertThat(result).isTrue();

        // Utilisateur non valide
        result = api.isValidUser("TAGADA");
        assertThat(result).isFalse();
    }

    @Test
    public void getUserAttrsTest() throws RemoteException {
        String[] attrs = TestConstants.TEST_ATTRS;
        Map<String, String> attributes = api.getUserAttrs(USER, attrs);

        LOGGER.info("Attributs obtenus : {}", attributes.keySet());

        assertThat(attributes.size())
                .as("Le nombre attendu d'attributs n'a pas a ete obtenu")
                .isEqualTo(attrs.length + 1);    // l'appel rend l'attribut "dn" en plus

        String attribute = GinaAttribute.FIRSTNAME.value;
        Optional<Map.Entry<String, String>> entry = attributes.entrySet()
                .stream()
                .filter(e -> attribute.equalsIgnoreCase(e.getKey()))
                .findAny();
        assertThat(entry)
                .as("L'attribut '" + attribute + "' est manquant")
                .isPresent()
                .hasValueSatisfying(e ->
                        assertThat(e.getValue())
                                .as("L'attribut '" + attribute + "' obtenu est incorrect")
                                .isEqualTo("Pierre"));
    }

    @Test
    public void hasRoleUserTest() throws RemoteException {
        String user = USER;
        String role = ROLE;
        boolean ret = api.hasUserRole(user, role);
        assertThat(ret)
                .as("L'usager " + USER + " devrait avoir le role " + role + ". Configuration = " + ldapConf.toString())
                .isTrue();
    }

    @Test
    public void hasRoleUserTest2() throws RemoteException {
        String user = USER;
        String role = "ROLE_BIDON_QUI_N_EXISTE_PAS_DANS_GINA";
        boolean ret = api.hasUserRole(user, role);
        assertThat(ret)
                .as(user + " ne devrait avoir le role " + role + ". Configuration = " + ldapConf.toString())
                .isFalse();
    }

    @Test
    public void hasUserRoleWithUserAndApplicationAndRoleTest() throws RemoteException {
        String user = USER;
        String application = DOMAIN_APPLICATION;
        String role = ROLE;
        boolean ret = api.hasUserRole(user, application, role);
        assertThat(ret).as(user + " devrait avoir le role " + role + " pour l'application "
                           + application + ". Configuration = " + ldapConf.toString())
                       .isTrue();
    }

    @Test
    public void getAppRolesTest() throws RemoteException {
//        List<String> roles = api.getAppRoles(APPLICATION);
        List<String> roles = api.getAppRoles(DOMAIN_APPLICATION);
        assertThat(roles).isNotNull();
        assertThat(roles.size()).isGreaterThan(0);
        LOGGER.info("roles.size() = {}", roles.size());
        LOGGER.info("roles = {}", roles);
        assertThat(TestTools.rolesAreCleaned(roles)).isTrue();
        assertThat(roles.contains("ADMINISTRATEUR")).isTrue();
        assertThat(roles.contains("SVN-READONLY-ALL")).isTrue();

        // Test supplementaire (commente, car execution assez longue)
        /*
        for (String role : roles) {
            List<Map<String, String>> users = api.getUsers("GESTREPO", role, new String[] {"cn"});
            LOGGER.info("users for role {}: list of size {}", role, users.size());
        }
        */
    }

}
