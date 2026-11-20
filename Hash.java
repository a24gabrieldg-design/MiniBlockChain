import java.math.BigInteger;
import java.security.MessageDigest;

public class Hash {

    // Devuelve el SHA-256 de un texto en hexadecimal (64 caracteres)
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes("UTF-8"));
            return String.format("%064x", new BigInteger(1, bytes));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular el hash", e);
        }
    }
}