package gina.impl.attribute;

import java.util.HashMap;
import java.util.Map;

/**
 * Donne la correspondance entre les noms d'attributs Gina et les noms d'attributs VLDAP, dans les deux sens.
 * Exemple : "firstname" (Gina) <-> "givenName" (VLDAP).
 */
public class AttributeMapper {

    private static final Map<String,String> LDAP_TO_GINA = new HashMap<>();

    private static final Map<String,String> GINA_TO_LDAP = new HashMap<>();

    static {
        map(GinaAttribute.DISABLED, LdapAttribute.LOGIN_DISABLED);
        map(GinaAttribute.EMAIL, LdapAttribute.MAIL);
        map(GinaAttribute.FIRSTNAME, LdapAttribute.GIVEN_NAME);
        map(GinaAttribute.FULLNAME, LdapAttribute.DISPLAY_NAME);
        map(GinaAttribute.HRID, LdapAttribute.EMPLOYEE_NUMBER);
        map(GinaAttribute.INITIALS, LdapAttribute.INITIALS);
        map(GinaAttribute.LANGUAGE, LdapAttribute.LANGUAGE);
        map(GinaAttribute.MOBILE, LdapAttribute.MOBILE);
        map(GinaAttribute.NAME, LdapAttribute.SN);
        map(GinaAttribute.PHONE, LdapAttribute.TELEPHONE_NUMBER);
        map(GinaAttribute.SERVICE, LdapAttribute.DEPARTMENT_NUMBER);
        map(GinaAttribute.TITLE, LdapAttribute.TITLE);
        map(GinaAttribute.USER, LdapAttribute.CN);
    }

    private AttributeMapper() {
    }

    private static void map(GinaAttribute gina, LdapAttribute ldap) {
        GINA_TO_LDAP.put(gina.value, ldap.value);
        LDAP_TO_GINA.put(ldap.value, gina.value);
    }

    /**
     * Rend le nom d'attribut Gina correspondant a l'attribut VLDAP donne.
     * @return un nom d'attribut Gina, ou null
     */
    public static String ldapToGina(String ldapAttributeName) {
        return LDAP_TO_GINA.get(ldapAttributeName);
    }

    /**
     * Rend le nom d'attribut VLDAP correspondant a l'attribut Gina donne.
     * @return un nom d'attribut VLDAP, ou null
     */
    public static String ginaToLdap(String ginaAttributeName) {
        return GINA_TO_LDAP.get(ginaAttributeName);
    }

}
