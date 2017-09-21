package gina.api;

import java.util.List;

public class GinaApiLdapTools {

    protected static boolean checkRolesAreCleaned(final List<String> roles) {
	boolean result = true;    
	for(String role : roles) {
	    if(role.contains("cn=")) {
		result = false;
		break;
	    }
	}
	return result;
    }
    

}
