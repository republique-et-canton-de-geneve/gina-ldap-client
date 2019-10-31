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
package gina.api.utils;

import gina.impl.GinaException;
import gina.impl.GinaLdapAccess;
import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapAccess.NOT_IMPLEMENTED));
    }

    public static GinaLdapConfiguration getGinaLdapConfiguration(
            String server,
            String user,
            String password,
            String domain,
            String application) {

        return getGinaLdapConfiguration(
                server,
                user,
                password,
                domain,
                application,
                GinaLdapUtils.DEFAULT_LDAP_CONNECTION_TIMEOUT,
                GinaLdapUtils.DEFAULT_LDAP_READ_TIMEOUT);
    }

    public static GinaLdapConfiguration getGinaLdapConfiguration(
            String server,
            String user,
            String password,
            String domain,
            String application,
            int connexionTimeout,
            int readTimeout) {

        if (StringUtils.isBlank(password)) {
            LOGGER.info("le mot de passe au serveur LDAP (il est necessaire avec Gina, mais inutile avec UnboundID) est manquant");
        }

        GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(
                server, user, password, domain, application, connexionTimeout, readTimeout);
        LOGGER.info("Connexion LDAP : {}", ldapConf);

        return ldapConf;
    }

}
