#
# spec file for package @ANTPROJECTNAME@ (Version @PROJECTVERSIONSPEC@)
#
# Copyright 2008 SUSE LINUX Products GmbH, Nuernberg, Germany.
# Copyright @PROJECTYEARS@ @COMPANYNAME@ @COMPANYURL@, @COMPANYCOUNTRYID@
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

#norootforbuild
Summary: @PROJECTNAME@
Name: @ANTPROJECTNAME@
Version: @PROJECTVERSIONIMPL@
Release: @PROJECTVERSIONRELEASE@
Source0: %{name}-%{version}.tar.gz
License: GPL
Group: Amusements/Games/Action/Arcade
%if 0%{?suse_version} < 1120
BuildRoot: %{_builddir}/%{name}-%{version}-%{release}.build
%endif%if 0%{?suse_version} < 1210
BuildRequires: java-1_6_0-sun-devel, ant, ant-apache-regexp, update-desktop-files, sf3jswing-jigaxtended
Requires: java-1_6_0-sun, sf3jswing-jigaxtended
%endif
%if 0%{?suse_version} < 1220
BuildRequires: java-1_6_0-openjdk, ant, ant-apache-regexp, update-desktop-files, sf3jswing-jigaxtended
Requires: java-1_6_0-openjdk, sf3jswing-jigaxtended
%else
BuildRequires: java-1_7_0-openjdk, ant, ant-apache-regexp, update-desktop-files, sf3jswing-jigaxtended
Requires: java-1_7_0-openjdk, sf3jswing-jigaxtended
%endif
%description
@PROJECTDESCRIPTION@
%prep
%setup -q
%build
%ant clean
%ant pre-install
%ant build-jar
%install
export MYRPM_BUILDROOT=%{buildroot}
%ant pre-rpm-build-install
%if 0%{?suse_version} < 1120
%clean
%{__rm} -rf '%{buildroot}'
%endif
%ant clean
%files
%defattr(-,root,root)
/usr/games/%name
/usr/share/games/%name
/usr/share/pixmaps/%name.png
/usr/share/applications/%name.desktop
%changelog
