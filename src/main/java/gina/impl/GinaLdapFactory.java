package gina.impl;

import gina.api.GinaApiLdapBaseAble;
import gina.impl.util.GinaLdapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaLdapFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapFactory.class);

    private GinaLdapFactory() {
    }

    public static GinaApiLdapBaseAble getInstance(GinaLdapConfiguration ldapConf) {
        try {
            GinaApiLdapBaseAble result;

            if (ldapConf.getLdapType() == GinaLdapConfiguration.Type.APPLICATION) {
                result = new GinaLdapApplication(ldapConf);
            } else if (ldapConf.getLdapType() == GinaLdapConfiguration.Type.DOMAIN) {
                result = new GinaLdapDomain(ldapConf);
            } else {
                throw new GinaException("Unexpected LDAP type [" + ldapConf.getLdapType() + "]");
            }

            return result;
        } catch (RuntimeException e) {
            LOGGER.error("Erreur : ", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Erreur : ", e);
            GinaException ge = new GinaException(e.getMessage());
            ge.initCause(e);
            throw ge;
        }
    }

}
