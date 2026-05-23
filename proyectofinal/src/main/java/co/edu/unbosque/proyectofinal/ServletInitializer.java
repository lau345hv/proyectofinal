package co.edu.unbosque.proyectofinal;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador de servlet que permite desplegar la aplicación
 * como archivo WAR en un servidor Tomcat externo.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ProyectofinalApplication.class);
	}

}
