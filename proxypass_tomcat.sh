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
#AWS support
ipaddress=$(curl http://instance-data/latest/meta-data/public-ipv4 2>/dev/null)
if [[ "x$ipaddress" == "x" ]] ; then
    ipaddress=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p')
fi
connector8081="<Connector port=\"8081\" proxyName=\"$ipaddress\" proxyPort=\"80\"\/>"


if ! grep -q "<Connector port=\"8081" $tomcatConf ; then
    sed -i "/Service name=\"Catalina\">/ a $connector8081" $tomcatConf
    service $tomcatService restart
fi
