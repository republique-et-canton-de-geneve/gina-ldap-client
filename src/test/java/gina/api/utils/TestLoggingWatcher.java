package gina.api.utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestWatcher JUnit pour afficher les evenements (d�but, fin, etc.) de chaque methode de test.
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
