package ch.cyberduck.core;

import ch.cyberduck.core.threading.ActionOperationBatcher;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by alive on 04.11.2016.
 */
public class PermissionOverwrite {
    public final PermissionOverwriteAction user, group, other;

    public PermissionOverwrite() {
        this((Boolean) null, (Boolean) null, (Boolean) null);
    }

    public PermissionOverwrite(Boolean read, Boolean write, Boolean execute) {
        this.user = new PermissionOverwriteAction(read, write, execute);
        this.group = new PermissionOverwriteAction(read, write, execute);
        this.other = new PermissionOverwriteAction(read, write, execute);
    }

    public PermissionOverwrite(PermissionOverwriteAction user, PermissionOverwriteAction group, PermissionOverwriteAction other) {
        this.user = user;
        this.group = group;
        this.other = other;
    }

    public Permission Resolve(final Permission original) {
        return new Permission(
                user.Resolve(original.getUser()),
                group.Resolve(original.getGroup()),
                other.Resolve(original.getOther()),
                original.isSticky(), original.isSetuid(), original.isSetgid());
    }

    @Override
    public String toString() {
        final StringBuilder symbolic = new StringBuilder();

        symbolic.append(user);
        symbolic.append(group);
        symbolic.append(other);

        return symbolic.toString();
    }
}
