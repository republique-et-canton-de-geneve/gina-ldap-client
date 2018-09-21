package gina.api.gina.api.utils;

import gina.impl.GinaLdapCommon;
import gina.impl.GinaException;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.rules.ExpectedException;

public class TestTools {

    public static boolean rolesAreCleaned(final List<String> roles) {
        boolean result = true;
        for (String role : roles) {
            if (role.contains("cn=")) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static void expectNotImplemented(ExpectedException thrown) {
        thrown.expect(GinaException.class);
        thrown.expectMessage(CoreMatchers.containsString(GinaLdapCommon.NOT_IMPLEMENTED));
    }

}
