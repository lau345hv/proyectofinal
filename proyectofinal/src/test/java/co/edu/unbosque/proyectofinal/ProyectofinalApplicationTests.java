package co.edu.unbosque.proyectofinal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Pruebas de arranque de la aplicación Spring Boot.
 *
 * @author Equipo de desarrollo
 * @version 1.1
 */
@SpringBootTest
class ProyectofinalApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context, "El contexto de Spring no debería ser nulo.");
    }
}