package gina.api;

import java.rmi.RemoteException;
import java.util.List;

public interface GinaApiLdapBaseAble extends GinaApiBaseAble {

    /**
     * Donne tous les r�les de l'utilisateur pass� en param�tre pour l'application courante
     *
     * @see gina.api.GinaApiBaseAble#getUserRoles(String, String)
     */
    List<String> getUserRoles(String user) throws RemoteException;

    /**
     * Indique si l'utilisateur pass� en param�tre a le r�le pour l'application courante
     *
     * @see gina.api.GinaApiBaseAble#hasUserRole(String, String, String)
     */
    boolean hasUserRole(String user, String role) throws RemoteException;

}
