#!/bin/sh

ps ax | grep dhcp | awk '{print $1}' | xargs kill
iptables -t mangle -A OUTPUT -d 234.0.0.12 -o wlan0 -j TTL --ttl-set 2
iptables -t mangle -A INPUT -d 234.0.0.12 -i eth0 -j TTL --ttl-set 2
ifdown eth0
ifup eth0
smcroute -D -d
