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

import gina.api.LdapAttribute;
import gina.impl.GinaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GinaLdapUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapUtils.class);

    // LDAP Attributes
    public static final String ATTRIBUTE_MEMBEROF = "memberOf";

    public static final String ATTRIBUTE_MEMBER = "member";

    public static final String ATTRIBUTE_CN = "cn";

    public static final String ATTRIBUTE_OU = "ou";

//    public static final String ATTRIBUTE_DEPARTMENT_NUMBER = "departmentNumber";

    /**
     * Temps maximal en millisecondes pour etablir la connexion au serveur LDAP.
     */
    public static final int DEFAULT_LDAP_CONNECTION_TIMEOUT = 5000;

    /**
     * Temps maximal en millisecondes pour une requete au serveur LDAP.
     */
    public static final int DEFAULT_LDAP_READ_TIMEOUT = 3000;

    /**
     * Depuis janv. 2019, utiliser {@link #DEFAULT_LDAP_READ_TIMEOUT}.
     */
    @Deprecated
    public static final int LDAP_DEFAULT_TIMEOUT = DEFAULT_LDAP_READ_TIMEOUT;

    private GinaLdapUtils() {
    }

    public static String extractDomain(final String domaineApplication) {
        String result = null;
        if (StringUtils.isNotBlank(domaineApplication) && domaineApplication.contains(".")) {
            final String[] splitApplication = StringUtils.split(domaineApplication, ".", 2);
            result = splitApplication[0];
        }
        return result;
    }

    public static String extractApplication(final String domaineApplication) {
        LOGGER.debug("domaineApplication = {}", domaineApplication);
        String result = null;
        if (StringUtils.isNotBlank(domaineApplication) && domaineApplication.contains(".")) {
            final String[] splitApplication = StringUtils.split(domaineApplication, ".", 2);
            result = splitApplication[1];
        } else {
            result = domaineApplication;
        }
        LOGGER.debug("result = {}", result);
        return result;
    }

    public static String createPropertie(final List<String> list) {
        StringBuilder builder = new StringBuilder();
        int size = 0;
        for (String s : list) {
            size++;
            builder.append(s);
            if (size < list.size()) {
                builder.append(',');
            }
        }
        return builder.toString();
    }

    public static String getLdapFilterOu(String ou) {
        return "ou=" + ou;
    }

    public static String getLdapFilterGroup() {
        return "(objectClass=groupOfNames)";
    }

/*
    public static String getLdapFilterGroup(String role) {
        return "ou=Groups,ou=" + role;
    }
 */

    public static String getLdapFilterUser() {
        return "(objectClass=person)";
    }
    public static String getLdapFilterUser(String user) {
        return "(&(objectClass=person)(cn=" + user + "))";
    }

    public static String getLdapFilterCn(String cn) {
        return "(&(cn=" + cn + "))";
    }

    public static String roleDnToString(String dn) {
        DnPart[] parts = DnPart.parse(dn);
        if (parts.length >= 4
                && parts[0].getAttr().equalsIgnoreCase(ATTRIBUTE_CN)
                && parts[1].getAttr().equalsIgnoreCase(ATTRIBUTE_OU)
                && parts[1].getValue().equalsIgnoreCase("Groups")
                && parts[2].getAttr().equalsIgnoreCase(ATTRIBUTE_OU)
                && parts[3].getAttr().equalsIgnoreCase(ATTRIBUTE_OU)) {
            /*
            return parts[3].getValue()
                    + "." + parts[2].getValue()
                    + "." + parts[0].getValue();
             */
            return parts[0].getValue();
        }
        return null;
    }

    /**
     * Traduit une liste d'attributs, du format javax.naming.directory vers un format Map.
     * @param names noms des attributs a traduire. Si null, tous les attributs sont traduits
     */
    public static Map<String,String> attributesToUser(String dn, Attributes attrs, String... names) {
        Map<String,String> result = new HashMap<>();

        result.put(LdapAttribute.DN.value, dn);

        NamingEnumeration<? extends Attribute> attributesEnum = attrs.getAll();
        while (attributesEnum.hasMoreElements()) {
            Attribute attribute = attributesEnum.nextElement();
            String name = attribute.getID();
            if (names == null || Arrays.asList(names).contains(name)) {
                result.put(name, firstValue(attribute));
            }
        }

        return result;
    }

    public static String firstValue(Attribute attr) {
        try {
            if (/*attr == null ||*/ attr.size() == 0) {
                return null;
            }
            return attr.get(0).toString();
        } catch (NamingException e) {
            return handle(e);
        }
    }

    public static String firstValue(Attributes attrs, String name) {
        return firstValue(attrs.get(name));
    }

    public static List<String> allValues(Attribute attr) {
        try {
            List<String> result = new ArrayList<String>();
            for (int i = 0; i < attr.size(); ++i) {
                result.add((String)attr.get(i));
            }
            return result;
        } catch (NamingException e) {
            return handle(e);
        }
    }

    public static List<String> allValues(Attributes attrs, String name) {
        return allValues(attrs.get(name));
    }

    public static String ldapFilterEquals(String attr, String value) {
        return "(" + attr + "=" + GinaLdapEncoder.filterEncode(value) + ")";
    }

    public static String ldapFilterAnd(String... subfilters) {
        return ldapFilterOp('&', subfilters);
    }

    private static String ldapFilterOp(char op, String... subfilters) {
        if (subfilters == null || subfilters.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append('(');
        buf.append(op);
        for (String sub: subfilters) {
            buf.append(sub);
        }
        buf.append(')');
        return buf.toString();
    }

    public static <T> T handle(NamingException e) {
        LOGGER.error("Erreur : ", e);
        throw new GinaException(e.getMessage());
    }

    public static void closeQuietly(NamingEnumeration<?> obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (NamingException e) {
                LOGGER.error("Erreur lors de la fermeture d'une NamingEnumeration : ", e);
            }
        }
    }

}
