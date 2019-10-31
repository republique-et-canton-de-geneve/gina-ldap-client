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

import gina.impl.util.GinaLdapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTools.class);

    public static GinaLdapConfiguration getGinaLdapConfiguration(
            String server,
            String user,
            String password,
            String domain,
            String application,
            int connexionTimeout,
            int readTimeout) {

        if (StringUtils.isBlank(password)) {
            LOGGER.info("le mot de passe au serveur LDAP (il est necessaire avec Gina et inutile avec UnboundID) est manquant");
        }

        GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(
                server, user, password, domain, application, connexionTimeout, readTimeout);
        LOGGER.info("Connexion LDAP : {}", ldapConf);

        return ldapConf;
    }

}
