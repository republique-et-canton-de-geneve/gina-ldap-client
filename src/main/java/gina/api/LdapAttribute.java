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

/**
 * Noms des attributs des utilisateurs dans le VLDAP.
 *
 * Un attribut dans Gina (par exemple firstname) n'a pas forcement le meme nom dans le VLDAP (par exemple givenName).
 */
public enum LdapAttribute {

    /** Nom d'utilisateur, par ex. "LAROCHEP". */
    CN("cn"),

    /** Service, par ex. "UO5751". */
    DEPARTMENT_NUMBER("departmentNumber"),

    /** Nom complet, par ex. "Laroche Pierre (DI)". */
    DISPLAY_NAME("displayName"),

    /** Nom unique de l'utilisateur, par ex. "cn=LAROCHEP,ou=Users,ou=GESTREPO,ou=CTI,o=gina". */
    DN("dn"),

    /** Numero d'employe, par ex. "40109194". */
    EMPLOYEE_NUMBER("employeeNumber"),

    /** Type d'employe, par ex. "internal-ge". */
    EMPLOYEE_TYPE("employeeType"),

    /** Prenom. */
    GIVEN_NAME("givenName"),

    /** Initiales, par ex. "PL". */
    INITIALS("initials"),

    /** Langue, par ex. "FR". */
    LANGUAGE("lang"),

    /** Indicateur de desactivation, par ex. "FALSE". */
    LOGIN_DISABLED("loginDisabled"),

    /** Adresse electronique. */
    MAIL("mail"),

    /** Numero de telephone mobile. */
    MOBILE("mobile"),

    /** Nom de famille. */
    SN("sn"),

    /** Numero de telephone. */
    TELEPHONE_NUMBER("telephoneNumber"),

    /** Titre, par ex. "Gestionnaire de service". */
    TITLE("title"),

    /** Identifiant, par ex. "LAROCHEP". */
    UID("uid");

    public final String value;

    LdapAttribute(String value) {
        this.value = value;
    }

}
