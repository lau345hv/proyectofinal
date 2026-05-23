package co.edu.unbosque.proyectofinal.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.service.CloudConvertService;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador REST encargado de recibir los archivos del usuario y
 * delegar la conversión al servicio de la API externa (CloudConvert).
 * <p>
 * El endpoint de conversión requiere un token JWT válido en el header
 * {@code Authorization: Bearer <token>}. El identificador del usuario
 * se extrae automáticamente del token mediante {@link AuthenticationPrincipal}
 * y se asocia al historial de conversión.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
@RestController
@RequestMapping("/conversion")
public class ConversionController {

	private final CloudConvertService cloudConvertService;
	private final LanzadorDeExcepcion lanzador;

	public ConversionController(CloudConvertService cloudConvertService,
			LanzadorDeExcepcion lanzador) {
		this.cloudConvertService = cloudConvertService;
		this.lanzador = lanzador;
	}

	@PostMapping(value = "/convertir", consumes = "multipart/form-data")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Convertir un archivo (requiere autenticación)",
			description = "Sube un archivo y retorna la URL de descarga del archivo "
					+ "convertido. Requiere un token JWT válido.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Conversión exitosa"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos")
	})
	public ResponseEntity<String> convertir(
			@AuthenticationPrincipal Usuario usuarioAutenticado,
			@RequestParam("archivo") MultipartFile archivo,
			@RequestParam(value = "tipoArchivo")
				@io.swagger.v3.oas.annotations.Parameter(
						schema = @Schema(allowableValues = { "AUDIO", "VIDEO", "IMAGEN" }))
				String tipoArchivo,
			@RequestParam(value = "formatoDestino")
				@io.swagger.v3.oas.annotations.Parameter(
						description = "Audio: mp3, wav, aac, flac, ogg, m4a · "
								+ "Video: mp4, mkv, avi, mov, webm, flv · "
								+ "Imagen: jpg, png, webp, gif, bmp, tiff")
				String formatoDestino) {
		TipoArchivo tipo;
		try {
			tipo = TipoArchivo.valueOf(tipoArchivo.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("tipoArchivo", tipoArchivo, "AUDIO, VIDEO, IMAGEN");
			return null;
		}
		String contentType = archivo.getContentType();
		if (contentType != null) {
			String mimePrefix = contentType.split("/")[0].toLowerCase();
			String esperado;
			switch (tipo) {
				case AUDIO: esperado = "audio"; break;
				case VIDEO: esperado = "video"; break;
				case IMAGEN: esperado = "image"; break;
				default: esperado = ""; break;
			}
			if (!esperado.isEmpty() && !mimePrefix.equals(esperado)) {
				lanzador.lanzarTipoArchivoNoCoincide(
						tipo.name(), mimePrefix.toUpperCase());
			}
		}
		String urlDescarga = cloudConvertService.convertirArchivo(
				archivo, formatoDestino, tipo, usuarioAutenticado.getId());
		return new ResponseEntity<>(urlDescarga, HttpStatus.OK);
	}

	@GetMapping("/formatos")
	@Operation(summary = "Listar los formatos soportados para un tipo de archivo",
			description = "ENDPOINT PÚBLICO. Útil para llenar el desplegable de "
					+ "formatoDestino desde el frontend.")
	public ResponseEntity<Set<String>> formatosPorTipo(
			@RequestParam(value = "tipoArchivo")
				@io.swagger.v3.oas.annotations.Parameter(
						schema = @Schema(allowableValues = { "AUDIO", "VIDEO", "IMAGEN" }))
				String tipoArchivo) {
		TipoArchivo tipo;
		try {
			tipo = TipoArchivo.valueOf(tipoArchivo.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("tipoArchivo", tipoArchivo, "AUDIO, VIDEO, IMAGEN");
			return null;
		}
		Set<String> formatos = cloudConvertService.getFormatosDisponibles(tipo);
		return new ResponseEntity<>(formatos, HttpStatus.OK);
	}
}