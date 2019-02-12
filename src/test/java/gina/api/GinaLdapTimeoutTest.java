package gina.api;

import gina.api.gina.api.utils.TestConstants;
import gina.api.gina.api.utils.TestLoggingWatcher;
import gina.impl.GinaException;
import gina.impl.GinaLdapFactory;
import gina.impl.util.GinaLdapConfiguration;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.CommunicationException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;

import static gina.api.gina.api.utils.TestTools.getGinaLdapConfiguration;
import static gina.impl.util.GinaLdapConfiguration.Type.DOMAIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class GinaLdapTimeoutTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapTimeoutTest.class);

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

    private static String base;
    private static String server;
    private static String user;
    private static String password;

    @BeforeClass
    public static void initApi() {
        base = "ou=CSBUGTRACK,o=gina";
        server = System.getProperty("test.domain.server");
        user = System.getProperty("test.domain.user");
        password = System.getProperty("test.domain.password");
    }

    @Test
    public void de_bons_timeouts_doivent_assurer_une_bonne_lecture() throws RemoteException {
        int connectionTimeout = 3000;
        int readTimeout = 4000;
        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(server, base, user, password, DOMAIN, connectionTimeout, readTimeout);
        api = GinaLdapFactory.getInstance(ldapConf);

        assertThat(api).isNotNull();
    }

    @Test
    @Ignore  // pas fiable : l'exception lancee varie
    public void connection_timeout_trop_court_doit_faire_planter_la_connexion() throws RemoteException {
        int connectionTimeout = 1;
        int readTimeout = 4000;
        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(server, base, user, password, DOMAIN, connectionTimeout, readTimeout);
        LOGGER.info("Une pile d'appel est attendue dans la ligne suivante");
        api = GinaLdapFactory.getInstance(ldapConf);

        Throwable thrown = catchThrowable(() -> api.isValidUser(TestConstants.GENERIC_USERNAME));
        LOGGER.info("cause : " + thrown.getCause());
        assertThat(thrown)
                .isInstanceOf(GinaException.class)
                .hasCauseInstanceOf(CommunicationException.class)
                .hasRootCauseExactlyInstanceOf(SocketTimeoutException.class);
        LOGGER.info("Test OK");
    }

    @Test
    public void read_timeout_trop_court_doit_faire_planter_la_lecture() throws RemoteException {
        int connexionTimeout = 5000;
        int readTimeout = 1;
        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration(server, base, user, password, DOMAIN, connexionTimeout, readTimeout);
        LOGGER.info("Une pile d'appel est attendue dans la ligne suivante");
        api = GinaLdapFactory.getInstance(ldapConf);

        Throwable thrown = catchThrowable(() -> api.isValidUser(TestConstants.GENERIC_USERNAME));
        assertThat(thrown)
                .isInstanceOf(GinaException.class)
                .hasMessage("LDAP response read timed out, timeout used:1ms.");
        LOGGER.info("Test OK");
    }

}
