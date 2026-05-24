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
 * Controlador REST encargado de recibir los archivos del usuario y delegar
 * la conversión al servicio de la API externa (CloudConvert).
 *
 * <p>Expone los siguientes endpoints bajo la ruta base {@code /conversion}:</p>
 * <ul>
 *   <li>{@code POST /conversion/convertir}: recibe un archivo multipart,
 *       lo envía a CloudConvert y devuelve la URL de descarga del resultado.
 *       Requiere autenticación JWT.</li>
 *   <li>{@code GET /conversion/formatos}: devuelve los formatos de destino
 *       disponibles para un tipo de archivo dado. Endpoint público.</li>
 * </ul>
 *
 * <p>El endpoint de conversión extrae el usuario autenticado directamente
 * del token JWT mediante {@link AuthenticationPrincipal}, evitando pasar
 * el identificador como parámetro explícito y asociando la conversión al
 * historial del usuario de forma segura.</p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 * @see CloudConvertService
 * @see LanzadorDeExcepcion
 * @see TipoArchivo
 */
@RestController
@RequestMapping("/conversion")
public class ConversionController {

    /** Servicio que gestiona la comunicación con la API externa CloudConvert. */
    private final CloudConvertService cloudConvertService;

    /** Componente centralizado para lanzar excepciones tipadas con mensajes uniformes. */
    private final LanzadorDeExcepcion lanzador;

    /**
     * Construye el controlador inyectando sus dependencias.
     *
     * @param cloudConvertService servicio que gestiona la conversión con CloudConvert
     * @param lanzador            componente centralizado para lanzar excepciones tipadas
     */
    public ConversionController(CloudConvertService cloudConvertService,
            LanzadorDeExcepcion lanzador) {
        this.cloudConvertService = cloudConvertService;
        this.lanzador = lanzador;
    }

    /**
     * Recibe un archivo multipart, lo convierte al formato indicado mediante la API
     * de CloudConvert y retorna la URL de descarga del archivo convertido.
     *
     * <p>El flujo interno es el siguiente:</p>
     * <ol>
     *   <li>Se convierte el parámetro {@code tipoArchivo} al enum {@link TipoArchivo};
     *       si el valor no es reconocido se lanza una excepción de opción no válida.</li>
     *   <li>Se verifica que el tipo MIME del archivo ({@code Content-Type}) coincida con
     *       la categoría declarada en {@code tipoArchivo}. Por ejemplo, un archivo
     *       con MIME {@code video/*} no puede procesarse como {@code AUDIO}.</li>
     *   <li>Si las validaciones pasan, se delega la conversión a
     *       {@link CloudConvertService#convertirArchivo(MultipartFile, String, TipoArchivo, Long)}.</li>
     *   <li>Se retorna la URL de descarga del archivo resultante.</li>
     * </ol>
     *
     * <p><b>Requiere</b> un token JWT válido en el encabezado
     * {@code Authorization: Bearer <token>}.</p>
     *
     * @param usuarioAutenticado usuario extraído del token JWT por Spring Security;
     *                           se usa su {@code id} para asociar la conversión
     *                           al historial personal
     * @param archivo            archivo a convertir enviado como {@code multipart/form-data};
     *                           el tamaño máximo permitido es 500 MB
     * @param tipoArchivo        categoría del archivo en mayúsculas: {@code AUDIO},
     *                           {@code VIDEO} o {@code IMAGEN}
     * @param formatoDestino     extensión del formato de salida deseado, por ejemplo:
     *                           {@code mp3}, {@code wav} para audio;
     *                           {@code mp4}, {@code mkv} para video;
     *                           {@code jpg}, {@code png} para imagen
     * @return {@code 200 OK} con la URL de descarga del archivo convertido como texto
     *         plano; {@code 400 Bad Request} si el tipo de archivo o el formato son
     *         inválidos, o si el MIME del archivo no coincide con el tipo declarado;
     *         {@code 401 Unauthorized} si el token JWT está ausente o ha expirado
     */
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

    /**
     * Devuelve el conjunto de formatos de destino soportados para un tipo de archivo dado.
     *
     * <p>Este endpoint es <b>público</b> y no requiere autenticación. Está diseñado
     * para que el frontend pueda consultar dinámicamente las opciones del desplegable
     * de formato antes de que el usuario seleccione el archivo a convertir.</p>
     *
     * <p>Ejemplos de respuesta por tipo:</p>
     * <ul>
     *   <li>{@code AUDIO}: {@code {mp3, wav, aac, flac, ogg, m4a}}</li>
     *   <li>{@code VIDEO}: {@code {mp4, mkv, avi, mov, webm, flv}}</li>
     *   <li>{@code IMAGEN}: {@code {jpg, png, webp, gif, bmp, tiff}}</li>
     * </ul>
     *
     * @param tipoArchivo categoría del archivo en mayúsculas: {@code AUDIO},
     *                    {@code VIDEO} o {@code IMAGEN}
     * @return {@code 200 OK} con un {@link Set} de extensiones de formato disponibles;
     *         {@code 400 Bad Request} si el valor de {@code tipoArchivo} no corresponde
     *         a ninguna categoría válida
     */
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