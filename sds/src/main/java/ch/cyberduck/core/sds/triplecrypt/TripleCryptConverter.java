package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicKeyContainer;

import eu.ssp_europe.sds.crypto.model.EncryptedFileKey;
import eu.ssp_europe.sds.crypto.model.PlainFileKey;
import eu.ssp_europe.sds.crypto.model.UserPublicKey;

public class TripleCryptConverter {
    public static FileKey toSwaggerFileKey(final EncryptedFileKey k) {
        return new FileKey().key(k.getKey()).iv(k.getIv()).tag(k.getTag()).version(k.getVersion());
    }

    public static FileKey toSwaggerFileKey(final PlainFileKey k) {
        return new FileKey().key(k.getKey()).iv(k.getIv()).tag(k.getTag()).version(k.getVersion());
    }

    public static UserPublicKey toCryptoUserPublicKey(final PublicKeyContainer c) {
        final UserPublicKey key = new UserPublicKey();
        key.setPublicKey(c.getPublicKey());
        key.setVersion(c.getVersion());
        return key;
    }

    public static PlainFileKey toCryptoPlainFileKey(final FileKey key) {
        final PlainFileKey fileKey = new PlainFileKey();
        fileKey.setKey(key.getKey());
        fileKey.setIv(key.getIv());
        fileKey.setTag(key.getTag());
        fileKey.setVersion(key.getVersion());
        return fileKey;
    }

    public static EncryptedFileKey toCryptoEncryptedFileKey(final FileKey k) {
        final EncryptedFileKey key = new EncryptedFileKey();
        key.setKey(k.getKey());
        key.setIv(k.getIv());
        key.setTag(k.getTag());
        key.setVersion(k.getVersion());
        return key;
    }
}
