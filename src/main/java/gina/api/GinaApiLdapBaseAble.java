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
