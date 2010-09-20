package org.kohsuke.putty;

import ch.ethz.ssh2.crypto.Base64;
import ch.ethz.ssh2.crypto.cipher.AES;
import ch.ethz.ssh2.crypto.cipher.CBCMode;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Interprets PuTTY's ".ppk" file.
 * <p/>
 * <h2>Notes</h2>
 * <ol>
 * <li>
 * The file appears to be a text file but it doesn't have the fixed encoding.
 * So we just use the platform default encoding, which is what PuTTY seems to use.
 * Fortunately, the important part is all ASCII, so this shouldn't really hurt
 * the interpretation of the key.
 * </ol>
 * <p/>
 * <h2>Sample PuTTY file format</h2>
 * <pre>
 * PuTTY-User-Key-File-2: ssh-rsa
 * Encryption: none
 * Comment: rsa-key-20080514
 * Public-Lines: 4
 * AAAAB3NzaC1yc2EAAAABJQAAAIEAiPVUpONjGeVrwgRPOqy3Ym6kF/f8bltnmjA2
 * BMdAtaOpiD8A2ooqtLS5zWYuc0xkW0ogoKvORN+RF4JI+uNUlkxWxnzJM9JLpnvA
 * HrMoVFaQ0cgDMIHtE1Ob1cGAhlNInPCRnGNJpBNcJ/OJye3yt7WqHP4SPCCLb6nL
 * nmBUrLM=
 * Private-Lines: 8
 * AAAAgGtYgJzpktzyFjBIkSAmgeVdozVhgKmF6WsDMUID9HKwtU8cn83h6h7ug8qA
 * hUWcvVxO201/vViTjWVz9ALph3uMnpJiuQaaNYIGztGJBRsBwmQW9738pUXcsUXZ
 * 79KJP01oHn6Wkrgk26DIOsz04QOBI6C8RumBO4+F1WdfueM9AAAAQQDmA4hcK8Bx
 * nVtEpcF310mKD3nsbJqARdw5NV9kCxPnEsmy7Sy1L4Ob/nTIrynbc3MA9HQVJkUz
 * 7V0va5Pjm/T7AAAAQQCYbnG0UEekwk0LG1Hkxh1OrKMxCw2KWMN8ac3L0LVBg/Tk
 * 8EnB2oT45GGeJaw7KzdoOMFZz0iXLsVLNUjNn2mpAAAAQQCN6SEfWqiNzyc/w5n/
 * lFVDHExfVUJp0wXv+kzZzylnw4fs00lC3k4PZDSsb+jYCMesnfJjhDgkUA0XPyo8
 * Emdk
 * Private-MAC: 50c45751d18d74c00fca395deb7b7695e3ed6f77
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
public class PuTTYKey {
    private static final String PUTTY_SIGNATURE = "PuTTY-User-Key-File-";

    private byte[] privateKey;
    private byte[] publicKey;

    private Map<String, String> payload
            = new HashMap<String, String>();

    /**
     * For each line that looks like "Xyz: vvv", it will be stored in this map.
     */
    private final Map<String, String> headers
            = new HashMap<String, String>();

    public PuTTYKey(File ppkFile) throws IOException {
        this(new FileReader(ppkFile));
    }

    public PuTTYKey(InputStream in) throws IOException {
        this(new InputStreamReader(in));
    }

    public PuTTYKey(Reader in) throws IOException {
        BufferedReader r = new BufferedReader(in);
        // Parse the text into headers and payloads
        try {
            String headerName = null;
            String line;
            while((line = r.readLine()) != null) {
                int idx = line.indexOf(": ");
                if(idx > 0) {
                    headerName = line.substring(0, idx);
                    headers.put(headerName, line.substring(idx + 2));
                }
                else {
                    String s = payload.get(headerName);
                    if(s == null) {
                        s = line;
                    }
                    else {
                        s += line;
                    }
                    payload.put(headerName, s);
                }
            }
        }
        finally {
            r.close();
        }
        publicKey = decodeBase64(payload.get("Public-Lines"));
        privateKey = decodeBase64(payload.get("Private-Lines"));
    }

    public boolean isEncrypted() {
        return "aes256-cbc".equals(headers.get("Encryption"));
    }

    /**
     * Decrypt private key
     *
     * @param passphrase
     */
    public void decrypt(String passphrase) throws IOException {
        if(this.isEncrypted()) {
            AES aes = new AES();
            byte[] key = toKey(passphrase);
            aes.init(false, key);
            CBCMode cbc = new CBCMode(aes, new byte[16], false); // initial vector=0
            byte[] out = new byte[privateKey.length];
            for(int i = 0; i < privateKey.length / cbc.getBlockSize(); i++) {
                cbc.transformBlock(privateKey, i * cbc.getBlockSize(), out, i * cbc.getBlockSize());
            }
            privateKey = out;
        }
    }

    /**
     * Key type. Either "ssh-rsa" for RSA key, or "ssh-dss" for DSA key.
     */
    public String getAlgorithm() {
        return headers.get("PuTTY-User-Key-File-2");
    }

    /**
     * Converts a passphrase into a key, by following the convention that PuTTY uses.
     * <p/>
     * <p/>
     * This is used to decrypt the private key when it's encrypted.
     */
    private byte[] toKey(String passphrase) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            digest.update(new byte[]{0, 0, 0, 0});
            digest.update(passphrase.getBytes());
            byte[] key1 = digest.digest();

            digest.update(new byte[]{0, 0, 0, 1});
            digest.update(passphrase.getBytes());
            byte[] key2 = digest.digest();

            byte[] r = new byte[32];
            System.arraycopy(key1, 0, r, 0, 20);
            System.arraycopy(key2, 0, r, 20, 12);

            return r;
        }
        catch(NoSuchAlgorithmException e) {
            throw new AssertionError(e);    // impossible
        }
    }

    private static byte[] decodeBase64(String s) throws IOException {
        return Base64.decode(s.toCharArray());
    }

    /**
     * Converts this key into OpenSSH format.
     *
     * @return A multi-line string that can be written back to a file.
     */
    public String toOpenSSH() throws IOException {
        if(getAlgorithm().equals("ssh-rsa")) {
            KeyReader r = new KeyReader(publicKey);
            r.skip();   // skip this
            BigInteger e = r.readInt();
            BigInteger n = r.readInt();

            r = new KeyReader(privateKey);
            BigInteger d = r.readInt();
            BigInteger p = r.readInt();
            BigInteger q = r.readInt();
            BigInteger iqmp = r.readInt();

            BigInteger dmp1 = d.mod(p.subtract(BigInteger.ONE));
            BigInteger dmq1 = d.mod(q.subtract(BigInteger.ONE));


            DEREncoder payload = new DEREncoder().writeSequence(
                    new DEREncoder().write(BigInteger.ZERO, n, e, d, p, q, dmp1, dmq1, iqmp).toByteArray()
            );

            StringBuilder buf = new StringBuilder();
            buf.append("-----BEGIN RSA PRIVATE KEY-----\n");
            buf.append(payload.toBase64());
            buf.append("-----END RSA PRIVATE KEY-----\n");

            // debug assist
            //        Object o = PEMDecoder.decode(buf.toString().toCharArray(), null);

            return buf.toString();
        }

        if(getAlgorithm().equals("ssh-dss")) {
            KeyReader r = new KeyReader(publicKey);
            r.skip();   // skip this
            BigInteger p = r.readInt();
            BigInteger q = r.readInt();
            BigInteger g = r.readInt();
            BigInteger y = r.readInt();

            r = new KeyReader(privateKey);
            BigInteger x = r.readInt();

            DEREncoder payload = new DEREncoder().writeSequence(
                    new DEREncoder().write(BigInteger.ZERO, p, q, g, y, x).toByteArray()
            );

            StringBuilder buf = new StringBuilder();
            buf.append("-----BEGIN DSA PRIVATE KEY-----\n");
            buf.append(payload.toBase64());
            buf.append("-----END DSA PRIVATE KEY-----\n");

            // debug assist
            //        Object o = PEMDecoder.decode(buf.toString().toCharArray(), null);

            return buf.toString();
        }

        throw new IllegalArgumentException("Unrecognized key type: " + getAlgorithm());
    }

    /**
     * Converts the key to OpenSSH format, then write it to a file.
     */
    public void toOpenSSH(File f) throws IOException {
        FileWriter w = new FileWriter(f);
        try {
            w.write(toOpenSSH());
        }
        finally {
            w.close();
        }
    }

    /**
     * Checks if the given file is a PuTTY's ".ppk" file, by looking at the file contents.
     */
    public static boolean isPuTTYKeyFile(File ppkFile) throws IOException {
        return isPuTTYKeyFile(new FileReader(ppkFile));
    }

    public static boolean isPuTTYKeyFile(InputStream in) throws IOException {
        return isPuTTYKeyFile(new InputStreamReader(in));
    }

    public static boolean isPuTTYKeyFile(Reader _reader) throws IOException {
        BufferedReader r = new BufferedReader(_reader);
        try {
            String line;
            while((line = r.readLine()) != null) {
                if(line.startsWith(PUTTY_SIGNATURE)) {
                    return true;
                }
            }
            return false;
        }
        finally {
            r.close();
        }
    }
}