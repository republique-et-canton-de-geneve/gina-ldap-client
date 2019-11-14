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
package gina.impl.util;

import org.apache.commons.lang3.StringUtils;

import gina.impl.GinaException;

/**
 * Helper class to encode and decode ldap names and values.
 * <p>
 * https://github.com/spring-projects/spring-security/blob/a82cab7afdb1fc58830b1c415f1874d36b2c6c92/ldap/src/main
 * /java/org/springframework/security/ldap/LdapEncoder.java
 *
 * <p>
 * NOTE: This is a copy from Spring LDAP so that both Spring LDAP 1.x and 2.x can be supported without reflection.
 * </p>
 *
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 */
public final class GinaLdapEncoder {

    private static final String[] NAME_ESCAPE_TABLE = new String[96];

    private static final String[] FILTER_ESCAPE_TABLE = new String['\\' + 1];

    // Message d'erreur pour les paramètres non valides
    private static final String PARAM_NOT_VALID = "Paramètre non valide (vide, null, ...) pour cette méthode";

    static {

        // Name encoding table -------------------------------------

        // all below 0x20 (control chars)
        for (char c = 0; c < ' '; c++) {
            NAME_ESCAPE_TABLE[c] = "\\" + toTwoCharHex(c);
        }

        NAME_ESCAPE_TABLE['#'] = "\\#";
        NAME_ESCAPE_TABLE[','] = "\\,";
        NAME_ESCAPE_TABLE[';'] = "\\;";
        NAME_ESCAPE_TABLE['='] = "\\=";
        NAME_ESCAPE_TABLE['+'] = "\\+";
        NAME_ESCAPE_TABLE['<'] = "\\<";
        NAME_ESCAPE_TABLE['>'] = "\\>";
        NAME_ESCAPE_TABLE['\"'] = "\\\"";
        NAME_ESCAPE_TABLE['\\'] = "\\\\";

        // Filter encoding table -------------------------------------

        // fill with char itself
        for (char c = 0; c < FILTER_ESCAPE_TABLE.length; c++) {
            FILTER_ESCAPE_TABLE[c] = String.valueOf(c);
        }

        // escapes (RFC2254)
        FILTER_ESCAPE_TABLE['*'] = "\\2a";
        FILTER_ESCAPE_TABLE['('] = "\\28";
        FILTER_ESCAPE_TABLE[')'] = "\\29";
        FILTER_ESCAPE_TABLE['\\'] = "\\5c";
        FILTER_ESCAPE_TABLE[0] = "\\00";

    }

    /**
     * All static methods - not to be instantiated.
     */
    private GinaLdapEncoder() {
    }

    private static String toTwoCharHex(char c) {

        String raw = Integer.toHexString(c).toUpperCase();

        if (raw.length() > 1) {
            return raw;
        } else {
            return "0" + raw;
        }
    }

    /**
     * Escape a value for use in a filter.
     *
     * @param value the value to escape.
     *
     * @return a properly escaped representation of the supplied value.
     */
    public static String filterEncode(String value) {
        final String trimedValue = StringUtils.trim(value);

        if (StringUtils.isEmpty(trimedValue)) {
            throw new GinaException(PARAM_NOT_VALID);
        }

        // make buffer roomy
        StringBuilder encodedValue = new StringBuilder(trimedValue.length() * 2);
        int length = trimedValue.length();
        for (int i = 0; i < length; i++) {
            char c = trimedValue.charAt(i);
            if (c < FILTER_ESCAPE_TABLE.length) {
                encodedValue.append(FILTER_ESCAPE_TABLE[c]);
            } else {
                // default: add the char
                encodedValue.append(c);
            }
        }

        return encodedValue.toString();
    }

}
