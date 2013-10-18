%define _topdir	 	%(echo $PWD)/
%define name		MariaDBManager
%define release		##RELEASE_TAG##
%define version 	##VERSION_TAG##
%define buildroot 	%{_topdir}/%{name}-%{version}-%{release}-root
%define install_path	/usr/local/tomcat7/webapps/

BuildRoot:		%{buildroot}
BuildArch:              noarch
Summary: 		MariaDB Manager
License: 		GPL
Name: 			%{name}
Version: 		%{version}
Release: 		%{release}
Source: 		%{name}-%{version}-%{release}.tar.gz
Prefix: 		/
Group: 			Development/Tools
Requires:		tomcat7

%description
MariaDB Manager is a tool to manage and monitor a set of MariaDB
servers using the Galera multi-master replication form Codership.
This package provides a web based user interface to the MariaDB
Manager management and monitoring API.

%prep

%setup -q

%build

%post
chown tomcat:tomcat %{install_path}MariaDBManager.war
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
service iptables save

%install
mkdir -p $RPM_BUILD_ROOT%{install_path}
cp MariaDBManager.war $RPM_BUILD_ROOT%{install_path}

%clean

%files
%defattr(-,root,root)
%{install_path}MariaDBManager.war

%changelog



