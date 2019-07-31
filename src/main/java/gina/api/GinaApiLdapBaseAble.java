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

import java.io.Closeable;
import java.rmi.RemoteException;
import java.util.List;

public interface GinaApiLdapBaseAble extends GinaApiBaseAble, Closeable {

    /**
     * Donne tous les roles de l'utilisateur donne' pour l'application courante.
     *
     * @see gina.api.GinaApiBaseAble#getUserRoles(String, String)
     */
    List<String> getUserRoles(String user) throws RemoteException;

    /**
     * Indique si l'utilisateur donne' a le role donne' pour l'application courante.
     *
     * @see gina.api.GinaApiBaseAble#hasUserRole(String, String, String)
     */
    boolean hasUserRole(String user, String role) throws RemoteException;

}
