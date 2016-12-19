package gina.api;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class GinaApiLdapDomainTest {

    private static final Logger LOG = Logger.getLogger(GinaApiLdapDomainTest.class);

    @Test
    public void getIsValidUserTest() {
	LOG.info("getIsValidUserTest");
	// Indique si le login passé en paramètre est un login existant ou non.
	try {

	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().isValidUser("benammoura");
	    LOG.info("user valid " + ret);

	    if (!ret) {
		Assert.assertTrue("L'utilisateur  benammoura  est censé être valide !", ret); 
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
	LOG.info("getUserAttrsTest");
	// Donne les valeurs des attributs passé en paramètre pour l'utilisateur
	// passé en paramètre
	try {

	    Map<String, String> att = new HashMap<String, String>();
	    String[] param = { "initials", "givenName", "sn" };
	    att = GinaApiLdapBaseFactory.getInstanceDomain().getUserAttrs("benammoura", param);
	    Assert.assertTrue( "le parametre initials pour benammoura est non vide " , att.get("initials") != null );
	    Assert.assertTrue( "le parametre givenName pour benammoura est non vide " , att.get("givenName") != null );
	    Assert.assertTrue( "le parametre sn pour benammoura est non vide " , att.get("sn") != null );
	    LOG.info("nb attribut  : " + att.size());

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
	LOG.info("getUserRolesTest");
	try {

	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getUserRoles("dtdcours01","GEN-ROLES");
	   // LOG.info("nb users appli GEN-ROLES roles GEN-ROLES-SMIL : " + users.size());
	    for (String role : roles) {
		
		if (role.contains(new String("GEN-ROLES-SMIL"))){
		    LOG.info(role);
		    assertTrue(true);
		}
	    } 
	    assertEquals(8, roles.size());
	   
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
	assertTrue(true);
	
    }
   

    @Test
    public void getUserTest() {
	LOG.info("getUserTest");
	// Donne la liste des utilisateurs ayant accès à l'application passée en
	// paramètre,
	try {

	    List<Map<String, String>> user = new ArrayList<Map<String, String>>();
	    String[] param = { "initials", "givenName", "sn" };
	    user = GinaApiLdapBaseFactory.getInstanceDomain().getUsers("GEN-ROLES", param);
	    Map<String, String> temp = user.get(0);
	    String s = temp.get(new String("initials"));
	    if (!s.equals("AB")) {
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
    public void hasRoleUserTest() {
	LOG.info("hasUserRole");
	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceDomain().hasUserRole("dtdcours01", "ACCESS-CONTROL",
		    "UTILISATEUR");
	    Assert.assertTrue( "dtdcours01 a bien le role UTILISATEUR pour l' appli ACCES-CONTROL" , ret );
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }

    @Test
    public void getUsersTest() {
	LOG.info("getUserRoleTest");
	// Donne la liste des utilisateurs ayant accès à l'application passée en
	// paramètre,
	try {

	    List<Map<String, String>> user = new ArrayList<Map<String, String>>();
	    String[] param = { "initials", "givenName", "sn" };
	    user = GinaApiLdapBaseFactory.getInstanceDomain().getUsers("GEN-ROLES", "GEN-ROLES-SMIL", param);
	    // LOG.info("nb user : " + user.size() +
	    // user.get(0).get("initials"));
	    Map<String, String> temp = user.get(0);
	    String s = temp.get(new String("initials"));
	    if (!s.equals("KA")) {
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
	// Donne la liste des rôles de l'application passée en paramètre.
	LOG.info("getAppRolesTest");
	try {
	    LOG.info("getAppRolesTest");
	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceDomain().getAppRoles("GEN-ROLES");
	    LOG.info("nb roles GEN-ROLES : " + roles.size());

	    if (!roles.contains(new String("GEN-ROLES-SMIL"))) {
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
