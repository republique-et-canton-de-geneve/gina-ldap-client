package gina.api;

import gina.api.util.GinaApiLdapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GinaApiLdapBaseFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaApiLdapBaseFactory.class);

    private GinaApiLdapBaseFactory() {
    }

    public static GinaApiLdapBaseAble getInstance(GinaApiLdapConfiguration ldapConf) {
        try {
            GinaApiLdapBaseAble result;


            if (GinaApiLdapConfiguration.APPLICATION.equals(ldapConf.getLdapType())) {
                result = new GinaApiLdapBaseAbleApplicationImpl(ldapConf);
            } else {
                result = new GinaApiLdapBaseAbleDomainImpl(ldapConf);
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
