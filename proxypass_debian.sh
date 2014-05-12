#!/bin/bash
#
# This file is distributed as part of the MariaDB Manager. It is free
# software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# version 2.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51
# Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Copyright 2014 (c) SkySQL Corporation Ab
#
# Author: Massimo Siani
# Date: April 2014

a2enmod proxy_ajp 
apacheMod=$(echo "ProxyPass /MariaDBManager ajp://localhost:8009/MariaDBManager" | sed 's/[]\/()$*.^|[]/\\&/g' | sed -e ':a;N;$!ba;s/\n/\\\n/g')
if ! grep -q ProxyPass /etc/apache2/sites-enabled/000-default ; then
    sed -i "/\/VirtualHost/i $apacheMod" /etc/apache2/sites-enabled/000-default
fi
service apache2 reload

# Connector ajp on port 8009 disabled?
if [[ -f /etc/tomcat6/server.xml ]] ; then
    tomcatConf="/etc/tomcat6/server.xml"
    tomcatService=tomcat6
elif [[ -f /usr/local/tomcat7/conf/server.xml ]] ; then
    tomcatConf="/usr/local/tomcat7/conf/server.xml"
    tomcatService=tomcat7
elif [[ -f /etc/tomcat7/server.xml ]] ; then
    tomcatConf="/etc/tomcat7/server.xml"
    tomcatService=tomcat7
else
    echo "ERROR: tomcat configuration file server.xml not found"
    exit 1
fi
connector=$(grep -C 1 "<Connector port=\"8009\"" $tomcatConf)
connOnly=$(head -n 2 <<<"$connector" | tail -n 1)
connComment=$(head -n 1 <<<"$connector")
connAfter=$(tail -n 1 <<<"$connector")
if [[ "$connComment" =~ ^[[:space:]]*\<\!--$ && "$connAfter" =~ ^[[:space:]]*--\>$ ]] ; then
	connectorLine=$(grep -n "$connOnly" $tomcatConf | cut -f1 -d":")
	(( before = connectorLine - 1 ))
	(( after = connectorLine + 1 ))
	sed -i -e "$before d" -e "$after d" $tomcatConf
else
	exit 0
fi
