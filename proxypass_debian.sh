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
