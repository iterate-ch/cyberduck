package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes a hash using the same algorithm that the Dropbox API uses for the
 * the "content_hash" metadata field.
 *
 * <p>
 * The {@link #digest()} method returns a raw binary representation of the hash.
 * The "content_hash" field in the Dropbox API is a hexadecimal-encoded version
 * of the digest.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * MessageDigest hasher = new DropboxContentHasher();
 * byte[] buf = new byte[1024];
 * InputStream in = new FileInputStream("some-file");
 * try {
 *     while (true) {
 *         int n = in.read(buf);
 *         if (n &lt; 0) break;  // EOF
 *         hasher.update(buf, 0, n);
 *     }
 * }
 * finally {
 *     in.close();
 * }
 *
 * byte[] rawHash = hasher.digest();
 * System.out.println(hex(rawHash));
 *     // Assuming 'hex' is a method that converts a byte[] to
 *     // a hexadecimal-encoded String
 * </pre>
 *
 * <p>
 * If you need to hash something as it passes through a stream, you can use the
 * {@link java.security.DigestInputStream} or {@code java.security.DigestOutputStream} helpers.
 * </p>
 *
 * <pre>
 * MessageDigest hasher = new DropboxContentHasher();
 * InputStream in = new FileInputStream("some-file");
 * UploadResponse r;
 * try {
 *     r = someApiClient.upload(new DigestInputStream(in, hasher)));
 * }
 * finally {
 *     in.close();
 * }
 *
 * String locallyComputed = hex(hasher.digest());
 * assert r.contentHash.equals(locallyComputed);
 * </pre>
 */
public final class DropboxContentHasher extends MessageDigest implements Cloneable
{
    private MessageDigest overallHasher;
    private MessageDigest blockHasher;
    private int blockPos = 0;

    public static final int BLOCK_SIZE = 4 * 1024 * 1024;

    public DropboxContentHasher()
    {
        this(newSha256Hasher(), newSha256Hasher(), 0);
    }

    public DropboxContentHasher(MessageDigest overallHasher, MessageDigest blockHasher, int blockPos)
    {
        super("Dropbox-Content-Hash");
        this.overallHasher = overallHasher;
        this.blockHasher = blockHasher;
        this.blockPos = blockPos;
    }

    @Override
    protected void engineUpdate(byte input)
    {
        finishBlockIfFull();

        blockHasher.update(input);
        blockPos += 1;
    }

    @Override
    protected int engineGetDigestLength()
    {
        return overallHasher.getDigestLength();
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len)
    {
        int inputEnd = offset + len;
        while (offset < inputEnd) {
            finishBlockIfFull();

            int spaceInBlock = BLOCK_SIZE - this.blockPos;
            int inputPartEnd = Math.min(inputEnd, offset+spaceInBlock);
            int inputPartLength = inputPartEnd - offset;
            blockHasher.update(input, offset, inputPartLength);

            blockPos += inputPartLength;
            offset += inputPartLength;
        }
    }

    @Override
    protected void engineUpdate(ByteBuffer input)
    {
        int inputEnd = input.limit();
        while (input.position() < inputEnd) {
            finishBlockIfFull();

            int spaceInBlock = BLOCK_SIZE - this.blockPos;
            int inputPartEnd = Math.min(inputEnd, input.position()+spaceInBlock);
            int inputPartLength = inputPartEnd - input.position();
            input.limit(inputPartEnd);
            blockHasher.update(input);

            blockPos += inputPartLength;
            input.position(inputPartEnd);
        }
    }

    @Override
    protected byte[] engineDigest()
    {
        finishBlockIfNonEmpty();
        return overallHasher.digest();
    }

    @Override
    protected int engineDigest(byte[] buf, int offset, int len)
            throws DigestException
    {
        finishBlockIfNonEmpty();
        return overallHasher.digest(buf, offset, len);
    }

    @Override
    protected void engineReset()
    {
        this.overallHasher.reset();
        this.blockHasher.reset();
        this.blockPos = 0;
    }

    @Override
    public DropboxContentHasher clone()
            throws CloneNotSupportedException
    {
        DropboxContentHasher clone = (DropboxContentHasher) super.clone();
        clone.overallHasher = (MessageDigest) clone.overallHasher.clone();
        clone.blockHasher = (MessageDigest) clone.blockHasher.clone();
        return clone;
    }

    private void finishBlock()
    {
        overallHasher.update(blockHasher.digest());
        blockPos = 0;
    }

    private void finishBlockIfFull()
    {
        if (blockPos == BLOCK_SIZE) {
            finishBlock();
        }
    }

    private void finishBlockIfNonEmpty()
    {
        if (blockPos > 0) {
            finishBlock();
        }
    }

    static MessageDigest newSha256Hasher()
    {
        try {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new AssertionError("Couldn't create SHA-256 hasher");
        }
    }
}