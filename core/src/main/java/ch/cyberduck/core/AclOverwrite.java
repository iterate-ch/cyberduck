package ch.cyberduck.core;

import java.util.List;
import java.util.Map;

/**
 * Created by alive on 08.11.2016.
 */
public class AclOverwrite {
    public final Map<Path, List<Acl.UserAndRole>> originalAcl;
    public final Map<Acl.User, Acl.Role> acl;

    public AclOverwrite(Map<Path, List<Acl.UserAndRole>> originalAcl, Map<Acl.User, Acl.Role> acl) {
        this.originalAcl = originalAcl;
        this.acl = acl;
    }
}
