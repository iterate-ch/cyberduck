package ch.cyberduck.core.deepbox;/*
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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class DeepboxI18nService {

    private static final Map<String, String> INBOX = ImmutableMap.<String, String>builder()
            .put("de-CH", "Inbox")
            .put("en-UK", "Inbox")
            .put("fr-CH", "Inbox")
            .put("it-CH", "Inbox")
            .build();

    private static final Map<String, String> DOCUMENTS = ImmutableMap.<String, String>builder()
            .put("de-CH", "Dokumente")
            .put("en-UK", "Documents")
            .put("fr-CH", "Documents")
            .put("it-CH", "Documenti")
            .build();
    private static final Map<String, String> TRASH = ImmutableMap.<String, String>builder()
            .put("de-CH", "Papierkorb")
            .put("en-UK", "Trash")
            .put("fr-CH", "Cestino")
            .put("it-CH", "Corbeille")
            .build();

    public static final Set<String> INBOX_NAMES = ImmutableSet.copyOf(INBOX.values());
    public static final Set<String> DOCUMENTS_NAMES = ImmutableSet.copyOf(DOCUMENTS.values());
    public static final Set<String> TRASH_NAMES = ImmutableSet.copyOf(TRASH.values());
    private final DeepboxSession session;

    public DeepboxI18nService(final DeepboxSession session) {
        this.session = session;
    }

    public String getInboxName() {
        return INBOX.get(session.getSelectedLanguage());
    }

    public String getDocumentsName() {
        return DOCUMENTS.get(session.getSelectedLanguage());
    }

    public String getTrashName() {
        return TRASH.get(session.getSelectedLanguage());
    }
}
