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

import gina.api.util.Configuration;

public class GinaApiLdapApplicationTest {
    
    private DirContext ctxtDir = null;
    private static final Logger LOG = Logger.getLogger(GinaApiLdapApplicationTest.class);

    @Before
    public void init(){
	 
	if (ctxtDir == null) {
	    LOG.info("init()");
	    ConfigurationTest conf = new ConfigurationTest();
	    conf.init("application");
	    ctxtDir = conf.getCtxtDir();
	    if (ctxtDir == null) {
		throw new GinaException("initialisation impossible");
	    }
	}
	    
    }
    
   
    
    
    @Test
    public void getUserRolesTest(){
	LOG.info("getUserRolesTest");
	
	try {
	    List<String> ret = GinaApiLdapBaseFactory.getInstanceApplication().getUserRoles("DRIVONOL");
	    assertSame(13 , ret.size());	   
    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
   
    @Test
    public void getIsValidUserTest() {
	LOG.info("getIsValidUserTest");

	try {

	    boolean ret = GinaApiLdapBaseFactory.getInstanceApplication().isValidUser("DRIVONOL");
	    LOG.info("user valid " + ret);	    
	    
	    if (!ret){	
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
    public void getUserAttrsTest() {
	LOG.info("getUserAttrsTest");

	try {
	    String[] paramArrayOfString = {"uid"};
	    Map<String, String> ret = GinaApiLdapBaseFactory.getInstanceApplication().getUserAttrs("DRIVONOL", paramArrayOfString);
	    for (Map.Entry<String,String> e : ret.entrySet()){
		LOG.info(e.getKey() + " : " + e.getValue());
		if(e.getKey().equalsIgnoreCase("uid")) {
		    assertTrue(e.getValue().equalsIgnoreCase("DRIVONOL"));	    
		}
		
	    }
	    LOG.info("user valid " + ret);	    
	    
	    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
    @Test
    public void hasRoleTest() {
	LOG.info("hasRoleTest");

	try {
	    String[] paramArrayOfString = {"uid"};
	    boolean ret = GinaApiLdapBaseFactory.getInstanceApplication().hasRole("ADMIN");
	    assertSame(false , ret);	   
    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
    @Test
    public void hasRoleUserTest() {
	LOG.info("hasRoleUserTest");

	try {
	    boolean ret = GinaApiLdapBaseFactory.getInstanceApplication().hasUserRole("DRIVONOL", "ADMIN");
	    assertSame(true , ret);	   
    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
    
    @Test
    public void getRolesTest(){
	LOG.info("getRolesTest");
	
	try {
	    List<String> ret = GinaApiLdapBaseFactory.getInstanceApplication().getRoles();
	    assertSame(0 , ret.size());	   
    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
    @Test
    public void getAppRolesTest(){
	LOG.info("getAppRolesTest");
	
	try {
	    List<String> ret = GinaApiLdapBaseFactory.getInstanceApplication().getAppRoles("CAMAC-GENEVE");
	    assertSame(21 , ret.size());	   
    
	    
	} catch (GinaException e) {
	    assertTrue(false);
	} catch (RemoteException e) {
	    assertTrue(false);
	}
    }
    
    
    
    
    
    
}
