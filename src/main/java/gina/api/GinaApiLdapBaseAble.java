package gina.api;

import java.rmi.RemoteException;
import java.util.List;

public interface GinaApiLdapBaseAble extends GinaApiBaseAble {

    /**
     * Donne tous les r�les de l'utilisateur courant pour l'application courante
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    public List<String> getRoles() throws RemoteException;

    /**
     * Donne tous les r�les de l'utilisateur pass� en param�tre pour l'application courante
     * 
     * @see gina.api.GinaApiBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    public List<String> getUserRoles(String user) throws RemoteException;

    public boolean hasRole(String role) throws RemoteException;

    public boolean hasUserRole(String user, String role) throws RemoteException;

}