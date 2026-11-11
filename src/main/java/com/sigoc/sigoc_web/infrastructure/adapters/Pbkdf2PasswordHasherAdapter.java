package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.ports.out.PasswordHasherPort;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash PBKDF2-HMAC-SHA256 compatible con el formato del desktop:
 * {@code PBKDF2$<rondas>$<salBase64>$<hashBase64>}.
 */
@Component
public class Pbkdf2PasswordHasherAdapter implements PasswordHasherPort {

    private static final int RONDAS = 120_000;
    private static final int BITS = 256;
    /** Sal de la cuenta inicial sembrada en sigoc_datos_iniciales.sql. */
    private static final String SAL_CLAVE_INICIAL = "U0lHT0MtREVNT1NUUkFDSU9OLVNBTFQ=";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String clave) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] result = derivar(clave, salt, RONDAS);
        Base64.Encoder enc = Base64.getEncoder();
        return "PBKDF2$" + RONDAS + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(result);
    }

    @Override
    public boolean verificar(String clave, String almacenada) {
        if (clave == null || almacenada == null || !almacenada.startsWith("PBKDF2$")) {
            return false;
        }
        String[] p = almacenada.split("\\$", -1);
        if (p.length != 4) {
            return false;
        }
        try {
            int rondas = Integer.parseInt(p[1]);
            if (rondas < 10_000 || rondas > 1_000_000) {
                return false;
            }
            byte[] esperado = Base64.getDecoder().decode(p[3]);
            byte[] actual = derivar(clave, Base64.getDecoder().decode(p[2]), rondas);
            return MessageDigest.isEqual(esperado, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean esClaveInicial(String almacenada) {
        return almacenada != null && almacenada.contains(SAL_CLAVE_INICIAL);
    }

    private byte[] derivar(String clave, byte[] salt, int rondas) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), salt, rondas, BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", ex);
        }
    }
}
