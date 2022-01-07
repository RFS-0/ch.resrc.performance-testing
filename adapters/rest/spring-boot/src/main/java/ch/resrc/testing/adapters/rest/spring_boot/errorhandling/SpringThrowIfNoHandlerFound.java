package ch.resrc.testing.adapters.rest.spring_boot.errorhandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class SpringThrowIfNoHandlerFound {

    @Autowired
    public void configure(DispatcherServlet dispatcherServlet) {
        // Let our REST exception handler handle illegal REST URLs
        // for which no handler method can be found:
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
    }
}
