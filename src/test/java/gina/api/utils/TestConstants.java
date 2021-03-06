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
package gina.api.utils;

import gina.api.LdapAttribute;

public class TestConstants {

    /**
     * Utilisateur PINAUDJ.
     */
    public static final String PINAUDJ_USERNAME = "PINAUDJ";

    public static final String GENERIC_USERNAME = "TCNLDAPAOCAAG";

    /**
     * Utilisateur DRIVONOL.
     */
    public static final String DRIVONOL_USERNAME = "DRIVONOL";

    /**
     * Proprietes de l'utilisateur.
     */
    public static String[] TEST_ATTRS = {
            LdapAttribute.CN.value,
            LdapAttribute.GIVEN_NAME.value,
            LdapAttribute.SN.value,
            LdapAttribute.UID.value,
    };

    // Chaine indiquant le d�but d'un test, pour les traces
    public static final String START_METHOD = "START";

    // Chaine indiquant la fin d'un test, pour les traces
    public static final String END_METHOD = "END";

}
