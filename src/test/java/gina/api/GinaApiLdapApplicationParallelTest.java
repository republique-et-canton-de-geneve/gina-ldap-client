package gina.api;

import gina.api.gina.api.utils.TestConstants;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.ParallelRunner;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ParallelRunner.class)
public class GinaApiLdapApplicationParallelTest extends GinaApiLdapApplicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaApiLdapApplicationParallelTest.class);

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

        String server = "ldaps://vldap-dev.ceti.etat-ge.ch:636";
        String user = "cn=TCNVLDAP9523DEVAAG,ou=Users,ou=CAMAC-GENEVE,ou=OAC,o=gina";
        String password = "Uddyzfsp4";

        // String server = "ldap://127.0.0.1:30636";
        // String user = "";
        // String password = "";

        int timeout = GinaApiLdapUtils.LDAP_DEFAULT_TIMEOUT;

        GinaApiLdapConfiguration ldapConf = new GinaApiLdapConfiguration(server, base, user, password, timeout);
        api = GinaApiLdapBaseFactory.getInstance(ldapConf);

        LOGGER.info(TestConstants.END_METHOD);
    }

}
