package gina.api;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.DirContext;

import org.junit.Before;
import org.junit.Test;

import gina.api.util.Configuration;

public class GinaApiLdapApplicationTest {
    
    private DirContext ctxtDir = null;
    
    @Before
    public void init(){
	 
	if (ctxtDir == null) {
	    System.out.println("init()");
	    Configuration conf = new Configuration();
	    conf.init();
	    ctxtDir = conf.getCtxtDir();
	    if (ctxtDir == null) {
		throw new GinaException("initialisation impossible");
	    }
	}
	    
    }
    
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
    
    
}
