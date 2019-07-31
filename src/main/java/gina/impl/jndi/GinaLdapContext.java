package gina.impl.jndi;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import java.io.Closeable;
import java.util.Hashtable;
import java.util.Properties;

public class GinaLdapContext implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GinaLdapContext.class);

    private final DirContext context;

    public GinaLdapContext(Properties props) throws NamingException {
        Hashtable<Object, Object> ht = new Hashtable<>();
        ht.putAll(props);
        context = new InitialLdapContext(ht, null);
    }

    public GinaLdapQuery newQuery() {
        return new GinaLdapQuery(context);
    }

    @Override
    public void close() {
        try {
            context.close();
        } catch (NamingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

}
