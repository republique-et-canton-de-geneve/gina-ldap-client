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
package gina.impl.attribute;

import gina.api.GinaApiBaseAble;

/**
 * Noms des attributs des utilisateurs dans Gina.
 * Un attribut dans Gina (par exemple firstname) n'a pas forcement le meme nom dans le VLDAP (par exemple givenName).
 * <p/>
 * Les attributs Gina n'ont pas d'interet pour le developpeur, ils ne servent qu'a la cuisine interne de cette
 * bibliotheque, pour transcrire en termes Gina les appels au VLDAP.
 * Les appels aux methodes de {@link GinaApiBaseAble} prennent en parametres et rendent en resultat uniquement des
 * attributs VLDAP.
 */
public enum GinaAttribute {

    /** Indicateur de desactivation. */
    DISABLED("disabled"),

    /** Nom unique de l'utilisateur. */
    DN("dn"),

    /** Adresse electronique. */
    EMAIL("email"),

    /** Prenom. */
    FIRSTNAME("firstname"),

    /** Nom complet. */
    FULLNAME("fullname"),

    /** Matricule SIRH. */
    HRID("hrid"),

    /** Initiales. */
    INITIALS("initials"),

    /** Langue. */
    LANGUAGE("lang"),

    /** Numero de telephone mobile. */
    MOBILE("mobile"),

    /** Nom de famille. */
    NAME("name"),

    /** OU (Organisation Unit) de l'utilisateur (Admin/Police/Justice). */
    OU("ou"),

    /** Numero de telephone. */
    PHONE("phone"),

    /** Service. */
    SERVICE("service"),

    /** Titre. */
    TITLE("title"),

    /** Nom d'utilisateur (login). */
    USER("user");

    public final String value;

    GinaAttribute(String value) {
        this.value = value;
    }

}
