package gina.api;

import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapConfiguration;
import gina.api.util.GinaApiLdapDirContext;

public class GinaApiLdapBaseFactory {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapBaseFactory.class);

    // Constructeur
    private GinaApiLdapBaseFactory() {
    }

    public static GinaApiLdapBaseAble getInstance(GinaApiLdapConfiguration ldapConf) {
	try {
	    GinaApiLdapBaseAble result;

	    if (GinaApiLdapDirContext.APPLICATION.equals(ldapConf.getLdapType())) {
		result = new GinaApiLdapBaseAbleApplicationImpl(ldapConf);
	    } else {
		result = new GinaApiLdapBaseAbleDomainImpl(ldapConf);
	    }

	    return result;
	} catch (RuntimeException e) {
	    LOG.error(e);
	    throw e;
	} catch (Exception e) {
	    LOG.error(e);
	    throw new GinaException(e.getMessage());
	}
    }

}
