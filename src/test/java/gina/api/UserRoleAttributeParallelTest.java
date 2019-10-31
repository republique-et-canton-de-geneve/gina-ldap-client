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
package gina.api;

import com.googlecode.junittoolbox.ParallelRunner;
import gina.api.utils.TestConstants;
import gina.impl.GinaLdapAccess;
import gina.impl.util.GinaLdapConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ParallelRunner.class)
public class UserRoleAttributeParallelTest extends UserRoleAttributeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleAttributeParallelTest.class);

    @BeforeClass
    public static void initApi() {
        LOGGER.info(TestConstants.START_METHOD);

        System.setProperty("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
        System.setProperty("com.sun.jndi.ldap.connect.pool.authentication", "simple");
        System.setProperty("com.sun.jndi.ldap.connect.pool.maxsize", "5");
        System.setProperty("com.sun.jndi.ldap.connect.pool.prefsize", "3");
        System.setProperty("com.sun.jndi.ldap.connect.pool.timeout", "240000");
        System.setProperty("com.sun.jndi.ldap.connect.pool.initsize", "3");
        System.setProperty("com.sun.jndi.ldap.connect.pool.debug", "fine");

        GinaLdapConfiguration ldapConf = getGinaLdapConfiguration();
        gina = new GinaLdapAccess(ldapConf);

        LOGGER.info(TestConstants.END_METHOD);
    }

    @AfterClass
    public static void releaseResources() {
        IOUtils.closeQuietly(gina);
    }

}
