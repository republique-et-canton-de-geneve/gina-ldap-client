package gina.api;

import static gina.impl.util.GinaLdapConfiguration.Type.APPLICATION;

import gina.api.gina.api.utils.TestConstants;
import gina.impl.GinaLdapFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.ParallelRunner;

import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ParallelRunner.class)
public class GinaLdapApplicationParallelTest extends GinaLdapApplicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapApplicationParallelTest.class);

    @BeforeClass
    public static void initApi() {
        LOGGER.info(TestConstants.START_METHOD);

        System.setProperty("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
        System.setProperty("com.sun.jndi.ldap.connect.pool.authentication", "simple");
        System.setProperty("com.sun.jndi.ldap.connect.pool.maxsize", "5");
        System.setProperty("com.sun.jndi.ldap.connect.pool.prefsize", "3");
        System.setProperty("com.sun.jndi.ldap.connect.pool.timeout", "240000");
        System.setProperty("com.sun.jndi.ldap.connect.pool.initsize", "3");
        System.setProperty("com.sun.jndi.ldap.connect.pool.debug", "fine");

        String base = "ou=OAC,o=gina";

        String server = System.getProperty("test.application.server");
        String user = System.getProperty("test.application.user");
        String password = System.getProperty("test.application.password");

        LOGGER.info("Connexion LDAP : server=[{}], user=[{}]", server, user);
        if (StringUtils.isBlank(password)) {
            LOGGER.info("le mot de passe au serveur LDAP (necessaire avec Gina, inutile avec UnboundID) est manquant");
        }

        int timeout = GinaLdapUtils.LDAP_DEFAULT_TIMEOUT;

        GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(server, base, user, password, APPLICATION, timeout);
        api = GinaLdapFactory.getInstance(ldapConf);

        LOGGER.info(TestConstants.END_METHOD);
    }

}
