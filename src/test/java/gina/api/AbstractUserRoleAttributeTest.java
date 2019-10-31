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

import gina.api.utils.TestTools;
import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapUtils;

import static gina.api.utils.TestTools.getGinaLdapConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Donnees de connexion LDAP pour les tests.
 */
public abstract class AbstractUserRoleAttributeTest {

    // LDAP : domaine Gina
    public static final String DOMAIN = "CTI";

    // LDAP : application Gina
    public static final String APPLICATION = "GESTREPO";

    // LDAP : domaine Gina + application Gina
    public static final String DOMAIN_APPLICATION = DOMAIN + "." + APPLICATION;

    // LDAP : serveur
    public static final String SERVER = System.getProperty("test.gestrepo.server");

    // LDAP : user
    public static final String USER = System.getProperty("test.gestrepo.user");

    // LDAP : mot de passe
    public static final String PASSWORD = System.getProperty("test.gestrepo.password");

    protected static GinaLdapConfiguration getGinaLdapConfiguration() {
        return TestTools.getGinaLdapConfiguration(
            AbstractUserRoleAttributeTest.SERVER,
            AbstractUserRoleAttributeTest.USER,
            AbstractUserRoleAttributeTest.PASSWORD,
            AbstractUserRoleAttributeTest.DOMAIN,
            AbstractUserRoleAttributeTest.APPLICATION,
            GinaLdapUtils.DEFAULT_LDAP_CONNECTION_TIMEOUT,
            GinaLdapUtils.DEFAULT_LDAP_READ_TIMEOUT);
    }

    protected static GinaLdapConfiguration getGinaLdapConfiguration(int connectionTimeout, int readTimeout) {
        return TestTools.getGinaLdapConfiguration(
            AbstractUserRoleAttributeTest.SERVER,
            AbstractUserRoleAttributeTest.USER,
            AbstractUserRoleAttributeTest.PASSWORD,
            AbstractUserRoleAttributeTest.DOMAIN,
            AbstractUserRoleAttributeTest.APPLICATION,
            connectionTimeout,
            readTimeout);
    }

}
