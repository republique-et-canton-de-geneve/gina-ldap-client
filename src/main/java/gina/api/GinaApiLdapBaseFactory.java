package gina.api;

import org.apache.log4j.Logger;

import gina.api.util.GinaApiLdapDirContext;

public class GinaApiLdapBaseFactory {

    // Logger
    private static final Logger LOG = Logger.getLogger(GinaApiLdapBaseFactory.class);

    // Constructeur
    private GinaApiLdapBaseFactory() {
    }

    public static GinaApiLdapBaseAble getInstance() {
	GinaApiLdapDirContext galdc = new GinaApiLdapDirContext();
	galdc.init();
	return getInstance(galdc);
    }

    /**
     * @deprecated(Il n'est pas recommandé d'utiliser cette méthode en direct. L'instanciation est censé se faire via le fichier de properties du serveur via getInstance)
     * @param galdc
     * @return
     */
    @Deprecated
    public static GinaApiLdapBaseAble getInstance(GinaApiLdapDirContext galdc) {
	try {
	    GinaApiLdapBaseAble result;

	    if (GinaApiLdapDirContext.APPLICATION.equals(galdc.getType())) {
		result = new GinaApiLdapBaseAbleApplicationImpl(galdc.getCtxtDir());
	    } else {
		result = new GinaApiLdapBaseAbleDomainImpl(galdc.getCtxtDir());
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
