# Custom Properties in Namespace `http://www.ctera.com/ns` in DAV Resources to Support Windows NT ACLs and Write Once Read Many (WORM) Data

## Introduction

Cascading permission checks for file system operations in Mountain Duck:

* **Filesystem permission model**: i.e. POSIX (NFS and File Provider API macOS) and
  Windows ACL (Cloud Files API Windows) with permissions checked by the operating system. Not all ACLs can
  be mapped to the filesystem permission model. <br/>_User experience:_ Filesystem operation not allowed with
  non-customizable error message.
* **Preflight checks**: Preflight checks before file system operation to fail fast prior server API invocation. <br/>
  _User experience:_ Filesystem operation not allowed with additional custom error notification.
  * Cyberduck: before user interaction (dropdown, drag-and-drop)
  * Mountain Duck 4+: synchronously for macOS NFS and Windows cbfs (_online_/_sync_ connect modes)
  * Mountain Duck 5+: asynchronously for Windows Cloud Files API (_Integrated_ connect mode) and File Provider API
    flags (_Integrated_ connect mode) Mountain Duck 5+
* **API Failure**: Fail in the CTERA backend ("portal"). This only happens when permissions are changed in the
  backend/portal (e.g. by retention policy).
  <br/>_User experience:_ Filesystem operation allowed with later error
  notification on synchronization.

### Required Preflight Checks for Filesystem Operations (Mountain Duck 4+)

| Filesystem Operation | Feature Preflight |
|----------------------|-------------------|
| read                 | `Read`            |
| ls                   | `ListService`     |                      
| write                | `Write`           |
| rm                   | `Delete`          |
| mv                   | `Move`            |
| touch                | `Touch`           |
| mkdir                | `Directory`       |

### Mapping of Preflight Checks to POSIX Permissions and Windows FileSystemRights (Mountain Duck 5+)

| Folder | File | Feature Preflight | Windows `FileSystemRights` | POSIX Permissions |
|--------|------|-------------------|----------------------------|-------------------|
|        | x    | `Read`            | `Read`                     | r--               |                      
| x      |      | `ListService`     | `ReadAndExecute`           | r-x               |                      
| x      | x    | `Write`           | `Write`                    | -w-               |
| x      | x    | `Delete`          | `Delete`                   | -w-               |
| x      |      | `Touch`           | `CreateFiles`              | -w-               |
| x      |      | `Directory`       | `CreateDirectories`        | -w-               |

N.B. `Write` on folders implies `CreateFiles` (=`WriteData` on files) and `CreateDirectories` (=`AppendData` on files).

The following `FileSystemRights` are always set under Windows:
`TakeOwnership`, `ChangePermissions`, `Synchronize`, `ReadAttributes`, `ReadExtendedAttributes`.

For directories, `WriteAttributes` and `WriteExtendedAttributes` are also set in addition to `Write`.

## CTERA Setup

### File/Directory Permission Setup

Rules:

- Directories with `writepermission` but no `createdirectoriespermission` allow for file creation only.
- Directories with `createdirectoriespermission` but no `writepermission` allow for directory and file creation.
- Directories with only `readpermission` do not allow for file nor directory creation, for listing only.

In other words:

- File creation is allowed if either `createdirectoriespermission` or `writepermission` is set or both are set
- Directory creation is allowed if `createdirectoriespermission` is set.

### Required ACLs for Preflight Checks (Mountain Duck 4+)

| Folder | File | Filesystem Operation | Feature       | Required Permissions (CTERA ACLs)                                                                                                               |
|--------|------|----------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| x      |      | ls                   | `ListService` | `readpermission`                                                                                                                                |
|        | x    | read                 | `Read`        | `readpermission`                                                                                                                                |                      
|        | x    | write                | `Write`       | `writepermission`                                                                                                                               |
| x      |      | mv                   | `Move`        | source:`deletepermission` AND target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission` |
|        | x    | mv                   | `Move`        | source:`deletepermission` AND target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)   |
| x      |      | cp                   | `Copy`        | target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission`                               |
|        | x    | cp                   | `Copy`        | target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)                                 |
|        | x    | touch                | `Touch`       | `createdirectoriespermission` or `writepermission` (future: target's parent `createfilepermission`)                                             |
| x      |      | mkdir                | `Directory`   | `createdirectoriespermission`                                                                                                                   |
| x      | x    | rm / rmdir           | `Delete`      | `deletepermission`                                                                                                                              |
|        | x    | exec                 | --            | --                                                                                                                                              |

N.B. no need to check `readpermission` upon mv/cp.

### Sample Mapping of ACLs (CTERA) to Filesystem Permission Models (Mountain Duck 5+)

| ACL (CTERA)                                                                            | POSIX (Folder)                                      | POSIX (File)                         | Windows `FileSystemRights` (Folder)                       | Windows `FileSystemRights` (File) | Example (Folder)                                                   | Example (File)                                                  |
|----------------------------------------------------------------------------------------|-----------------------------------------------------|--------------------------------------|-----------------------------------------------------------|-----------------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------|
| -                                                                                      | `---`                                               | -                                    | empty                                                     | -                                 | `/ACL test (Alex Berman)/NoAccess/`                                | -                                                               |
| `readpermission`                                                                       | `r-x`                                               | `r--`                                | `ReadAndExecute`                                          | `Read`                            | `/ACL test (Alex Berman)/ReadOnly/`                                | `/ACL test (Alex Berman)/ReadOnly/ReadOnly.txt`                 |
| `readpermission`, `createdirectoriespermission`                                        | `rwx` (delete prevented in preflight)               | -                                    | `ReadAndExecute`, `CreateDirectories`, `CreateFiles` (!), | -                                 | `/WORM test (Alex Berman)/Retention Folder (no write, no delete)/` | -                                                               |
| `readpermission`, `deletepermission`                                                   | `rwx` (folder/file creation prevented in preflight) | `rw-` (write prevented in preflight) | `ReadAndExecute`,  `Delete`                               | `Read`, `Delete`                  | `/ACL test (Alex Berman)/NoCreateFolderPermission`                 | `/ACL test (Alex Berman)/NoCreateFolderPermission/trayIcon.png` |
| `readpermission`, `deletepermission`, `writepermission`                                | -                                                   | `rwx`                                | -                                                         | `Read`, `Delete`, `Write`         | -                                                                  | `/ACL test (Alex Berman)/ReadWrite/Free Access.txt`             |
| `readpermission`, `deletepermission`, `writepermission`, `createdirectoriespermission` | `rwx`                                               | -                                    | `ReadAndExecute`, `Delete`, `Write`                       | -                                 | `/ACL test (Alex Berman)/ReadWrite/`                               | -                                                               |

#### References

* https://learn.microsoft.com/en-us/dotnet/api/system.security.accesscontrol.filesystemrights?view=net-8.0
* https://developer.apple.com/documentation/fileprovider/nsfileproviderfilesystemflags
* https://developer.apple.com/documentation/fileprovider/nsfileprovideritemcapabilities
