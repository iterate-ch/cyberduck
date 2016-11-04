package ch.cyberduck.core;

import ch.cyberduck.core.threading.ActionOperationBatcher;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by alive on 04.11.2016.
 */
public class PermissionOverwrite {
    public final Action user, group, other;

    public PermissionOverwrite() {
        this((Boolean) null, (Boolean) null, (Boolean) null);
    }

    public PermissionOverwrite(Boolean read, Boolean write, Boolean execute) {
        this.user = new Action(read, write, execute);
        this.group = new Action(read, write, execute);
        this.other = new Action(read, write, execute);
    }

    public PermissionOverwrite(Action user, Action group, Action other) {
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

    public class Action {
        public Boolean read;
        public Boolean write;
        public Boolean execute;

        public Action(Boolean read, Boolean write, Boolean execute) {
            this.read = read;
            this.write = write;
            this.execute = execute;
        }

        public Permission.Action Resolve(Permission.Action original) {
            Permission.Action result = Permission.Action.none;

            result = SolvePermission(result, Permission.Action.read, read == null ? original.implies(Permission.Action.read) : read.booleanValue());
            result = SolvePermission(result, Permission.Action.write, write == null ? original.implies(Permission.Action.write) : write.booleanValue());
            result = SolvePermission(result, Permission.Action.execute, execute == null ? original.implies(Permission.Action.execute) : execute.booleanValue());

            return result;
        }

        @Override
        public String toString() {
            final StringBuilder symbolic = new StringBuilder();

            symbolic.append(read != null ? read ? 'r' : '-' : '?');
            symbolic.append(write != null ? write ? 'w' : '-' : '?');
            symbolic.append(execute != null ? execute ? 'x' : '-' : '?');

            return symbolic.toString();
        }
    }

    private static Permission.Action SolvePermission(Permission.Action base, Permission.Action permission, boolean value) {
        return value ? base.or(permission) : base.and(permission.not());
    }
}
