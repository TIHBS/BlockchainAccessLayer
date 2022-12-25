package blockchains.iaas.uni.stuttgart.de;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class Utils {
    private static final String SPEC = "secp256k1";
    private static final String ALGO = "SHA256withECDSA";

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static boolean ValidateSignature(String plaintext, String signer,
                                            byte[] signature) throws SignatureException,
            InvalidKeyException, UnsupportedEncodingException,
            NoSuchAlgorithmException {
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");

        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(signer));
        try {
            temp();
//            ObjectNode s = Utils.sender();
//
//            boolean res = Utils.receiver(s);
//            log.debug("Signature result {}", res);
        } catch (Exception e) {
            log.error("Unable to validate signature", e);
        }

        return false;
//        KeyFactory keyFactory = KeyFactory.getInstance("EC");
//        PublicKey publicKey = null;
//        try {
//            publicKey = keyFactory.generatePublic(publicKeySpec);
//            //ecdsaVerify.initVerify(publicKey);
//            // ecdsaVerify.update(plaintext.getBytes("UTF-8"));
//            ///  return ecdsaVerify.verify(Base64.getDecoder().decode(signature));
//
//            ecdsaVerify.initVerify(publicKey);
//            ecdsaVerify.update(plaintext.getBytes("UTF-8"));
//            return ecdsaVerify.verify(Hex.decode(signature));
//        } catch (InvalidKeySpecException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static ObjectNode sender() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, SignatureException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        ECGenParameterSpec ecSpec = new ECGenParameterSpec(SPEC);
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(ecSpec, new SecureRandom());
        KeyPair keypair = g.generateKeyPair();
        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        String plaintext = "Hello";

        //...... sign
        Signature ecdsaSign = Signature.getInstance(ALGO);
        ecdsaSign.initSign(privateKey);
        ecdsaSign.update(plaintext.getBytes("UTF-8"));
        byte[] signature = ecdsaSign.sign();
        String pub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String sig = Base64.getEncoder().encodeToString(signature);
        System.out.println(sig);
        System.out.println(pub);

        rootNode.put("publicKey", pub);
        rootNode.put("signature", sig);
        rootNode.put("message", plaintext);
        rootNode.put("algorithm", ALGO);

        return rootNode;
    }

    public static boolean receiver(ObjectNode obj) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException, SignatureException {

        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
        KeyFactory kf = KeyFactory.getInstance("EC");

        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(String.valueOf(obj.get("publicKey"))));

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(String.valueOf(obj.get("message")).getBytes("UTF-8"));
        boolean result = ecdsaVerify.verify(Base64.getDecoder().decode(String.valueOf(obj.get("signature"))));

        return result;
    }

    public static void temp() {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("EC");

            keyGen.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());

            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();

            /*
             * Create a Signature object and initialize it with the private key
             */

            Signature ecdsa = Signature.getInstance("SHA256withECDSA");

            ecdsa.initSign(priv);

            String str = "This is string to sign";
            byte[] strByte = str.getBytes("UTF-8");
            ecdsa.update(strByte);

            /*
             * Now that all the data to be signed has been read in, generate a
             * signature for it
             */

            byte[] realSig = ecdsa.sign();
            String sig = new BigInteger(1, realSig).toString(16);
            log.debug("Signature: " + sig);

            String publicKeyHex = bytesToHex(pair.getPublic().getEncoded());

            verfiy(publicKeyHex, str, sig, pair.getPublic());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean verfiy(String signer, String text, String signature, PublicKey pk) {
        KeyFactory keyFactory = null;
        try {

            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");

            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodeUsingBigInteger(signer));

            keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = null;
            try {
                publicKey = keyFactory.generatePublic(publicKeySpec);
                ecdsaVerify.initVerify(publicKey);
                ecdsaVerify.update(text.getBytes("UTF-8"));
                boolean temp = ecdsaVerify.verify(Hex.decode(signature));
                log.debug("Temp %s", temp);
                ecdsaVerify.initVerify(pk);
                ecdsaVerify.update(text.getBytes("UTF-8"));

                boolean res = ecdsaVerify.verify(Hex.decode(signature));
                log.debug("Is valid %s", res);

                return res;

            } catch (SignatureException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] decodeUsingBigInteger(String hexString) {
        byte[] byteArray = new BigInteger(hexString, 16)
                .toByteArray();
        if (byteArray[0] == 0) {
            byte[] output = new byte[byteArray.length - 1];
            System.arraycopy(
                    byteArray, 1, output,
                    0, output.length);
            return output;
        }
        return byteArray;
    }

}
