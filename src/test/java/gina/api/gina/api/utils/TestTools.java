package gina.api.gina.api.utils;

import static gina.impl.util.GinaLdapConfiguration.Type.APPLICATION;

import gina.impl.GinaLdapCommon;
import gina.impl.GinaException;
import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapUtils;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTools.class);

    public static boolean rolesAreCleaned(final List<String> roles) {
        boolean result = true;
        for (String role : roles) {
            if (role.contains("cn=")) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static void expectNotImplemented(ExpectedException thrown) {
        thrown.expect(GinaException.class);
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapCommon.NOT_IMPLEMENTED));
    }

    public static GinaLdapConfiguration getGinaLdapConfiguration(
            String server,
            String base,
            String user,
            String password,
            GinaLdapConfiguration.Type type) {

        return getGinaLdapConfiguration(
                server,
                base,
                user,
                password,
                type,
                GinaLdapUtils.DEFAULT_LDAP_CONNECTION_TIMEOUT,
                GinaLdapUtils.DEFAULT_LDAP_READ_TIMEOUT);
    }

    public static GinaLdapConfiguration getGinaLdapConfiguration(
            String server,
            String base,
            String user,
            String password,
            GinaLdapConfiguration.Type type,
            int connexionTimeout,
            int readTimeout) {

        if (StringUtils.isBlank(password)) {
            LOGGER.info("le mot de passe au serveur LDAP (qui est necessaire avec Gina, mais inutile avec UnboundID) est manquant");
        }

        GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(
                server, base, user, password, type, connexionTimeout, readTimeout);
        LOGGER.info("Connexion LDAP : {}", ldapConf);

        return ldapConf;
    }

}
