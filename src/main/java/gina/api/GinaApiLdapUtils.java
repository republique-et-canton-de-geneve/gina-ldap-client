package gina.api;

import org.apache.commons.lang.StringUtils;

public class GinaApiLdapUtils {

    protected static String extractDomain(final String domaineapplication) {
	String result = null;
	if (StringUtils.isNotBlank(domaineapplication) && domaineapplication.contains(".")) {
	    final String[] splitApplication = StringUtils.split(domaineapplication, ".", 2);
	    result = splitApplication[0];
	}
	return result;
    }
    
    protected static String extractApplication(final String domaineapplication) {
	String result = null;
	if (StringUtils.isNotBlank(domaineapplication) && domaineapplication.contains(".")) {
	    final String[] splitApplication = StringUtils.split(domaineapplication, ".", 2);
	    result = splitApplication[1];
	}
	return result;
    }
    

}
