package ch.cyberduck.core;

/**
 * Created by alive on 04.11.2016.
 */
public class TestPermissionAttributes extends PathAttributes {
    public TestPermissionAttributes(Permission.Action action) {
        this(action, action);
    }

    public TestPermissionAttributes(Permission.Action userGroup, Permission.Action other) {
        this(userGroup, userGroup, other);
    }

    public TestPermissionAttributes(Permission.Action user, Permission.Action group, Permission.Action other) {
        super();
        setPermission(new Permission(user, group, other));
    }
}