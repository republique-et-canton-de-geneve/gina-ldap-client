package gina.api.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class GinaApiLdapUtils {
    
    public static final String ATTRIBUTE_MEMBEROF = "memberOf";
    public static final String ATTRIBUTE_MEMBER = "member";
    public static final String ATTRIBUTE_CN = "cn";

    public static final int LDAP_DEFAULT_TIMEOUT = 3000;

    private GinaApiLdapUtils() {
    }
    
    public static String extractDomain(final String domaineapplication) {
	String result = null;
	if (StringUtils.isNotBlank(domaineapplication) && domaineapplication.contains(".")) {
	    final String[] splitApplication = StringUtils.split(domaineapplication, ".", 2);
	    result = splitApplication[0];
	}
	return result;
    }
    
    public static String extractApplication(final String domaineapplication) {
	String result = null;
	if (StringUtils.isNotBlank(domaineapplication) && domaineapplication.contains(".")) {
	    final String[] splitApplication = StringUtils.split(domaineapplication, ".", 2);
	    result = splitApplication[1];
	}
	return result;
    }
    
    public static String createPropertie(final List<String> list) {
	StringBuilder builder = new StringBuilder();
	int size = 0;
	for (String s : list) {
	    size++;
	    builder.append(s);
	    if (size < list.size()) {
		builder.append(',');
	    }
	}
	return builder.toString();
    }

    public static String getLdapFilterUser(String user) {
	return "(&(objectClass=user)(cn=" + user + "))";
    }

}
