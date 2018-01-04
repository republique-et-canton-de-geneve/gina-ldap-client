package gina.api;

import java.rmi.RemoteException;
import java.util.List;

public interface GinaApiLdapBaseAbleApplication {

    /**
     * Donne tous les rôles de l'utilisateur courant pour l'application courante
     * 
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    public List<String> getRoles() throws RemoteException;

    /**
     * Donne tous les rôles de l'utilisateur passé en paramètre pour l'application courante
     * 
     * @see gina.api.GinaApiBaseAble#getUserRoles(java.lang.String,
     * java.lang.String)
     */
    public List<String> getUserRoles(String user) throws RemoteException;

}
