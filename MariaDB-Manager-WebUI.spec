%define _topdir	 	%(echo $PWD)/
%define name		MariaDB-Manager-WebUI
%define release		##RELEASE_TAG##
%define version 	##VERSION_TAG##
%define buildroot 	%{_topdir}/%{name}-%{version}-%{release}-root
%define install_path	/usr/local/tomcat7/webapps/

BuildRoot:		%{buildroot}
BuildArch:              noarch
Summary: 		MariaDB Manager Web UI
License: 		GPL
Name: 			%{name}
Version: 		%{version}
Release: 		%{release}
Source: 		%{name}-%{version}-%{release}.tar.gz
Prefix: 		/
Group: 			Development/Tools
Requires:		tomcat7  libMariaDB-Manager-java >= 0.1-11

%description
MariaDB Manager is a tool to manage and monitor a set of MariaDB
servers using the Galera multi-master replication form Codership.
This package provides a web based user interface to the MariaDB
Manager management and monitoring API.

%prep

%setup -q

%build

%post
chkconfig --add tomcat7
#/etc/init.d/tomcat7 restart
/etc/init.d/tomcat7 stop
rm -rf %{install_path}MariaDBManager/
chown tomcat:tomcat %{install_path}MariaDBManager.war
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
service iptables save
sed -i 's|shared.loader=|shared.loader=/usr/local/skysql/share/*.jar|g' /usr/local/tomcat7/conf/catalina.properties
/usr/local/skysql/config/proxypass_tomcat.sh
rm -f /usr/local/skysql/config/proxypass_tomcat.sh
/etc/init.d/tomcat7 start

%install
mkdir -p $RPM_BUILD_ROOT%{install_path}
cp MariaDBManager.war $RPM_BUILD_ROOT%{install_path}

mkdir -p $RPM_BUILD_ROOT/etc/init.d/
cp tomcat7 $RPM_BUILD_ROOT/etc/init.d/

mkdir -p $RPM_BUILD_ROOT/usr/local/skysql/config/
cp proxypass_tomcat.sh  $RPM_BUILD_ROOT/usr/local/skysql/config/

%clean

%files
%defattr(-,root,root)
%{install_path}MariaDBManager.war
/usr/local/skysql/config/proxypass_tomcat.sh
/etc/init.d/tomcat7

%changelog



