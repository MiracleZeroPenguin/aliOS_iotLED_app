package com.groundspace.lampmqtt;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static String encryptHMAC(String signMethod, String content, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes("utf-8"), signMethod);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        byte[] data = mac.doFinal(content.getBytes("utf-8"));
        return bytesToHexString(data);
    }
}
