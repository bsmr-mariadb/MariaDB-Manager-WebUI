%define _topdir	 	%(echo $PWD)/
%define name		admin_ui
%define release		1
%define version 	1.2
%define buildroot %{_topdir}/%{name}-%{version}-%{release}-root
%define install_path	/usr/local/tomcat7/webapps/

BuildRoot:		%{buildroot}
Summary: 		SkySQL Administration Console
License: 		GPL
Name: 			%{name}
Version: 		%{version}
Release: 		%{release}
Source: 		%{name}-%{version}-%{release}.tar.gz
Prefix: 		/
Group: 			Development/Tools
Requires:		tomcat7-skysql admin_php

%description
SkySQL Administration Console

%prep

%setup -q

%build

%install
mkdir -p $RPM_BUILD_ROOT%{install_path}
mkdir -p $RPM_BUILD_ROOT/etc/httpd/conf.d
cp ConsoleV.war $RPM_BUILD_ROOT%{install_path}
cp skysql_rewrite.conf $RPM_BUILD_ROOT/etc/httpd/conf.d/skysql_rewrite.conf

%clean

%files
%defattr(-,root,root)
%{install_path}ConsoleV.war
/etc/httpd/conf.d/skysql_rewrite.conf

%changelog


