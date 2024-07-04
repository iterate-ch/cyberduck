# Custom Properties in Namespace `http://www.ctera.com/ns` in DAV Resources to Support Windows NT ACLs and Write Once Read Many (WORM) Data

## Introduction

Lines of defense, i.e. are actions intercepted:

* **1st line of defense**: already at filesystem/OS/mount level (i.e. POSIX (NFS and File Provider API macOS) and
  Windows ACL (Cloud Files API Windows) permissions checked by OS).
  Only Cloud Files API under Windows allows to intercept all disallowed operations at this level.
  All other modes have some gaps, i.e. permissions wide enough to pass through and intercept later.
  _User experience:_ operation not allowed.
* **2nd line of defense**: preflight checks before upload (in fail fast nfs, Cyberduck client, cbfs api
  Windows). _User experience:_ operation allowed with error feedback.
* **3rd line of defense**: fail in the CTERA backend ("portal"). This only happens when permissions are changed in the
  backend/portal (e.g. by retention policy).
  _User experience:_ operation allowed with error feedback.

### File/Directory Permission Setup

Rules:

- Directories with `writepermission` but no `createdirectoriespermission` allow for file creation only.
- Directories with `createdirectoriespermission` but no `writepermission` allow for directory and file creation.
- Directories with only `readpermission` do not allow for file nor directory creation, for listing only.

In other words:

- File creation is allowed if either `createdirectoriespermission` or `writepermission` is set or both are set
- Directory creation is allowed if `createdirectoriespermission` is set.

## Implemented Preflight Checks

| Filesystem Operation | Feature       | Folder | File | Required Permissions (CTERA ACLs)                                                                                                               | Preflight Check |
|----------------------|---------------|--------|------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| ls                   | `ListService` | x      |      | `readpermission`                                                                                                                                | x               |
| read                 | `Read`        |        | x    | `readpermission`                                                                                                                                | x               |                      
| write                | `Write`       |        | x    | `writepermission`                                                                                                                               | x               |
| mv                   | `Move`        | x      |      | source:`deletepermission` AND target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission` | x               |
| mv                   | `Move`        |        | x    | source:`deletepermission` AND target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)   | x               |
| cp                   | `Copy`        | x      |      | target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission`                               | x               |
| cp                   | `Copy`        |        | x    | target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)                                 | x               |
| touch                | `Touch`       |        | x    | `createdirectoriespermission` or `writepermission` (future: target's parent `createfilepermission`)                                             | x               |
| mkdir                | `Directory`   | x      |      | `createdirectoriespermission`                                                                                                                   | x               |
| rm / rmdir           | `Delete`      | x      | x    | `deletepermission`                                                                                                                              | x               |
| exec                 | --            |        | x    | --                                                                                                                                              | --              |

N.B. no need to check `readpermission` upon mv/cp.

## Static Mapping of ACLs (CTERA) to Filesystem Permission Models (Mountain Duck 5+)

| ACL (CTERA)                                                                            | POSIX (Folder)                                          | POSIX (File)                             | Windows `FileSystemRights` (Folder)                       | Windows `FileSystemRights` (File) | Example (Folder)                                                   | Example (File)                                                  |
|----------------------------------------------------------------------------------------|---------------------------------------------------------|------------------------------------------|-----------------------------------------------------------|-----------------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------|
| -                                                                                      | `---`                                                   | -                                        | empty                                                     | -                                 | `/ACL test (Alex Berman)/NoAccess/`                                | -                                                               |
| `readpermission`                                                                       | `r-x`                                                   | `r--`                                    | `ReadAndExecute`                                          | `Read`                            | `/ACL test (Alex Berman)/ReadOnly/`                                | `/ACL test (Alex Berman)/ReadOnly/ReadOnly.txt`                 |
| `readpermission`, `createdirectoriespermission`                                        | `rwx` (delete prevented in preflight (§))               | -                                        | `ReadAndExecute`, `CreateDirectories`, `CreateFiles` (!), | -                                 | `/WORM test (Alex Berman)/Retention Folder (no write, no delete)/` | -                                                               |
| `readpermission`, `deletepermission`                                                   | `rwx` (folder/file creation prevented in preflight (§)) | `rw-` (write prevented in preflight (§)) | `ReadAndExecute`,  `Delete`                               | `Read`, `Delete`                  | `/ACL test (Alex Berman)/NoCreateFolderPermission`                 | `/ACL test (Alex Berman)/NoCreateFolderPermission/trayIcon.png` |
| `readpermission`, `deletepermission`, `writepermission`                                | -                                                       | `rwx`                                    | -                                                         | `Read`, `Delete`, `Write`         | -                                                                  | `/ACL test (Alex Berman)/ReadWrite/Free Access.txt`             |
| `readpermission`, `deletepermission`, `writepermission`, `createdirectoriespermission` | `rwx`                                                   | -                                        | `ReadAndExecute`, `Delete`, `Write`                       | -                                 | `/ACL test (Alex Berman)/ReadWrite/`                               | -                                                               |

(§) i.e. synchronously for NFS and asynchronously for file provider (sync flag)

### macOS NFS POSIX (mode online/sync) and File Provider API flags (_Integrated_ connect mode) Mountain Duck 5+

| Folder | File | NFS (POSIX) | Filesystem Operation                                         | Implementation                                                                                                                                                                  |
|--------|------|-------------|--------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        | x    | `r`         | read                                                         | `r` <-- `Read.preflight` <-- `readpermission`                                                                                                                                   |
| x      |      | `rx`        | ls                                                           | `rx` <-- `ListService.preflight` <-- `readpermission`                                                                                                                           |                      
|        | x    | `w`         | write, rm, mv source file, mv target file (if exists)        | `w` <--  (`Write.preflight` OR `Delete.preflight`  <-- (`writepermission` OR `deletepermission`)                                                                                |
| x      |      | `w`         | rmdir, mkdir, mv source folder, mv target folder (if exists) | `w` <--  (`Write.preflight` OR `Delete.preflight` OR `Directory.preflight` OR `Touch.preflight`) <-- (`writepermission` OR `deletepermission` OR `createdirectoriespermission`) |

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

