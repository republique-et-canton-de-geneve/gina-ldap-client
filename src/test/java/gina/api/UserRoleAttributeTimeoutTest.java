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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.CommunicationException;
import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class UserRoleAttributeTimeoutTest extends AbstractUserRoleAttributeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleAttributeTimeoutTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Trace de debut d'execution et de fin d'execution de chaque methode de test.
     */
    @Rule
    public TestWatcher watcher = new TestLoggingWatcher();

    @Test
    public void de_bons_timeouts_doivent_assurer_une_bonne_lecture() throws IOException {
        int connectionTimeout = 3000;
        int readTimeout = 4000;
        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(connectionTimeout, readTimeout);

        try (GinaApiLdapBaseAble gina = new GinaLdapAccess(ldapConf)) {
          assertThat(gina).isNotNull();
        }
    }

    @Test
    @Ignore("pas fiable : l'exception lancee varie")
    public void un_connection_timeout_trop_court_doit_faire_planter_la_connexion() throws IOException {
        int connectionTimeout = 1;
        int readTimeout = 4000;
        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(connectionTimeout, readTimeout);
        try (GinaApiLdapBaseAble gina = new GinaLdapAccess(ldapConf)) {
            LOGGER.info("Une NamingException est attendue dans la ligne suivante");
            Throwable thrown = catchThrowable(() -> gina.isValidUser(TestConstants.GENERIC_USERNAME));
            LOGGER.info("cause : " + thrown.getCause());
            assertThat(thrown)
                    .isInstanceOf(GinaException.class)
                    .hasCauseInstanceOf(CommunicationException.class)
                    .hasRootCauseExactlyInstanceOf(SocketTimeoutException.class);
            LOGGER.info("Test OK");
        }
    }

    @Test
    @Ignore("pas fiable : n'emet pas toujours d'exception")
    public void un_read_timeout_trop_court_doit_faire_planter_la_lecture() throws IOException {
        int connectionTimeout = 5000;
        int readTimeout = 1;
        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(connectionTimeout, readTimeout);
        try (GinaApiLdapBaseAble gina = new GinaLdapAccess(ldapConf)) {
            LOGGER.info("Une NamingException est attendue dans la ligne suivante");
            Throwable thrown = catchThrowable(() -> {
                boolean test = gina.isValidUser(TestConstants.GENERIC_USERNAME);
                // la ligne suivante ne doit pas s'afficher, car la ligne precedente doit avoir leve une exception
                LOGGER.info("Utilisateur valide : {}", test);
            });
            assertThat(thrown)
                    .as("L'exception attendue est absente")
                    .isNotNull()
                    .isInstanceOf(GinaException.class)
                    .hasMessage("LDAP response read timed out, timeout used:1ms.");
            LOGGER.info("Test OK");
        }
    }

}
