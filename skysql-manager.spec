%define _topdir	 	%(echo $PWD)/
%define name		skysql-manager
%define release		##RELEASE_TAG##
%define version 	##VERSION_TAG##
%define buildroot 	%{_topdir}/%{name}-%{version}-%{release}-root
%define install_path	/usr/local/tomcat7/webapps/

BuildRoot:		%{buildroot}
Summary: 		SkySQL Manager
License: 		GPL
Name: 			%{name}
Version: 		%{version}
Release: 		%{release}
Source: 		%{name}-%{version}-%{release}.tar.gz
Prefix: 		/
Group: 			Development/Tools
Requires:		tomcat7

%description
SkySQL Manager

%prep

%setup -q

%build

%post
chown tomcat:tomcat %{install_path}SkySQLManager.war

%install
mkdir -p $RPM_BUILD_ROOT%{install_path}
cp SkySQLManager.war $RPM_BUILD_ROOT%{install_path}

%clean

%files
%defattr(-,root,root)
%{install_path}SkySQLManager.war

%changelog
* Fri May 31 2013 Timofey Turenko <timofey.turenko@skysql.com> - 1.2-2
- bugzilla 93, 95; other bug fixes; enabled variable datapoints settings in
  charts



