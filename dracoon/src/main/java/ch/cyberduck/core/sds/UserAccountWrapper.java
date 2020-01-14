package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.sds.io.swagger.client.model.UserAccount;

import org.apache.commons.lang3.StringUtils;

public class UserAccountWrapper {

    private UserAccount account;

    public UserAccountWrapper(final UserAccount account) {
        this.account = account;
    }

    public boolean isEncryptionEnabled() {
        final Boolean enabled = account.isIsEncryptionEnabled();
        return enabled != null && enabled;
    }

    public Long getId() {
        return account.getId();
    }

    public boolean isUserInRole(final Acl.Role role) {
        return account.getUserRoles().getItems().stream().anyMatch(r -> StringUtils.equals(r.getName(), role.getName()));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserAccountWrapper{");
        sb.append("account=").append(account);
        sb.append('}');
        return sb.toString();
    }
}
