package ch.cyberduck.core.ocs;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.features.Share;

import org.apache.http.HttpEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class OcsShareeResponseHandler extends OcsResponseHandler<Set<Share.Sharee>> {

    @Override
    public Set<Share.Sharee> handleEntity(final HttpEntity entity) throws IOException {
        final XmlMapper mapper = new XmlMapper();
        final ch.cyberduck.core.ocs.model.Share value = mapper.readValue(entity.getContent(), ch.cyberduck.core.ocs.model.Share.class);
        if(value.data != null) {
            if(value.data.users != null) {
                final Set<Share.Sharee> sharees = new HashSet<>();
                for(ch.cyberduck.core.ocs.model.Share.user user : value.data.users) {
                    final String id = user.value.shareWith;
                    final String label = String.format("%s (%s)", user.label, user.shareWithDisplayNameUnique);
                    sharees.add(new Share.Sharee(id, label));
                }
                return sharees;
            }
        }
        return Collections.emptySet();
    }
}
