/*
*  Copyright (c) 2000 The Legion Of The Bouncy Castle http://www.bouncycastle.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 *  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 *
 */
package com.sshtools.ext.bouncycastle.cipher;

import com.sshtools.j2ssh.transport.AlgorithmOperationException;
import com.sshtools.j2ssh.transport.cipher.SshCipher;

public class CbcBlockCipher extends SshCipher {
    private CipherEngine engine;
    private int mode;
    private byte[] ivStart = null;
    private byte[] ivBlock = null;
    private byte[] xorBlock = null;
    private byte[] key = null;

    public CbcBlockCipher(int keybits, CipherEngine engine) {
        key = new byte[keybits / 8];
        this.engine = engine;
    }

    public void init(int mode, byte[] iv, byte[] keydata) throws
            AlgorithmOperationException {
        this.mode = mode;
        // Copy keydata into the correct size key
        System.arraycopy(keydata, 0, key, 0, key.length);
        // Initiate the engine
        engine.init(mode == ENCRYPT_MODE, key);
        // Setup the IV
        ivStart = new byte[getBlockSize()];
        System.arraycopy(iv, 0, ivStart, 0, ivStart.length);
        ivBlock = (byte[]) ivStart.clone();
        xorBlock = new byte[getBlockSize()];
    }

    public int getBlockSize() {
        return engine.getBlockSize();
    }

    public byte[] transform(byte[] in, int offset, int len) throws
            AlgorithmOperationException {
        if (ivBlock == null) {
            throw new AlgorithmOperationException("Cipher not initialized!");
        }
        if ((len % getBlockSize()) != 0) {
            throw new AlgorithmOperationException("Input data length MUST be a multiple of the cipher block size!");
        }
        byte[] output = new byte[len];
        for (int pos = 0; pos < len; pos += getBlockSize()) {
            switch (mode) {
                case ENCRYPT_MODE:
                    {
                        for (int i = 0; i < getBlockSize(); i++) {
                            xorBlock[i] = (byte) (in[offset + pos + i] ^ ivBlock[i]);
                        }
                        engine.processBlock(xorBlock, 0, ivBlock, 0);
                        System.arraycopy(ivBlock, 0, output, pos, getBlockSize());
                        break;
                    }
                case DECRYPT_MODE:
                    {
                        byte[] ivTmp = new byte[getBlockSize()];
                        System.arraycopy(in, pos, ivTmp, 0, getBlockSize());
                        engine.processBlock(in, offset + pos, xorBlock, 0);
                        for (int i = 0; i < getBlockSize(); i++) {
                            output[i + pos] = (byte) (xorBlock[i] ^ ivBlock[i]);
                        }
                        System.arraycopy(ivTmp, 0, ivBlock, 0, getBlockSize());
                        ivTmp = null;
                        break;
                    }
                default:
                    throw new AlgorithmOperationException("Invalid cipher mode!");
            }
        }
        return output;
    }
}
