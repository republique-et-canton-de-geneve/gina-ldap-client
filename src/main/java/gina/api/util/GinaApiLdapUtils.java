package gina.api.util;

import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import gina.api.GinaException;

public class GinaApiLdapUtils {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapUtils.class);

    // LDAP Attributes
    public static final String ATTRIBUTE_MEMBEROF = "memberOf";
    public static final String ATTRIBUTE_MEMBER = "member";
    public static final String ATTRIBUTE_CN = "cn";
    public static final String ATTRIBUTE_DEPARTMENT_NUMBER = "departmentNumber";
    public static final int LDAP_DEFAULT_TIMEOUT = 3000;

    // Constructeur
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

    public static String getLdapFilterOu(String ou) {
	return "ou=" + ou;
    }

    public static String getLdapFilterGroup(String role) {
	return "ou=Groups,ou=" + role;
    }

    public static String getLdapFilterUser(String user) {
	return "(&(objectClass=person)(cn=" + user + "))";
    }

    public static String getLdapFilterCn(String cn) {
	return "(&(cn=" + cn + "))";
    }

    public static void closeQuietly(NamingEnumeration<?> obj) {
	if (obj != null) {
	    try {
		obj.close();
	    } catch (NamingException e) {
		LOG.error(e);
	    }
	}
    }

}
