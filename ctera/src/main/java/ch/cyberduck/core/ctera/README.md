# Custom Properties in namespace `http://www.ctera.com/ns` in DAV Resources to Support NT-ACL and WORM Data

## Preflight Checks (nfs 4.x)

| local      | Feature     | folder | file | CTERA required permissions                                                                                                                      | preflight |
|------------|-------------|--------|------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| ls         | ListService | x      |      | `readpermission`                                                                                                                                | x         |
| read       | Read        |        | x    | `readpermission`                                                                                                                                | x         |                      
| write      | Write       |        | x    | `writepermission`                                                                                                                               | x         |
| mv         | Move        | x      |      | source:`deletepermission` AND target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission` | x         |
| mv         | Move        |        | x    | source:`deletepermission` AND target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)   | x         |
| cp         | Copy        | x      |      | target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission`                               | x         |
| cp         | Copy        |        | x    | target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)                                 | x         |
| touch      | Touch       |        | x    | `createdirectoriespermission` (future: target's parent `createfilepermission`)                                                                  | x         |
| mkdir      | Directory   | x      |      | `createdirectoriespermission`                                                                                                                   | x         |
| rm / rmdir | Delete      | x      | x    | `deletepermission`                                                                                                                              | x         |
| exec       | --          |        | x    | --                                                                                                                                              | --        |

N.B. no need to check `readpermission` upon mv/cp.

## Filesystem Mapping (5.x)

| permission set from backend                                                            | POSIX folders                                   | POSIX files                               | ACL folder                                                | ACL files                 | example folders                                                    | example files                                       |
|----------------------------------------------------------------------------------------|-------------------------------------------------|-------------------------------------------|-----------------------------------------------------------|---------------------------|--------------------------------------------------------------------|-----------------------------------------------------|
| -                                                                                      | `---`                                           | -                                         | empty                                                     | -                         | `/ACL test (Alex Berman)/NoAccess/`                                | -                                                   |
| `readpermission`                                                                       | `r-x`                                           | `r-x`                                     | `ReadAndExecute`                                          | `Read`                    | `/ACL test (Alex Berman)/ReadOnly/`                                | `/ACL test (Alex Berman)/ReadOnly/ReadOnly.txt`     |
| `readpermission`, `createdirectoriespermission`                                        | `rwx` (write/delete prevented in preflight (§)) | -                                         | `ReadAndExecute`, `CreateDirectories`, `CreateFiles` (!), | -                         | `/WORM test (Alex Berman)/Retention Folder (no write, no delete)/` | -                                                   |
| `readpermission`, `deletepermission`, `writepermission`                                | -                                               | `rwx`                                     | -                                                         | `Read`, `Delete`, `Write` | -                                                                  | `/ACL test (Alex Berman)/ReadWrite/Free Access.txt` |
| `readpermission`, `deletepermission`, `writepermission`, `createdirectoriespermission` | `rwx`                                           | -                                         | `ReadAndExecute`, `Delete`, `Write`                       | -                         | `/WORM test (Alex Berman)/`                                        | -                                                   |
| `readpermission`, `deletepermission`                                                   | `rwx` (write prevented in preflight (§))        | `rwx`  (write prevented in preflight (§)) | `ReadAndExecute`,  `Write`                                | `Read`, `Delete`, `Write` | -  (!)                                                             | - (!)                                               |

(§) i.e. synchronously for nfs and asynchronously for file provider (sync flag)

### macOS NFS POSIX (mode online/sync) (5.x)

| folder | file | NFS (POSIX) | affected local operations                                    | implementation                                                                                                                                             |
|--------|------|-------------|--------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        | x    | `r`         | read                                                         | `r` <-- `Read.preflight` <-- `readpermission`                                                                                                              |
| x      |      | `rx`        | ls                                                           | `rx` <-- `Read.preflight` <-- `readpermission`                                                                                                             |                      
|        | x    | `w`         | write, rm, mv source file, mv target file (if exists)        | `w` <--  (`Write.preflight` OR `Delete.preflight`  <-- (`writepermission` OR `deletepermission`)                                                           |
| x      |      | `w`         | rmdir, mkdir, mv source folder, mv target folder (if exists) | `w` <--  (`Write.preflight` OR `Delete.preflight` OR `Directory.preflight`) <-- (`writepermission` OR `deletepermission` OR `createdirectoriespermission`) |

N.B. `x` on files is only set for POSIX backends, i.e. never for CTERA.

### macOS File Provider Capabilities (mode integrated) 5.x

| folder | file | File Provider Capabilities                                                                 | affected local operations |
|--------|------|--------------------------------------------------------------------------------------------|---------------------------|
| x      |      | `NSFileProviderFileSystemUserReadable` <-- `ListService.preflight`                         | ls                        |
|        | x    | `NSFileProviderFileSystemUserReadable` <-- `Read.preflight`                                | read                      |
| x      |      | `NSFileProviderFileSystemUserWritable` <-- `Touch.preflight`  <-- TRUE for CTERA           | mv, touch, mkdir          |
|        | x    | `NSFileProviderFileSystemUserWritable` <-- `Write.preflight`                               | write, mv                 |
| x      |      | `NSFileProviderFileSystemUserExecutable` <-- `ListService.preflight`                       | ls                        |
|        | x    | `NSFileProviderFileSystemUserExecutable` <-- `permission.isExecutable` <-- FALSE for CTERA | exec                      |

N.B. File Provider sets the `x` flag on all folders independent of `NSFileProviderFileSystemUserExecutable`.
N.B. `x` on files is only set for POSIX backends, i.e. never for CTERA.

#### Documentation

* https://developer.apple.com/documentation/fileprovider/nsfileproviderfilesystemflags
* https://developer.apple.com/documentation/fileprovider/nsfileprovideritemcapabilities

### Windows CBFS API (mode sync) and Cloud Files API (mode integrated) (5.x)

| folder | file | access right        | affected local operations                                           | implementation (`WindowsAcl.Translate`)                 |
|--------|------|---------------------|---------------------------------------------------------------------|---------------------------------------------------------|
|        | x    | `Read`              | read, exec                                                          | `Read.preflight` <-- `readpermission`                   |                      
| x      |      | `ReadAndExecute`    | ls                                                                  | `ListService.preflight` <-- `readpermission`            |                      
| x      | x    | `Write`             | write, touch, mkdir, mv source file, mv target file (if exists)     | `Write.preflight` <-- `writepermission`                 |
| x      | x    | `Delete`            | rm, rmdir, mv source file/folder, mv target file/folder (if exists) | `Delete.preflight` <-- `deletepermission`               |
| x      |      | `CreateDirectories` | mkdir, mv target folder (if target folder does not exist)           | `Directory.preflight` <-- `createdirectoriespermission` |
| x      |      | `CreateFiles`       | touch                                                               | `Directory.preflight` <-- `createdirectoriespermission` |

N.B. `Write` on folders implies `CreateFiles` (=`WriteData` on files) and `CreateDirectories` (=`AppendData` on files).

#### Documentation

* https://learn.microsoft.com/en-us/dotnet/api/system.security.accesscontrol.filesystemrights?view=net-8.0

