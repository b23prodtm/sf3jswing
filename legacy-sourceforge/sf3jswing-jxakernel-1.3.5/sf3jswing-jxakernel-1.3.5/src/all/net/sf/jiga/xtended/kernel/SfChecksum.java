/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Checksum;

/**
 *
 * @author www.b23prodtm.info
 */
public abstract class SfChecksum implements Checksum {

    MessageDigest md;

    public SfChecksum(String algorithm) {
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            if (JXAenvUtils._debugSys) {
                ex.printStackTrace();
            }
        }
    }
    boolean altered = false;
    long sum = 0;

    public void update(int b) {
        md.update((byte) b);
        altered = true;
    }

    public void update(byte[] b, int off, int len) {
        md.update(b, off, len);
        altered = true;
    }

    public long getValue() {
        sum = altered ? decodeLong(md.digest()) : sum;
        altered = false;
        return sum;
    }

    public void reset() {
        md.reset();
        altered = true;
    }
    
    public static long decodeLong(byte[] digestValue) {
        return new BigInteger(1, digestValue).longValue();
    }
}
