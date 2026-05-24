package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para {@link AESUtil}.
 * Verifica encriptación/desencriptación AES-GCM y los métodos de hasheo.
 */
@DisplayName("AESUtil - Encriptación y Hasheo")
class AESUtilTest {

    

    @Test
    @DisplayName("encrypt(texto) -> decrypt(cifrado) debe devolver el texto original")
    void encryptDecryptRoundtripDefault() {
        String original = "HolaMundo123";
        String cifrado = AESUtil.encrypt(original);
        assertNotNull(cifrado, "El texto cifrado no debe ser nulo");
        assertNotEquals(original, cifrado, "El texto cifrado debe diferir del original");
        assertEquals(original, AESUtil.decrypt(cifrado));
    }

    @Test
    @DisplayName("encrypt/decrypt con llave e IV explícitos")
    void encryptDecryptRoundtripExplicito() {
        String llave    = "1234567890123456"; // 16 chars
        String iv       = "abcdefghijklmnop"; // 16 chars
        String original = "TextoSecreto";
        String cifrado  = AESUtil.encrypt(llave, iv, original);
        assertEquals(original, AESUtil.decrypt(llave, iv, cifrado));
    }

    @Test
    @DisplayName("Con IV fijo el cifrado es determinista para la misma entrada")
    void encryptMismoTextoMismoResultado() {
        String original = "DatoRepetido";
        String c1 = AESUtil.encrypt(original);
        String c2 = AESUtil.encrypt(original);
        assertEquals(c1, c2, "Con IV fijo el cifrado debe ser determinista");
    }

    @Test
    @DisplayName("encrypt con cadena vacía no lanza excepción")
    void encryptCadenaVacia() {
        assertDoesNotThrow(() -> {
            String cifrado = AESUtil.encrypt("");
            assertNotNull(cifrado);
        });
    }

    @Test
    @DisplayName("encrypt produce texto diferente al original")
    void encryptProduceTextoDiferente() {
        String original = "TextoParaVerificar";
        String cifrado  = AESUtil.encrypt(original);
        assertNotNull(cifrado);
        assertNotEquals(original, cifrado);
       
        assertEquals(original, AESUtil.decrypt(cifrado));
    }

    

    @Test
    @DisplayName("hashingToMD5 devuelve cadena hexadecimal de 32 chars")
    void hashingMD5Longitud() {
        String hash = AESUtil.hashingToMD5("test");
        assertNotNull(hash);
        assertEquals(32, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"), "MD5 debe ser hexadecimal en minúsculas");
    }

    @Test
    @DisplayName("hashingToMD5 es determinista para la misma entrada")
    void hashingMD5Determinista() {
        assertEquals(AESUtil.hashingToMD5("abc"), AESUtil.hashingToMD5("abc"));
    }

    @Test
    @DisplayName("hashingToMD5 produce resultados distintos para entradas distintas")
    void hashingMD5Colision() {
        assertNotEquals(AESUtil.hashingToMD5("abc"), AESUtil.hashingToMD5("ABC"));
    }

    

    @Test
    @DisplayName("hashingToSHA1 devuelve cadena hexadecimal de 40 chars")
    void hashingSHA1Longitud() {
        String hash = AESUtil.hashingToSHA1("test");
        assertEquals(40, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("hashingToSHA1 es determinista")
    void hashingSHA1Determinista() {
        assertEquals(AESUtil.hashingToSHA1("hello"), AESUtil.hashingToSHA1("hello"));
    }

    

    @Test
    @DisplayName("hashingToSHA256 devuelve cadena hexadecimal de 64 chars")
    void hashingSHA256Longitud() {
        String hash = AESUtil.hashingToSHA256("test");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("hashingToSHA256 es determinista")
    void hashingSHA256Determinista() {
        assertEquals(AESUtil.hashingToSHA256("data"), AESUtil.hashingToSHA256("data"));
    }

    @Test
    @DisplayName("hashingToSHA256 difiere de SHA-1 y MD5 para la misma entrada")
    void hashingSHA256DifereDeMD5ySHA1() {
        String input = "mismoDato";
        assertNotEquals(AESUtil.hashingToMD5(input),  AESUtil.hashingToSHA256(input));
        assertNotEquals(AESUtil.hashingToSHA1(input), AESUtil.hashingToSHA256(input));
    }

   

    @Test
    @DisplayName("hashingToSHA384 devuelve cadena hexadecimal de 96 chars")
    void hashingSHA384Longitud() {
        assertEquals(96, AESUtil.hashingToSHA384("test").length());
    }

    @Test
    @DisplayName("hashingToSHA384 es determinista")
    void hashingSHA384Determinista() {
        assertEquals(AESUtil.hashingToSHA384("x"), AESUtil.hashingToSHA384("x"));
    }

   

    @Test
    @DisplayName("hashingToSHA512 devuelve cadena hexadecimal de 128 chars")
    void hashingSHA512Longitud() {
        assertEquals(128, AESUtil.hashingToSHA512("test").length());
    }

    @Test
    @DisplayName("hashingToSHA512 es determinista")
    void hashingSHA512Determinista() {
        assertEquals(AESUtil.hashingToSHA512("seguro"), AESUtil.hashingToSHA512("seguro"));
    }

    @Test
    @DisplayName("Los cinco algoritmos producen hashes distintos para la misma entrada")
    void todosAlgoritmosDiferentes() {
        String input  = "proyectofinal";
        String md5    = AESUtil.hashingToMD5(input);
        String sha1   = AESUtil.hashingToSHA1(input);
        String sha256 = AESUtil.hashingToSHA256(input);
        String sha384 = AESUtil.hashingToSHA384(input);
        String sha512 = AESUtil.hashingToSHA512(input);

        assertNotEquals(md5,    sha1);
        assertNotEquals(sha1,   sha256);
        assertNotEquals(sha256, sha384);
        assertNotEquals(sha384, sha512);
    }
}