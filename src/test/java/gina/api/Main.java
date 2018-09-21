package gina.api;

import gina.impl.GinaLdapFactory;
import gina.impl.GinaException;
import java.rmi.RemoteException;
import java.util.List;

import gina.impl.util.GinaLdapConfiguration;
import gina.impl.util.GinaLdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String TEST_SEPARATOR = "************************ ";

    public static void main(String[] args) {
        System.setProperty("com.sun.jndi.ldap.connect.pool.debug", "fine");

        try {
            String server = "ldaps://vldap-dev.ceti.etat-ge.ch:636";
            String base = "ou=OAC,o=gina";
            String user = "cn=TCNVLDAP9523DEVAAG,ou=Users,ou=CAMAC-GENEVE,ou=OAC,o=gina";
            String password = "Uddyzfsp4";
            int timeout = GinaLdapUtils.LDAP_DEFAULT_TIMEOUT;

            GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(server, base, user, password, timeout);
            GinaApiLdapBaseAble ldapApplication = GinaLdapFactory.getInstance(ldapConf);

            LOG.debug("LDAP APPLICATION");

            String userName = "DRIVONOL";
            LOG.debug("userName=" + userName);

            String roleName = "CAMAC-GENEVE";
            LOG.debug("roleName=" + roleName);

            List<String> roles;

            LOG.debug(TEST_SEPARATOR + "getUserRoles(userName)");
            roles = ldapApplication.getUserRoles(userName);
            LOG.debug("size=" + roles.size());
            LOG.debug("{}", roles);

            LOG.debug(TEST_SEPARATOR + "getUserRoles(userName)");
            roles = ldapApplication.getUserRoles(userName);
            for (String role : roles) {
                LOG.debug("role=" + role);
            }

            LOG.debug(TEST_SEPARATOR + "getAppRoles(roleName)");
            roles = ldapApplication.getAppRoles(roleName);
            LOG.debug("size=" + roles.size());
            for (String role : roles) {
                LOG.debug("role=" + role);
            }
        } catch (GinaException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
