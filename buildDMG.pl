#!/usr/bin/perl -w
#
my $versionStr = '$Id$';
#
#  Created by Jšrg Westheide on Fri Feb 13 2003.
#  Copyright (c) 2003, 2004 Jšrg Westheide. All rights reserved.
#
#  Permission to use, copy, modify and distribute this software and its documentation
#  is hereby granted, provided that both the copyright notice and this permission
#  notice appear in all copies of the software, derivative works or modified versions,
#  and any portions thereof, and that both notices appear in supporting documentation,
#  and that credit is given to Jšrg Westheide in all documents and publicity
#  pertaining to direct or indirect use of this code or its derivatives.
#
#  THIS IS EXPERIMENTAL SOFTWARE AND IT IS KNOWN TO HAVE BUGS, SOME OF WHICH MAY HAVE
#  SERIOUS CONSEQUENCES. THE COPYRIGHT HOLDER ALLOWS FREE USE OF THIS SOFTWARE IN ITS
#  "AS IS" CONDITION. THE COPYRIGHT HOLDER DISCLAIMS ANY LIABILITY OF ANY KIND FOR ANY
#  DAMAGES WHATSOEVER RESULTING DIRECTLY OR INDIRECTLY FROM THE USE OF THIS SOFTWARE
#  OR OF ANY DERIVATIVE WORK.
#
#  For the most recent version see <http://www.objectpark.org>
#
use strict;
use diagnostics;
use Getopt::Long;
use Cwd;

my $version;
my $debug;
my $help;
my $output;
my $err;
my $minVolSize = 5;   # minimum size of a dmg volume in MB

# determine the build directory, compression level, the list of files to copy, and the size of the dmg volume
# from the environment unless set from the command line
my $buildDir         = $ENV{BUILT_PRODUCTS_DIR};
my $compressionLevel = $ENV{DMG_COMPRESSIONLEVEL};
my $volSize          = $ENV{DMG_VOLSIZE};
my $volName          = $ENV{DMG_VOLNAME};
my $dmgName          = $ENV{DMG_NAME};
my $internetEnabled  = $ENV{DMG_INTERNETENABLED};
my $slaRsrcFile      = $ENV{DMG_SLA_RSRCFILE};
my $deleteHeaders    = ($ENV{DMG_DELETEHEADERS} && ($ENV{DMG_DELETEHEADERS} =~ /^\s*yes\s*$/i));
my $files;

# override them with command line options
GetOptions('help'               => \$help,
           'version'            => \$version,
           'buildDir=s'         => \$buildDir,
           'compressionLevel=i' => \$compressionLevel,
           'debug'              => \$debug,
           'deleteHeaders!'     => \$deleteHeaders,
           'dmgName=s'          => \$dmgName,
           'internetEnabled!'   => \$internetEnabled,
           'slaRsrcFile=s'      => \$slaRsrcFile,
           'volSize=i'          => \$volSize,
           'volName=s'          => \$volName
          );

if ($help) {
    print `perldoc $0`;
    exit 0;
}

if ($version) {
    my ($prog, $version) = ($versionStr =~ /:\s*(\w+).pl\S*\s+(\d+\.?\d*)/);
    print "$prog v$version\n";
    exit 0;
}

my $firstFile = $ARGV[0];   # save an unescaped version, we may need it for the dmg's name
for (my $i = @ARGV-1; $i >= 0; $i--) {
    $ARGV[$i] =~ s/ /\\ /g;         # escape spaces (we pass the files on the command line)
}

die "FATAL: No files to copy specified\n" unless @ARGV or $ENV{DMG_FILESLIST};

$files = join(' ', @ARGV);
$files .= " $ENV{DMG_FILESLIST}" if $ENV{DMG_FILESLIST};

$buildDir = cwd() unless $buildDir;

# determine dmg and volume name
if (my $settings = readSettings()) {
    my ($name)    = ($settings =~ /<key>CFBundleName<\/key>.*?<string>(.*?)<\/string>/is);
    my ($version) = ($settings =~ /<key>CFBundleVersion<\/key>.*?<string>(.*?)<\/string>/is);
    $volName = "$name $version" unless $volName;
    unless ($dmgName) {
        $dmgName = "$name $version";
        $dmgName =~ tr/ ./__/;
    }
}

unless ($ENV{SETTINGS_FILE}) {
    $dmgName = $firstFile unless $dmgName;
    $dmgName =~ s#.*/([^/]+)$#$1#;           # we have to cut off the path
    $dmgName =~ s/(.*?)(\.[^.]*)?$/$1/;      # cut off the extension
    $volName = $dmgName unless $volName;
}

# if ProjectBuilder asks us to "clean" we remove the dmg. if we determined the name ourself, we cannot determine 
# it now since PB has already deleted the settings file :-(. So we delete all dmgs in the build directory
if ($ENV{ACTION} && $ENV{ACTION} =~ /clean/i) {
    $dmgName = '*' unless $dmgName;
    print glob "$buildDir/$dmgName.dmg";
    unlink glob "$buildDir/$dmgName.dmg";
    exit 0;
}

# if requested determine required size for the dmg
unless ($volSize && ($volSize > 0)) {
    eval { $output = `du -csk $files`};
    die "Couldn't determine the required space for the dmg: $@\n" if $@;
    
    ($volSize) = ($output =~ /\s*(\d+)\s+total\s*$/si);
    $volSize = int $volSize * 1.5 / 1024 + 1;
    $volSize = $minVolSize if $volSize < $minVolSize;
}

# OK, we have determined all out parameters.

# print them for debugging
if ($debug) {
    print STDERR "buildDir: ", $buildDir ? $buildDir : "", "\n";
    print STDERR "compressionLevel: ", $compressionLevel ? $compressionLevel : "", "\n";
    print STDERR "volSize: ", $volSize ? $volSize : "", "\n";
    print STDERR "volName: ", $volName ? $volName : "", "\n";
    print STDERR "dmgName: ", $dmgName ? $dmgName : "", "\n";
    print STDERR "internetEnabled: ", $internetEnabled ? $internetEnabled : "", "\n";
    print STDERR "slaRsrcFile: ", $slaRsrcFile ? $slaRsrcFile : "", "\n";
    print STDERR "deleteHeaders: ", $deleteHeaders ? $deleteHeaders : "", "\n";
    print STDERR "files: ", $files ? $files : "", "\n";
}

# Now we start our work...

# create the dmg
print "> hdiutil create \"$buildDir/$dmgName\" -ov -megabytes $volSize -fs HFS+ -volname \"$volName\"\n" if $debug;
$output = `hdiutil create \"$buildDir/$dmgName\" -ov -megabytes $volSize -fs HFS+ -volname \"$volName\"`;
die "FATAL: Couldn't create dmg $dmgName (Error: $?)\nIs it possibly mounted?\n" if $?;

($dmgName) = ($output =~ /created\s*:\s*(?:.*?$buildDir\/)?(.+?)\s*$/m);
die "FATAL: Couldn't read created dmg name\n" unless $dmgName;

print "Changed dmgName to \"$dmgName\"\n" if $debug;

# mount the dmg
print "> hdiutil attach \"$buildDir/$dmgName\"\n" if $debug;
$output = `hdiutil attach \"$buildDir/$dmgName\"`;
die "FATAL: Couldn't mount DMG $dmgName (Error: $?)\n" if $?;

my ($dev)  = ($output =~ /(\/dev\/.+?)\s*Apple_partition_scheme/im);
my ($dest) = ($output =~ /Apple_HFS\s+(.+?)\s*$/im);

# copy the files onto the dmg
print "Copying files to $dest...\n";
print "> /Developer/Tools/CpMac -r $files \"$dest\"\n" if $debug;
$output = `/Developer/Tools/CpMac -r $files \"$dest\"`;
$err = $?;

# delete headers
if ($deleteHeaders) {
    print "Deleting header files and directories...\n";
    print "> find -E -d \"$dest\" -regex \".*/(Private)?Headers\" -exec rm -rf {} \";\"\n" if $debug;
    $output = `find -E -d "$dest" -regex ".*/(Private)?Headers" -exec rm -rf {} ";"`;
}

# unmount the dmg
print "> hdiutil detach $dev\n" if $debug;
$output = `hdiutil detach $dev`;
die "FATAL: Error while copying files (Error: $err)\n" if $err;
die "FATAL: Couldn't unmount device $dev: $?\n" if $?;

# compress the dmg
my $tmpDmgName = "$dmgName~";

if ($compressionLevel) {
    print "Compressing $dmgName...\n";
    print "> mv -f \"$buildDir/$dmgName\" \"$buildDir/$tmpDmgName\"\n" if $debug;
    $output = `mv -f "$buildDir/$dmgName" "$buildDir/$tmpDmgName"`;
    
    print "> hdiutil convert \"$buildDir/$tmpDmgName\" -format UDZO -imagekey zlib-level=$compressionLevel -o \"$buildDir/$dmgName\"\n" if $debug;
    $output = `hdiutil convert "$buildDir/$tmpDmgName" -format UDZO -imagekey zlib-level=$compressionLevel -o "$buildDir/$dmgName"`;
    die "Error: Couldn't compress the dmg $dmgName: $?\n" if $?;
    
    unlink "$buildDir/$tmpDmgName";
}

# Adding the SLA
if ($slaRsrcFile) {
    print "Adding SLA...\n";
    print "> hdiutil unflatten \"$buildDir/$dmgName\"\n" if $debug;
    $output = `hdiutil unflatten \"$buildDir/$dmgName"`;
    die "Couldn't unflatten dmg (Error:$?)\n" if $?;
    
    unless ($?) {
        print "> /Developer/Tools/Rez /Developer/Headers/FlatCarbon/*.r \"$slaRsrcFile\" -a -o \"$buildDir/$dmgName\"\n" if $debug;
        $output = `/Developer/Tools/Rez /Developer/Headers/FlatCarbon/*.r "$slaRsrcFile" -a -o "$buildDir/$dmgName"`;
        print STDERR "Couldn't add SLA (Error: $?)\n" if $?;
        
        print "> hdiutil flatten \"$buildDir/$dmgName\"\n" if $debug;
        $output = `hdiutil flatten "$buildDir/$dmgName"`;
        die "Couldn't flatten dmg (Error: $?)\n" if $?;
    }
}

# Enabling internet access
if ($internetEnabled) {
    print "> hdiutil internet-enable -yes \"$buildDir/$dmgName\"\n" if $debug;
    $output = `hdiutil internet-enable -yes "$buildDir/$dmgName"`;
    print STDERR "Couldn't enable internet access for $dmgName (Error: $?)\n" if $?;
}

print "Done.\n";

exit 0;



sub readSettings {
    return undef unless $ENV{SETTINGS_FILE};
    return undef if ($ENV{ACTION} =~ /clean/i) && !(-s $ENV{SETTINGS_FILE});
    
    my $settings;
    my $oldSep = $/;
    undef $/;
    
    open FH, "<$ENV{SETTINGS_FILE}" or die "Couldn't read file $ENV{SETTINGS_FILE}\n";
    $settings = <FH>;
    close FH;
    
    $/ = $oldSep;
    
    return $settings;
}


=head1 NAME

B<buildDMG> - build a DMG from the commandline or from inside ProjectBuilder

=head1 SYNOPSIS

buildDMG.pl [-help] [-version] [-debug] [-buildDir dir] [-compressionLevel n] [-deleteHeaders] [-dmgName name] [-slaRsrcFile file] [-volName name] 
[-volSize n] files...

=head1 DESCRIPTION

buildDMG can be used to create a dmg either from command line or within ProjectBuilder. The special support for ProjectBuilder consist 
of evaluating environment variables and creating volume and dmg names from the project's settings file.

The following options are available (and override the mentioned environment variables):

=over 4

=item B<-buildDir> I<directory>

specifies the I<directory> in which the dmg should be created. If this option is not specified the value of the environment variable 
B<BUILT_PRODUCTS_DIR> (which is automatically provided by ProjectBuilder). If no value is provided the default will be the current 
directory

=item B<-compressionLevel> I<n>

specifies the compression level for zlib compression. Legal values for I<n> are 1-9 with 1 being fastet, 9 best compression. 0 turns 
compression off. The corresponding environment variable is B<DMG_COMPRESSIONLEVEL>. The default is 0 (no compression)

=item B<-debug>

enables output of debug information

=item B<-[no]deleteHeaders>

specifies whether all the folders "Headers" and "PrivateHeaders" on the dmg should be deleted or not. The environment variable is 
B<DMG_DELETEHEADERS>, the default is not to delete

=item B<-dmgName> I<name>

specifies the I<name> of the dmg to produce (without extension). The corresponding environment variable is B<DMG_NAME>. If neither 
the option, nor the environment variable contains a I<name>, nor a settings file is specified (see environment variable 
B<SETTINGS_FILE> in the Project Builder Support section below) the name of the first file will be used

=item B<-help>

displays this documentation

=item B<-[no]internetEnabled>

specifies whether the dmg should be enabled for internet access or not (default). Seems this works only works with compressed dmgs, 
but since that is a "feature" of B<hdiutil> this is not enforced by buildDMG

=item B<-slaRsrcFile> I<file>

specifies the .r I<file> containing the source of the resources for the software license agreement to display when the dmg is mounted. 
The corresponding environment variable is B<DMG_SLA_RSRCFILE>. The source will be compiled with the Rez command and the result 
attached to the dmg

=item B<-version>

displays the version number

=item B<-volName> I<name>

specifies the I<name> of the volume inside the dmg. The corresponding environment variable is B<DMG_VOLNAME>. If neither the option, 
nor the environment variable contains a I<name>, nor a settings file is specified (see environment variable B<SETTINGS_FILE> in the
Project Builder Support section below) the name of the first file will be used

=item B<-volSize> I<n>

specifies the size of the volume to create in megabytes. The environment variable is B<DMG_VOLSIZE>. If no value or 0 is specified 
B<buildDmg> will try to determine the size by looking at the files to copy

=back

The B<files> specified as parameters AND the files specified in the environment variable B<DMG_FILESLIST> are copied onto the dmg 
(before the headers are deleted), starting with the files from the command line

=head1 PROJECT BUILDER SUPPORT

Due to the possibility to use environment variables instead of the above mentioned command line options you can use this script from 
a "Legacy Makefile" target. Therefore you have to set the build tool to "/usr/bin/perl", the arguments to "<pathToScript>/buildDMG.pl",
and check the "Pass build settings in environment" checkbox. You then can control everything with the build settings. If you make this 
target depending on your "application target" you can build you app and put it in a dmg with a single click

The B<SETTINGS_FILE> environment variable is only used if the dmg or volume name is not specified. If B<SETTINGS_FILE> is set it 
should point to the "Info.plist" of the project to copy onto the dmg. buildDMG is then able to automatically generate the dmg and 
volume name from the B<CFBundleName> and B<CFBundleVersion> entries. For the dmg name some characters which may be problematic 
are then replaced by an underscore ('_')

When cleaning the target there is a problem with Project Builder cleaning the dependent target first, so chances are good that the file
specified in B<SETTINGS_FILE> is not existing anymore. If so buildDMG deletes all dmg files in B<buildDir>

=head1 EXAMPLES

C</usr/bin/perl buildDMG.pl>

This is the way buildDMG can be called when all required environment variables are set (e.g. from ProjectBuilder)

C<./buildDMG.pl -dmgName Name -buildDir build -volSize 10 -volName Volume -compressionLevel 9 -slaRsrcFile SLA.r Example.app 
-deleteHeaders>

This creates a dmg called "Name.dmg" in the directory "build". It contains a 10 MB volume named "Volume" and is compressed with the 
highest compression level. The source for the SLA is obtained from the file SLA.r and the file (or file tree) "Example.app" is copied 
onto the dmg, with header directories removed (after copying!)

=head1 AUTHOR

Joerg Westheide (joerg@objectpark.org)

=head1 SEE ALSO

Rez(1), hdiutil(1)

