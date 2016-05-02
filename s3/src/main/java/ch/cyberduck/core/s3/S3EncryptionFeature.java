package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Encryption;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class S3EncryptionFeature implements Encryption {

    private S3Session session;

    public S3EncryptionFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public List<String> getKeys(final LoginCallback prompt) throws BackgroundException {
        return Collections.emptyList();
    }

    @Override
    public List<String> getAlgorithms() {
        return Arrays.asList("AES256", "aws:kms");
    }

    @Override
    public String getEncryption(final Path file) throws BackgroundException {
        if(file.isFile()) {
            return new S3AttributesFeature(session).find(file).getEncryption();
        }
        return null;
    }

    @Override
    public void setEncryption(final Path file, final String algorithm) throws BackgroundException {
        if(file.isFile()) {
            final S3ThresholdCopyFeature copy = new S3ThresholdCopyFeature(session);
            // Copy item in place to write new attributes
            final AclPermission feature = session.getFeature(AclPermission.class);
            if(null == feature) {
                copy.copy(file, file, new S3StorageClassFeature(session).getClass(file), algorithm,
                        Acl.EMPTY);
            }
            else {
                copy.copy(file, file, new S3StorageClassFeature(session).getClass(file), algorithm,
                        feature.getPermission(file));
            }
        }
    }
}
