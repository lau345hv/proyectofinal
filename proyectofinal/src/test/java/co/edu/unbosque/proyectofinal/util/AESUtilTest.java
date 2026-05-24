package co.edu.unbosque.proyectofinal.util;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utilidad de encriptación y hasheo para la plataforma de conversión de archivos.
 * <p>
 * Provee dos tipos de operaciones criptográficas:
 * </p>
 * <ul>
 *   <li><b>Encriptación simétrica AES/GCM/NoPadding:</b> permite cifrar y
 *       descifrar texto usando una llave e IV (vector de inicialización) de
 *       16 caracteres. El resultado se codifica en Base64 para ser
 *       transportable como texto plano.</li>
 *   <li><b>Hasheo unidireccional:</b> genera resúmenes criptográficos de
 *       un texto usando MD5, SHA-1, SHA-256, SHA-384 o SHA-512. Útil para
 *       verificar integridad o almacenar datos sensibles sin posibilidad
 *       de reversión.</li>
 * </ul>
 * <p>
 * Los métodos de conveniencia {@link #encrypt(String)} y
 * {@link #decrypt(String)} usan una llave e IV predeterminados.
 * Para mayor seguridad en producción, use las sobrecargas que reciben
 * llave e IV explícitos.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class AESUtilTest {

	/** Algoritmo base para la construcción de la clave secreta. */
	private static final String ALGORITMO = "AES";

	/** Modo de cifrado: AES con GCM y sin padding. */
	private static final String TIPO_CIFRADO = "AES/GCM/NoPadding";

	/** IV predeterminado (16 caracteres = 128 bits). */
	private static final String IV_DEFAULT = "programacioncomp";

	/** Llave predeterminada (16 caracteres = 128 bits). */
	private static final String KEY_DEFAULT = "llavede16carater";

	private AESUtilTest() {
	}

	/**
	 * Encripta un texto plano usando AES/GCM/NoPadding con la llave e IV
	 * proporcionados. El resultado se devuelve en Base64.
	 *
	 * @param llave  clave secreta de exactamente 16 caracteres (128 bits).
	 * @param iv     vector de inicialización de exactamente 16 caracteres.
	 * @param texto  texto plano a encriptar.
	 * @return texto encriptado codificado en Base64, o cadena vacía si falla.
	 */
	public static String encrypt(String llave, String iv, String texto) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TIPO_CIFRADO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		byte[] encrypted = null;
		try {
			encrypted = cipher.doFinal(texto.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}

		return new String(encodeBase64(encrypted));
	}

	/**
	 * Desencripta un texto previamente encriptado con {@link #encrypt(String, String, String)}.
	 * <p>
	 * Si la llave o el IV son incorrectos, AES/GCM detecta el fallo de
	 * autenticación (AEADBadTagException) y devuelve cadena vacía en lugar
	 * de propagar la excepción.
	 * </p>
	 *
	 * @param llave     clave secreta de exactamente 16 caracteres (128 bits).
	 * @param iv        vector de inicialización de exactamente 16 caracteres.
	 * @param encrypted texto encriptado en Base64.
	 * @return texto plano original, o cadena vacía si falla la autenticación.
	 */
	public static String decrypt(String llave, String iv, String encrypted) {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TIPO_CIFRADO);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		byte[] enc = decodeBase64(encrypted);
		try {
			
			byte[] decrypted = cipher.doFinal(enc);
			return new String(decrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Desencripta usando la llave e IV predeterminados.
	 *
	 * @param encrypted texto encriptado en Base64.
	 * @return texto plano original.
	 */
	public static String decrypt(String encrypted) {
		return decrypt(KEY_DEFAULT, IV_DEFAULT, encrypted);
	}

	/**
	 * Encripta usando la llave e IV predeterminados.
	 *
	 * @param plainText texto plano a encriptar.
	 * @return texto encriptado en Base64.
	 */
	public static String encrypt(String plainText) {
		return encrypt(KEY_DEFAULT, IV_DEFAULT, plainText);
	}

	/**
	 * Genera un hash MD5 del contenido dado.
	 * <p>
	 * <b>Nota:</b> MD5 no es apto para almacenar contraseñas; úsalo solo
	 * para verificación de integridad de datos no sensibles.
	 * </p>
	 *
	 * @param content texto a hashear.
	 * @return hash MD5 en hexadecimal.
	 */
	public static String hashingToMD5(String content) {
		return DigestUtils.md5Hex(content);
	}

	/**
	 * Genera un hash SHA-1 del contenido dado.
	 *
	 * @param content texto a hashear.
	 * @return hash SHA-1 en hexadecimal.
	 */
	public static String hashingToSHA1(String content) {
		return DigestUtils.sha1Hex(content);
	}

	/**
	 * Genera un hash SHA-256 del contenido dado.
	 *
	 * @param content texto a hashear.
	 * @return hash SHA-256 en hexadecimal.
	 */
	public static String hashingToSHA256(String content) {
		return DigestUtils.sha256Hex(content);
	}

	/**
	 * Genera un hash SHA-384 del contenido dado.
	 *
	 * @param content texto a hashear.
	 * @return hash SHA-384 en hexadecimal.
	 */
	public static String hashingToSHA384(String content) {
		return DigestUtils.sha384Hex(content);
	}

	/**
	 * Genera un hash SHA-512 del contenido dado.
	 *
	 * @param content texto a hashear.
	 * @return hash SHA-512 en hexadecimal.
	 */
	public static String hashingToSHA512(String content) {
		return DigestUtils.sha512Hex(content);
	}
}