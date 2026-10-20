package controlador;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InicioAplicacion implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent evento) {
        evento.getServletContext().setAttribute("contenedor", new Contenedor());
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
    }
}
