package gina.api;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.DirContext;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class GinaApiLdapDomainTest {
    
    private DirContext ctxtDir = null;
    private static final Logger LOG = Logger.getLogger(GinaApiLdapDomainTest.class);
	    
	    

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
    public void hasRoleTest() {
	System.out.println("hasRoleTest");
	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().hasRole("ACT-JMS-GDE", "TECH-AUDITEUR-AUTORISATIONS");
	    if (ret) {
		assertTrue(false);
	    } else {
		assertTrue(true);
	    }
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
    
    @Test
    public void hasRoleUserTest() {
	System.out.println("hasUserRole");
	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole("fortchantrel" , "ACT-JMS", "TECH-AUDITEUR-AUTORISATIONS");
	    if (ret) {
		assertTrue(true);
	    } else {
		assertTrue(false);
	    }
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
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
		
		if (role.contains(new String("GEN-ROLES-SMIL"))){
		    System.out.println(role);
		    assertTrue(true);
		}
	    } 
	    assertEquals(155, roles.size());
	   
	    
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}

	
    }

    

    @Test
    public void getUserRolesTest() {
	//Donne tous les rôles de l'utilisateur passé en paramètre pour l'application passée en paramètre.
	System.out.println("getUserRolesTest");
	try {

	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles("fortchantrel","GEN-ROLES");
	   // System.out.println("nb users appli GEN-ROLES roles GEN-ROLES-SMIL : " + users.size());
	    for (String role : roles) {
		
		if (role.contains(new String("GEN-ROLES-SMIL"))){
		    System.out.println(role);
		    assertTrue(true);
		}
	    } 
	    assertEquals(155, roles.size());
	   
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }
   
    
    @Test
    public void getUserRoleTest() {
	System.out.println("getUserRoleTest");
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

  

}
