package gina.api;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

public abstract interface GinaApiLdapBaseAble
{
  
  public abstract String getUser()
    throws GinaException, RemoteException;

  public abstract boolean isValidUser(String paramString)
    throws GinaException, RemoteException;

  public abstract List<Map<String, String>> getAllUsers(String paramString, String[] paramArrayOfString)
    throws GinaException, RemoteException;

  public abstract Map<String, String> getUserAttrs(String paramString, String[] paramArrayOfString)
    throws GinaException, RemoteException;

  public abstract Map<String, String> getUserAttrs(String[] paramArrayOfString)
    throws GinaException, RemoteException;

  public abstract String getLanguage()
    throws GinaException, RemoteException;

  public abstract String getEnvironment()
    throws GinaException, RemoteException;

  public abstract boolean hasRole(String paramString1, String paramString2)
    throws GinaException, RemoteException;

  public abstract boolean hasUserRole(String paramString1, String paramString2, String paramString3)
    throws GinaException, RemoteException;

  public abstract List<String> getRoles(String paramString)
    throws GinaException, RemoteException;

  public abstract List<String> getUserRoles(String paramString1, String paramString2)
    throws GinaException, RemoteException;

  public abstract List<String> getIntegrationUserRoles(String paramString1, String paramString2)
    throws GinaException, RemoteException;

  public abstract List<String> getIntegrationUserAttributes(String paramString1, String paramString2)
    throws GinaException, RemoteException;

  public abstract List<String> getAppRoles(String paramString)
    throws GinaException, RemoteException;

  public abstract List<String> getBusinessRoles(String paramString)
    throws GinaException, RemoteException;

  public abstract List<Map<String, String>> getUsers(String paramString, String[] paramArrayOfString)
	    throws GinaException, RemoteException;

  public abstract List<Map<String, String>> getUsers(String paramString1, String paramString2, String[] paramArrayOfString)
    throws GinaException, RemoteException;

  public abstract List<Map<String, String>> getUsersByPhone(String paramString, Boolean paramBoolean, String[] paramArrayOfString)
    throws GinaException, RemoteException;

  public abstract List<Map<String, String>> getUsersBySIRHNumber(String paramString, Boolean paramBoolean, String[] paramArrayOfString)
    throws GinaException, RemoteException;
  
  public abstract List<Map<String, String>> getUsersByName(String paramString, Boolean paramBoolean, String[] paramArrayOfString)
    throws GinaException, RemoteException;

  public abstract List<String> getInheritingRoles(String paramString1, String paramString2)
    throws UnsupportedOperationException;

  public abstract List<String> getPMProprieteMetier(String paramString)
    throws UnsupportedOperationException;

  public abstract String getOwnIDUniqueForPPorPseudo()
    throws UnsupportedOperationException;

  public abstract List<String> getOwnPMProprieteMetier(String paramString)
    throws UnsupportedOperationException;

  public abstract List<String> getPPProprieteMetier(String paramString)
    throws UnsupportedOperationException;

  public abstract List<String> getOwnPPProprieteMetier(String paramString)
    throws UnsupportedOperationException;

  /*@Deprecated
  public abstract void sendMail(String paramString1, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString2, String paramString3, String paramString4)
    throws GinaException, RemoteException;*/
}// End of source file