package gina.api;

import java.rmi.RemoteException;
import java.util.List;

public interface GinaApiLdapBaseAble extends GinaApiBaseAble {

    /**
     * Donne tous les rôles de l'utilisateur courant pour l'application courante
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    public List<String> getRoles() throws GinaException, RemoteException;

    /**
     * Donne tous les rôles de l'utilisateur passé en paramètre pour l'application courante
     * 
     * @see gina.api.GinaApiBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    public List<String> getUserRoles(String user) throws GinaException, RemoteException;

    public boolean hasRole(String role) throws GinaException, RemoteException;

    public boolean hasUserRole(String user, String role) throws GinaException, RemoteException;

}