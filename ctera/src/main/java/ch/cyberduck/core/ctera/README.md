# Custom Properties in Namespace `http://www.ctera.com/ns` in DAV Resources to Support Windows NT ACLs and Write Once Read Many (WORM) Data

## Introduction

Cascading permission checks for file system operations:

* **Filesystem permission model**: i.e. POSIX (NFS and File Provider API macOS) and
  Windows ACL (Cloud Files API Windows) with permissions checked by the operating system. Not all ACLs can
  be mapped to the filesystem permission model. _User experience:_ Filesystem operation not allowed with
  non-customizable error message.
* **Preflight checks**: Preflight checks before file system operation to fail fast prior server API invocation. _User
  experience:_ Filesystem operation not allowed with additional custom error notification.
* **API Failure**: Fail in the CTERA backend ("portal"). This only happens when permissions are changed in the
  backend/portal (e.g. by retention policy). _User experience:_ Filesystem operation allowed with later error
  notification on synchronization.

### File/Directory Permission Setup

Rules:

- Directories with `writepermission` but no `createdirectoriespermission` allow for file creation only.
- Directories with `createdirectoriespermission` but no `writepermission` allow for directory and file creation.
- Directories with only `readpermission` do not allow for file nor directory creation, for listing only.

In other words:

- File creation is allowed if either `createdirectoriespermission` or `writepermission` is set or both are set
- Directory creation is allowed if `createdirectoriespermission` is set.

## Implemented Preflight Checks

| Folder | File | Filesystem Operation | Feature       | Required Permissions (CTERA ACLs)                                                                                                               | Preflight Check |
|--------|------|----------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| x      |      | ls                   | `ListService` | `readpermission`                                                                                                                                | x               |
|        | x    | read                 | `Read`        | `readpermission`                                                                                                                                | x               |                      
|        | x    | write                | `Write`       | `writepermission`                                                                                                                               | x               |
| x      |      | mv                   | `Move`        | source:`deletepermission` AND target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission` | x               |
|        | x    | mv                   | `Move`        | source:`deletepermission` AND target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)   | x               |
| x      |      | cp                   | `Copy`        | target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission`                               | x               |
|        | x    | cp                   | `Copy`        | target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)                                 | x               |
|        | x    | touch                | `Touch`       | `createdirectoriespermission` or `writepermission` (future: target's parent `createfilepermission`)                                             | x               |
| x      |      | mkdir                | `Directory`   | `createdirectoriespermission`                                                                                                                   | x               |
| x      | x    | rm / rmdir           | `Delete`      | `deletepermission`                                                                                                                              | x               |
|        | x    | exec                 | --            | --                                                                                                                                              | --              |

N.B. no need to check `readpermission` upon mv/cp.

## Static Mapping of ACLs (CTERA) to Filesystem Permission Models (Mountain Duck 5+)

| ACL (CTERA)                                                                            | POSIX (Folder)                                      | POSIX (File)                         | Windows `FileSystemRights` (Folder)                       | Windows `FileSystemRights` (File) | Example (Folder)                                                   | Example (File)                                                  |
|----------------------------------------------------------------------------------------|-----------------------------------------------------|--------------------------------------|-----------------------------------------------------------|-----------------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------|
| -                                                                                      | `---`                                               | -                                    | empty                                                     | -                                 | `/ACL test (Alex Berman)/NoAccess/`                                | -                                                               |
| `readpermission`                                                                       | `r-x`                                               | `r--`                                | `ReadAndExecute`                                          | `Read`                            | `/ACL test (Alex Berman)/ReadOnly/`                                | `/ACL test (Alex Berman)/ReadOnly/ReadOnly.txt`                 |
| `readpermission`, `createdirectoriespermission`                                        | `rwx` (delete prevented in preflight)               | -                                    | `ReadAndExecute`, `CreateDirectories`, `CreateFiles` (!), | -                                 | `/WORM test (Alex Berman)/Retention Folder (no write, no delete)/` | -                                                               |
| `readpermission`, `deletepermission`                                                   | `rwx` (folder/file creation prevented in preflight) | `rw-` (write prevented in preflight) | `ReadAndExecute`,  `Delete`                               | `Read`, `Delete`                  | `/ACL test (Alex Berman)/NoCreateFolderPermission`                 | `/ACL test (Alex Berman)/NoCreateFolderPermission/trayIcon.png` |
| `readpermission`, `deletepermission`, `writepermission`                                | -                                                   | `rwx`                                | -                                                         | `Read`, `Delete`, `Write`         | -                                                                  | `/ACL test (Alex Berman)/ReadWrite/Free Access.txt`             |
| `readpermission`, `deletepermission`, `writepermission`, `createdirectoriespermission` | `rwx`                                               | -                                    | `ReadAndExecute`, `Delete`, `Write`                       | -                                 | `/ACL test (Alex Berman)/ReadWrite/`                               | -                                                               |

(§) i.e. synchronously for NFS and asynchronously for file provider (sync flag)

### Preflight Checks Required for Filesystem Operations (Mountain Duck 4+)

| Folder | File | Filesystem Operation                                         | Implementation                                                                        |
|--------|------|--------------------------------------------------------------|---------------------------------------------------------------------------------------|
|        | x    | read                                                         | `Read.preflight`                                                                      |
| x      |      | ls                                                           | `ListService.preflight`                                                               |                      
|        | x    | write, rm, mv source file, mv target file (if exists)        | `Write.preflight` OR `Delete.preflight`                                               |
| x      |      | rmdir, mkdir, mv source folder, mv target folder (if exists) | `Write.preflight` OR `Delete.preflight` OR `Directory.preflight` OR `Touch.preflight` |

N.B. `x` on files is only set for POSIX backends, i.e. never for CTERA.
N.B. File Provider sets the `x` flag on all folders independent of `NSFileProviderFileSystemUserExecutable`.

#### Documentation

* https://developer.apple.com/documentation/fileprovider/nsfileproviderfilesystemflags
* https://developer.apple.com/documentation/fileprovider/nsfileprovideritemcapabilities

### Windows Cloud Files API (_Integrated_ connect mode) Mountain Duck 5+

| Folder | File | Widows `FileSystemRights` | Filesystem Operation                                                | Implementation (`WindowsAcl.Translate`)                                  |
|--------|------|---------------------------|---------------------------------------------------------------------|--------------------------------------------------------------------------|
|        | x    | `Read`                    | read, exec                                                          | `Read.preflight` <-- `readpermission`                                    |                      
| x      |      | `ReadAndExecute`          | ls                                                                  | `ListService.preflight` <-- `readpermission`                             |                      
| x      | x    | `Write`                   | write, touch, mkdir, mv source file, mv target file (if exists)     | `Write.preflight` <-- `writepermission`                                  |
| x      | x    | `Delete`                  | rm, rmdir, mv source file/folder, mv target file/folder (if exists) | `Delete.preflight` <-- `deletepermission`                                |
| x      |      | `CreateDirectories`       | mkdir, mv target folder (if target folder does not exist)           | `Directory.preflight` <-- `createdirectoriespermission`                  |
| x      |      | `CreateFiles`             | touch                                                               | `Touch.preflight` <-- `createdirectoriespermission` or `writepermission` |

N.B. `Write` on folders implies `CreateFiles` (=`WriteData` on files) and `CreateDirectories` (=`AppendData` on files).

#### Documentation

* https://learn.microsoft.com/en-us/dotnet/api/system.security.accesscontrol.filesystemrights?view=net-8.0

