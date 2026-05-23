package co.edu.unbosque.proyectofinal;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

/**
 * Clase principal de la plataforma de conversión de archivos.
 * <p>
 * Esta aplicación permite a los usuarios convertir archivos de audio,
 * video e imagen entre distintos formatos utilizando una API externa
 * gratuita (CloudConvert). También gestiona usuarios con dos roles
 * (USUARIO y ADMIN), autenticación mediante JWT y mantiene el historial
 * de cada conversión.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 2.0
 */
@SpringBootApplication
public class ProyectofinalApplication {

	/**
	 * Punto de entrada principal de la aplicación.
	 *
	 * @param args Argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProyectofinalApplication.class, args);
	}

	/**
	 * Bean de ModelMapper disponible en todo el contexto de Spring,
	 * utilizado para mapear entidades a DTOs y viceversa.
	 *
	 * @return Instancia configurada de {@link ModelMapper}.
	 */
	@Bean
	public ModelMapper getModelMapper() {
		return new ModelMapper();
	}

	/**
	 * Bean de RestTemplate utilizado para realizar las llamadas HTTP
	 * a la API REST externa de CloudConvert.
	 *
	 * @return Instancia de {@link RestTemplate}.
	 */
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	/**
	 * Bean del codificador de contraseñas BCrypt usado en todo el sistema
	 * para cifrar contraseñas antes de persistirlas y para validar
	 * credenciales durante el login.
	 *
	 * @return Instancia de {@link BCryptPasswordEncoder}.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}