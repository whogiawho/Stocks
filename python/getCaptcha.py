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


import os
import shutil
import tempfile
import hexinQsClient
import xiadanQsClient 


def autoHexin(okDir, failDir):
    hxclient = hexinQsClient.HexinQsClient();
    while True:
        #start qs hexin.exe
        hxclient.start();
        (sCaptcha, sInPng) = hxclient.getCaptchaString(False)
    
        bOK = hxclient.loginOk(sCaptcha)
        if bOK:
            #move sInPng to ok dir
            shutil.move(sInPng, okDir)
            #quit XiaDan
            xdclient = xiadanQsClient.XiadanQsClient();
            xdclient.quit();
        else:
            #move sInPng to fail dir
            shutil.move(sInPng, failDir)





def main():
    tmpDir=tempfile.gettempdir();
    okDir=tmpDir+"\\cOk"
    os.makedirs(okDir, exist_ok=True)
    failDir=tmpDir+"\\cFail"
    os.makedirs(failDir, exist_ok=True)
    autoHexin(okDir, failDir)

main()
