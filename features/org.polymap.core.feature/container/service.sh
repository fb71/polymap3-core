#!/bin/sh
#
# Start POLYMAP3 as a service.
# This script depends on the PolymapServiceController to actually
# start/stop/control the service. The controller support log file
# rotation, status check, ...
#
# Copy this script to /etc/init.d/<SERVICENAME>
#
### BEGIN INIT INFO
# Provides: polymap3
# Required-Start: $network
# Required-Stop: $network
# Default-Start: 2
# Default-Stop: 2
# Description: Start a POLYMAP3 service instance
### END INIT INFO

# Change this to be the install dir of POLYMAP3 
POLYMAPDIR=`dirname $0`
# The user to start this service for
USER=`whoami`

EXE=$POLYMAPDIR/start.sh
SERVICENAME=Polymap3
LOG=$POLYMAPDIR/logs/$SERVICENAME.log

java -jar $POLYMAPDIR/PolymapServiceController.jar -exe $EXE -serviceName $SERVICENAME -log $LOG -user $USER $1 