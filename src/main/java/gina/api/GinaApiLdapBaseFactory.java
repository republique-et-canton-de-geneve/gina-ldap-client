package gina.api;

import org.apache.log4j.Logger;

public class GinaApiLdapBaseFactory {

    private static final Logger LOG = Logger.getLogger(GinaApiLdapBaseFactory.class);

    public static GinaApiLdapBaseAble getInstanceApplication() {
	try {
	    return new GinaApiLdapBaseAbleApplicationImpl();

	} catch (RuntimeException e) {
	    LOG.error(e);
	    throw e;
	} catch (Exception e) {
	    LOG.error(e);
	    throw new GinaException(e.getMessage());
	}
    }

    public static GinaApiLdapBaseAble getInstanceDomain() {
	try {
	    return new GinaApiLdapBaseAbleDomainImpl();
	} catch (RuntimeException e) {
	    LOG.error(e);
	    throw e;
	} catch (Exception e) {
	    LOG.error(e);
	    throw new GinaException(e.getMessage());
	}
    }

    public static GinaApiLdapConfig getInstanceConfigApplication() {
	try {
	    return new GinaApiLdapBaseAbleApplicationImpl();
	} catch (RuntimeException e) {
	    LOG.error(e);
	    throw e;
	} catch (Exception e) {
	    LOG.error(e);
	    throw new GinaException(e.getMessage());
	}
    }

    public static GinaApiLdapConfig getInstanceConfigDomain() {
	try {
	    return new GinaApiLdapBaseAbleDomainImpl();
	} catch (RuntimeException e) {
	    LOG.error(e);
	    throw e;
	} catch (Exception e) {
	    LOG.error(e);
	    throw new GinaException(e.getMessage());
	}
    }

}
