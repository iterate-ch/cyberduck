package ch.cyberduck.core;

public class PermissionOverwriteAction {
    public Boolean read;
    public Boolean write;
    public Boolean execute;

    public PermissionOverwriteAction(Boolean read, Boolean write, Boolean execute) {
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

    private static Permission.Action SolvePermission(Permission.Action base, Permission.Action permission, boolean value) {
        return value ? base.or(permission) : base.and(permission.not());
    }
}