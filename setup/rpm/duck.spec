Summary: Cyberduck CLI
Name: duck
Version: ${VERSION}.${REVISION}
Release: 1
License: GPL
Vendor: duck.sh
Prefix: /opt
Provides: duck
Requires: glibc
Autoprov: 0
Autoreq: 0

#avoid ARCH subfolder
%define _rpmfilename %%{NAME}-${VERSION}.${REVISION}.%%{ARCH}.rpm

%define package_filelist %{_tmppath}/%{name}.files
%define app_filelist %{_tmppath}/%{name}.app.files
%define filesystem_filelist %{_tmppath}/%{name}.filesystem.files
%define license_install_file %{_defaultlicensedir}/%{name}-%{version}/%{basename:${LICENSEFILE}}

%define default_filesystem / /opt /usr /usr/bin /usr/lib /usr/local /usr/local/bin /usr/local/lib

#comment line below to enable effective jar compression
#it could easily get your package size from 40 to 15Mb but
#build time will substantially increase and it may require unpack200/system java to install
%define __jar_repack %{nil}

%description
Cyberduck CLI

%prep

%build

# from template https://github.com/openjdk/jdk/blob/303631e3d5e08668ce77be53bfb23545aabe84d1/src/jdk.jpackage/linux/classes/jdk/jpackage/internal/resources/template.spec
#
%install
rm -rf %{buildroot}
install -d -m 755 %{buildroot}/opt/duck
cp -r %{_sourcedir}/opt/duck/* %{buildroot}/opt/duck
install -d -m 755 "%{buildroot}%{dirname:%{license_install_file}}"
install -m 644 "${LICENSEFILE}" "%{buildroot}%{license_install_file}"
(cd %{buildroot} && find . -type d) | sed -e 's/^\.//' -e '/^$/d' | sort > %{app_filelist}
{ rpm -ql filesystem || echo %{default_filesystem}; } | sort > %{filesystem_filelist}
comm -23 %{app_filelist} %{filesystem_filelist} > %{package_filelist}
sed -i -e 's/.*/%dir "&"/' %{package_filelist}
(cd %{buildroot} && find . -not -type d) | sed -e 's/^\.//' -e 's/.*/"&"/' >> %{package_filelist}
sed -i -e 's|"%{license_install_file}"||' -e '/^$/d' %{package_filelist}

%files -f %{package_filelist}
%license "%{license_install_file}"

%post
ln -sf /opt/duck/bin/duck /usr/local/bin/duck

%clean
rm -rf %{buildroot}

%postun
