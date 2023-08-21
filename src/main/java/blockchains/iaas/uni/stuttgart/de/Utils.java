/********************************************************************************
 * Copyright (c) 2023 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de;

import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.Base64;
import com.starkbank.ellipticcurve.utils.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static boolean ValidateSignature(String text, String signer,
                                            String signature) {
        try {

            return verifySignature(signer, signature, text);
        } catch (Exception e) {
            log.error("Unable to validate signature {}", signature);
            return false;
        }
    }

    public static boolean verifySignature(String pubKey, String base64EncodedSignature, String message) {
        try {
            byte[] bytessSig = Base64.decode(base64EncodedSignature);
            ByteString byteString = new ByteString(bytessSig);
            Signature signature = Signature.fromDer(byteString);

            byte[] bytessPub = Base64.decode(pubKey);
            ByteString b = new ByteString(bytessPub);
            PublicKey publicKey = PublicKey.fromDer(b);

            boolean verified = Ecdsa.verify(message, signature, publicKey);
            return verified;
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
