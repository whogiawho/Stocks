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
import re
import time
import tempfile
import subprocess
import pywinauto
import settings

class HexinLoginCtrls:
    def __init__(self, mainW):
        self.loginWindow = mainW.child_window(title=settings.sLogin_Title, 
                class_name="Button",)
        self.closeWindow = mainW.child_window(control_id=settings.iClose_Control_ID, 
                class_name="Button",)
        self.captchaWindow = mainW.child_window(control_id=settings.iCaptcha_Control_ID, 
                class_name="Static",)
        self.captchaInputWindow = mainW.child_window(control_id=settings.iCaptcha_Input__Control_ID, 
                class_name="Edit",)
        self.accountInputWindow = mainW.child_window(control_id=settings.iAccount_Input_Control_ID, 
                class_name="Edit",)
        self.passwordInputWindow = mainW.child_window(control_id=settings.iPassword_Input_Control_ID, 
                class_name="Edit",)

class HexinQsClient:
    def start(self):
        #start qs hexin.exe
        os.startfile(settings.sHexinFile)
        time.sleep(settings.WaitsAfterStartingProc)
    
        self.app = pywinauto.Application().connect(path=settings.sHexinFile, timeout=10)
        self.mainW = self.app.top_window()
        self.mainW.wait('ready', timeout=15);
        self.ctrls = HexinLoginCtrls(self.mainW)

    def _getCaptchaString(self, ctrls, bRemoveTmpImage):
        #get the CAPTCHA image
        img = ctrls.captchaWindow.capture_as_image()
        tempIn_path = tempfile.mktemp(suffix=".png")
        img.save(tempIn_path, dpi=(96,96))
        print(tempIn_path)
        
        tempOut_path = tempfile.mktemp()
        print(tempOut_path)
        
        #run tesseract to turn CAPTCHA image into txt
        cmdList=[settings.tesseractFile, tempIn_path, tempOut_path];
        optionList=settings.tesseractOptions.split(" ");
        cmdList.extend(optionList);
        subprocess.run(cmdList);
        #get the string of tempOut_path's 1st line
        tempOutTxt_path = tempOut_path+".txt"
        sCompletion=subprocess.run([settings.headFile, "-n 1", tempOutTxt_path], capture_output=True)
        sCaptcha=sCompletion.stdout.decode("utf-8")
    
        if bRemoveTmpImage:
            os.remove(tempIn_path)
        os.remove(tempOutTxt_path)
    
        return (sCaptcha, tempIn_path)

    def getCaptchaString(self, bRemoveTmpImage):
        return self._getCaptchaString(self.ctrls, bRemoveTmpImage)

    def loginOk(self, sCaptcha):
        return self._loginOk(sCaptcha, self.app, self.ctrls);

    def _loginOk(self, sCaptcha, app, ctrls):
        #if txt is invalid, kill hexin.exe and loop until a valid one is returned
        pattern = re.compile("^[0-9]{4}\n$")
        match = pattern.match(sCaptcha)
        if match!=None:
            self.inputLoginInfo(sCaptcha, ctrls)
            #click login
            ctrls.loginWindow.click()
            try:
                app.wait_for_process_exit(timeout=10)
            except pywinauto.timings.TimeoutError:
                print("incorrect:" + sCaptcha)
                self.quitHeXin(ctrls)
                return False
            return True 
        else:
            print("nomatch:" + sCaptcha)
            self.quitHeXin(ctrls)
            return False

    def inputLoginInfo(self, sCaptcha, ctrls):
        ctrls.accountInputWindow.set_edit_text(settings.sAccount);
        ctrls.passwordInputWindow.set_edit_text(settings.sPassword);
        
        #remove \n
        sCaptcha = sCaptcha.rstrip()
        print("match:" + sCaptcha)
        ctrls.captchaInputWindow.set_edit_text(sCaptcha)
    
    def quitHeXin(self, ctrls):
        #click the close button
        ctrls.closeWindow.click()




