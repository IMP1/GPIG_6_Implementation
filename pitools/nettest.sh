#!/bin/sh
# Looks at the network connections and diddles the default route as appropriate

ethstatus="eth"

while [ 1 -eq 1 ] ; do
	eth0=`ifconfig | grep -A 7 eth0 | grep RUNNING`
	wlan0=`ifconfig | grep -A 7 wlan0 | grep RUNNING`
	if [ -z "$eth0" ] ; then
		if [ $ethstatus = "eth" ] ; then
			echo "eth0 down!"
			route del default
			route add default gw 192.168.1.202 wlan0
			ethstatus="wlan"
			echo "Changed route to wireless"
		fi
	elif [ -n "$eth0" ] ; then
		if [ $ethstatus != "eth" ] ; then
			echo "eth0 back up"
			echo "Waiting a moment..."
			sleep 4
			route del default
			route add default gw 192.168.1.1 eth0
			ethstatus="eth"
			echo "Changed route to wired" 
		fi
	fi
	if [ -z "$wlan0" ] ; then
		if [ $ethstatus = "wlan" ] ; then
			echo "Both interfaces down!"
			ethstatus="fail"
		fi
	elif [ -n "$wlan0" ] ; then
		if [ $ethstatus = "fail" ] ; then
			echo "wlan0 back, readding route";
			route add default gw 192.168.1.202 wlan0
			ethstatus="wlan"
		fi
	fi
	sleep 1
done 
