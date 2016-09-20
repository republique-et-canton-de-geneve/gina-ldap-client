package gina.api;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GinaApiLdapTest {
    
    @Test
    public void getIsValidUserTest() {
	System.out.println("getIsValidUserTest");
	//Indique si le login passé en paramètre est un login existant ou non.
	try {

	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().isValidUser("benammoura");
	    System.out.println("user valid " + ret);	    
	    
	    if (!ret){	
		  assertTrue(false);
	    }
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }
    
    @Test
    public void getUserAttrsTest() {
	System.out.println("getUserAttrsTest");
	 //Donne les valeurs des attributs passé en paramètre pour l'utilisateur passé en paramètre 
	try {

	    Map<String, String> att = new HashMap<String, String>();
	    String[] param = { "initials", "givenName", "sn" };
	    att = GinaApiLdapBaseFactory.getInstanceDomain().getUserAttrs("benammoura", param);
	    System.out.println("nb attribut  : " + att.size());

	
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }

    
    @Test
    public void getUserTest() {
	System.out.println("getUserTest");
    //Donne la liste des utilisateurs ayant accès à l'application passée en paramètre,
	try {

	    List<Map<String, String>> user = new ArrayList<Map<String, String>>();
	    String[] param = { "initials", "givenName", "sn" };
	    user = GinaApiLdapBaseFactory.getInstanceDomain().getUsers("GEN-ROLES", param);
	    //System.out.println("nb user  : " + user.size() + user.get(0).get("initials"));
	    Map<String, String> temp =  user.get(0);
	    String s = temp.get(new String("initials"));
	    if (!s.equals("AB")){	
		  assertTrue(false);
	    }
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }
    
    
    @Test
    public void getUserTestRole() {
	System.out.println("getUserTest");
    //Donne la liste des utilisateurs ayant accès à l'application passée en paramètre,
	try {

	    List<Map<String, String>> user = new ArrayList<Map<String, String>>();
	    String[] param = { "initials", "givenName", "sn" };
	    user = GinaApiLdapBaseFactory.getInstanceDomain().getUsers("GEN-ROLES","GEN-ROLES-SMIL" , param);
	    //System.out.println("nb user  : " + user.size() + user.get(0).get("initials"));
	    Map<String, String> temp =  user.get(0);
	    String s = temp.get(new String("initials"));
	    if (!s.equals("FB")){	
		  assertTrue(false);
	    }
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }

    
  
    @Test
    public void getAppRolesTest() {
    //Donne la liste des rôles de l'application passée en paramètre.
	System.out.println("getAppRolesTest");
	try {
	    System.out.println("getAppRolesTest");
	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getAppRoles("GEN-ROLES");
	    System.out.println("nb roles GEN-ROLES : " + roles.size());

	    if (!roles.contains(new String("GEN-ROLES-SMIL"))){	
		  assertTrue(false);
	    }
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }

    @Test
    public void getRolesTest() {
        //Donne tous les rôles de l'utilisateur courant pour l'application passée en paramètre
	System.out.println("getRolesTest");
	try {
	    String userName = System.getProperty("user.name");
	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getRoles("GEN-ROLES");
	    System.out.println("nb roles GEN-ROLES : " + roles.size());
	    for (String role : roles) {
		System.out.println(role);
	    }   
	   
	    if (!roles.contains(new String("GEN-ROLES-SMIL"))){	
		  assertTrue(false);
	    }
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }


    @Test
    public void getUserRolesTest() {
	//Donne tous les rôles de l'utilisateur passé en paramètre pour l'application passée en paramètre.
	try {

	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles("fortchantrel","GEN-ROLES");
	   // System.out.println("nb users appli GEN-ROLES roles GEN-ROLES-SMIL : " + users.size());

	    if (!roles.contains(new String("GEN-ROLES-SMIL"))){	
		  assertTrue(false);
	    }
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }
}
