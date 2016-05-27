#!/bin/sh

ps ax | grep dhcp | awk '{print $1}' | xargs kill
