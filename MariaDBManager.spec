%define _topdir	 	%(echo $PWD)/
%define name		MariaDBManager
%define release		##RELEASE_TAG##
%define version 	##VERSION_TAG##
%define buildroot 	%{_topdir}/%{name}-%{version}-%{release}-root
%define install_path	/usr/local/tomcat7/webapps/

BuildRoot:		%{buildroot}
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
MariaDB Manager

%prep

%setup -q

%build

%post
chown tomcat:tomcat %{install_path}MariaDBManager.war

%install
mkdir -p $RPM_BUILD_ROOT%{install_path}
cp MariaDBManager.war $RPM_BUILD_ROOT%{install_path}

%clean

%files
%defattr(-,root,root)
%{install_path}MariaDBManager.war

%changelog



