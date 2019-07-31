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
package gina.api.utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestWatcher JUnit pour afficher les evenements (debut, fin, etc.) de chaque methode de test.
 */
public class TestLoggingWatcher extends TestWatcher {

    @Override
    protected void starting(Description description) {
        getLogger(description).info("DEBUT de {}", description.getMethodName());
    }

    @Override
    protected void finished(Description description) {
        getLogger(description).info("FIN de {}", description.getMethodName());
    }

    private Logger getLogger(Description description) {
        return LoggerFactory.getLogger(description.getTestClass());
    }

}
