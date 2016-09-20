package gina.api.util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import gina.api.GinaApiLdapBaseAble;
import gina.api.GinaApiLdapBaseFactory;
import gina.api.GinaException;

public class Main {

    public static void main(String[] args) {
	
	try {
	    String userName = System.getProperty("user.name");
            List<String> roles = new ArrayList<String>();
            roles = gina.api.GinaApiLdapBaseFactory.getInstanceApplication().getRoles("CAMAC-GENEVE");
            System.out.println("getRoles('CAMAC-GENEVE'):");
            for (String role : roles) {
		System.out.println("role" + role);
	    }

            
            userName = "DRIVONOL";
            roles = gina.api.GinaApiLdapBaseFactory.getInstanceApplication().getUserRoles(userName, "CAMAC-GENEVE");
            System.out.println(".getUserRoles(userName, 'CAMAC-GENEVE'):");
            for (String role : roles) {
		System.out.println("role" + role);
	    }

	    

	    roles = GinaApiLdapBaseFactory.getInstanceApplication().getAppRoles("CAMAC-GENEVE");
	    System.out.println("getAppRoles('CAMAC-GENEVE'):" + roles.size());
	    for (String role : roles) {
		System.out.println("role" + role);
	    }
	    
	    
	  
  
	 /*   String userName = System.getProperty("user.name");
	    List<String> roles = new ArrayList<String>();
	    roles = GinaApiLdapBaseFactory.getInstanceApplication().getRoles("GEN-ROLES");
	    System.out.println("nb roles GEN-ROLES : " + roles.size());
	    for (String role : roles) {
		System.out.println(role);
	    }
	    
	    
	    List<String> users = new ArrayList<String>();
	    users = GinaApiLdapBaseFactory.getInstanceApplication().getUserRoles("GEN-ROLES","GEN-ROLES-SMIL");
	    System.out.println("nb users appli GEN-ROLES roles GEN-ROLES-SMIL : " + users.size());
	    for (String user : users) {
		System.out.println(user);
	    }*/
	    
	} catch (GinaException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (RemoteException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	//conf.getCtxtDir()
    }

}
