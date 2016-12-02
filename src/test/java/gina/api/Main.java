package gina.api;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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
	    

	    
	} catch (GinaException e) {

	    e.printStackTrace();
	} catch (RemoteException e) {
	    e.printStackTrace();
	}

    }

}
