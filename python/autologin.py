#coding=utf-8
#
# Copyright (C) 2019-2050 WestSword, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3, or (at your option)
# any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, see <https://www.gnu.org/licenses/>.
#
# Written by whogiawho <whogiawho@gmail.com>.


import hexinQsClient
import xiadanQsClient 




def autoXiaDan():
    xdclient = xiadanQsClient.XiadanQsClient();
    xdclient.restore();


def autoHexin():
    hxclient = hexinQsClient.HexinQsClient();
    while True:
        #start qs hexin.exe
        hxclient.start();
        (sCaptcha, sInPng) = hxclient.getCaptchaString(True)
    
        bOK = hxclient.loginOk(sCaptcha)
        #break if it is an ok login; else continue
        if bOK:
            break
        else:
            continue

def main():
    autoHexin()
    #xiadan.exe will be on normally after logging in hexin.exe
    autoXiaDan()

main()
