package ch.cyberduck.core.eue;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.DeleteResourceApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class EueDeleteFeature extends EueTrashFeature implements Delete {

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueDeleteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        super(session, fileid);
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        try {
            // Move to trash first as precondition of delete
            this.delete(super.trash(files, prompt, callback));
            for(Path f : files.keySet()) {
                fileid.cache(f, null);
            }
        }
        catch(ApiException e) {
            for(Path f : files.keySet()) {
                throw new EueExceptionMappingService().map("Cannot delete {0}", e, f);
            }
        }
    }

    protected void delete(final List<String> resources) throws ApiException {
        for(String resourceId : resources) {
            new DeleteResourceApi(new EueApiClient(session)).resourceResourceIdDelete(resourceId, null, null);
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        if(StringUtils.equals(EueResourceIdProvider.TRASH, file.attributes().getFileId())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file.getName())).withFile(file);
        }
        else if(StringUtils.equals(session.getHost().getProperty("cryptomator.vault.name.default"), file.getName())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file.getName())).withFile(file);
        }
    }
}
