package com.control.chavedigital;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Encryption
{
    // Some random salt
    private static final byte[]	SALT = { (byte) 0x21, (byte) 0x21, (byte) 0xF0, (byte) 0x55, (byte) 0xC3, (byte) 0x9F, (byte) 0x5A, (byte) 0x75	};

    private final static int ITERATION_COUNT = 31;

    Encryption()
    {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encode(String input)
    {
        if (input == null)
        {
            throw new IllegalArgumentException();
        }
        try
        {
            KeySpec keySpec = new PBEKeySpec(null, SALT, ITERATION_COUNT);
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

            Cipher ecipher = Cipher.getInstance(key.getAlgorithm());
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

            byte[] enc = ecipher.doFinal(input.getBytes());

            String res = new String(Base64.getEncoder().encode(enc));
            // escapes for url
            res = res.replace('+', '-').replace('/', '_').replace("%", "%25").replace("\n", "%0A");

            return res;

        }
        catch (Exception ignored)
        {
        }

        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decode(String token)
    {
        if (token == null)
        {
            return null;
        }
        try
        {
            String input = token.replace("%0A", "\n").replace("%25", "%").replace('_', '/').replace('-', '+');
            byte[] dec = Base64.getDecoder().decode(input.getBytes());

            KeySpec keySpec = new PBEKeySpec(null, SALT, ITERATION_COUNT);
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

            Cipher dcipher = Cipher.getInstance(key.getAlgorithm());
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

            byte[] decoded = dcipher.doFinal(dec);

            return new String(decoded);
        }
        catch (Exception e)
        {
            // use logger in production code
            e.printStackTrace();
        }

        return null;
    }

}
