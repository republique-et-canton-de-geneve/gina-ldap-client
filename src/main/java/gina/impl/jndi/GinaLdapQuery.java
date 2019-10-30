/*
 * GINA LDAP client
 *
 * Copyright 2016-2019 Republique et canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gina.impl.jndi;

import gina.impl.GinaException;
import gina.impl.util.GinaLdapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gina.impl.util.GinaLdapUtils.handle;

public class GinaLdapQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapQuery.class);
	
    private final DirContext ctx;

    private final SearchControls constraints = new SearchControls();

    private String baseDn;

    private String filter;

    public enum Scope {
        OBJECT(SearchControls.OBJECT_SCOPE),
        ONELEVEL(SearchControls.ONELEVEL_SCOPE),
        SUBTREE(SearchControls.SUBTREE_SCOPE);

        private final int value;

        Scope(int value) {
            this.value = value;
        }
    }

    /**
     * TODO remplacer par un lambda
     */
    public interface Consumer<T> {
        T consume(String dn, Attributes attrs) throws NamingException;
    }

    GinaLdapQuery(DirContext ctx) {
        this.ctx = ctx;
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningObjFlag(false);
    }

    public GinaLdapQuery setBaseDn(String baseDn) {
        this.baseDn = baseDn;
        return this;
    }

    public GinaLdapQuery setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public GinaLdapQuery setScope(Scope scope) {
        constraints.setSearchScope(scope.value);
        return this;
    }

    public GinaLdapQuery setAttributes(String... attrs) {
        constraints.setReturningAttributes(attrs);
//        constraints.setReturningObjFlag(true);
        constraints.setReturningObjFlag(false);
        return this;
    }

    public GinaLdapQuery setAllAttributes() {
        constraints.setReturningAttributes(null);
//        constraints.setReturningObjFlag(true);
        constraints.setReturningObjFlag(false);
        return this;
    }

    public <T> List<T> forEach(Consumer<T> cons) {
        NamingEnumeration<SearchResult> enm = startSearch();
        try {
            List<T> list = new ArrayList<>();
            while (enm.hasMore()) {
                SearchResult sr = enm.next();
                T t = invokeCons(cons, sr);
                if (t != null) {
                    list.add(t);
                }
            }
            return list;
        } catch (NamingException e) {
            return handle(e);
        } finally {
            GinaLdapUtils.closeQuietly(enm);
        }
    }

    public <T> T unique(Consumer<T> cons) {
        NamingEnumeration<SearchResult> enm = startSearch();
        try {
            T result = null;
            while (enm.hasMore()) {
                SearchResult sr = enm.next();
                T t = invokeCons(cons, sr);
                if (t != null) {
                    if (result == null) {
                        result = t;
                    } else {
                        throw new GinaException("Plus d'un resultat obtenu - "
                                + "base DN = [" + baseDn + "], filtre = [" + filter + "]");
                    }
                }
            }
            return result;
        } catch (NamingException e) {
            return handle(e);
        } finally {
            GinaLdapUtils.closeQuietly(enm);
        }
    }

    public <T> T first(Consumer<T> cons) {
        NamingEnumeration<SearchResult> enm = startSearch();
        try {
            while (enm.hasMore()) {
                SearchResult sr = enm.next();
                T t = invokeCons(cons, sr);
                if (t != null) {
                    return t;
                }
            }
            return null;
        } catch (NamingException e) {
            return handle(e);
        } finally {
            GinaLdapUtils.closeQuietly(enm);
        }
    }

    private NamingEnumeration<SearchResult> startSearch() {
        LOGGER.debug("Requete LDAP = {}", this);
        try {
            return ctx.search(baseDn, filter, constraints);
        } catch (NamingException e) {
            return handle(e);
        }
    }

    private static <T> T invokeCons(Consumer<T> cons, SearchResult sr) {
        try {
            return cons.consume(sr.getNameInNamespace(), sr.getAttributes());
        } catch (NamingException e) {
            return handle(e);
        }
    }

    @Override
    public String toString() {
    	return new StringBuilder()
    			.append("{")
    			.append("base=")
    			.append(baseDn)
    			.append(", scope=")
    			.append(constraints.getSearchScope())
    			.append(", deref=")
    			.append(constraints.getDerefLinkFlag())
    			.append(", sizelimit=")
    			.append(constraints.getCountLimit())
    			.append(", timelimit=")
    			.append(constraints.getTimeLimit())
    			.append(", filter=")
    			.append(filter)
    			.append(", attrs=")
//    			.append(Arrays.asList(constraints.getReturningAttributes()))
    			.append(Arrays.asList(ArrayUtils.nullToEmpty(constraints.getReturningAttributes())))
    			.append("}")
    			.toString();
    }

}
