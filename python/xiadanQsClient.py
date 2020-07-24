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


import time
import settings
import pywinauto

class XiaDanCtrls:
    def __init__(self, mainW):
        self.restoreWindow = mainW.child_window(control_id=settings.iRestore_Control_ID, 
                class_name="Button",)
        self.quitWindow = mainW.child_window(control_id=settings.iQuit_Control_ID, 
                class_name="Button",)

class XiadanQsClient:
    def isBulletinVisible(self, mainW):
        try:
            sOk = "确定"
            okButton = mainW.child_window(title=sOk)
            if okButton.is_visible():
                okButton.click()    #active mainW
                okButton.click()    #click okButton
                print("click " + sOk)
                return True 
        except pywinauto.findbestmatch.MatchError:
            print(sOk + ": nomatch")
            return False
        except pywinauto.timings.TimeoutError:
            print(sOk + ": timeout")
            return False
        except pywinauto.findwindows.ElementNotFoundError:
            print(sOk + ": notfound")
            return False
    def isInfoHintVisible(self, mainW):
        try:
            sInfoHint = "信息提示"
            testW = mainW.child_window(best_match=sInfoHint)
            if testW.is_visible():
                closeW = mainW.child_window(control_id=settings.iClose_InfoHint_ID, 
                        class_name="Button")
                closeW.click()      #active mainW
                closeW.click()      #click closeButton
                print("close " + sInfoHint)
                return True 
        except pywinauto.findbestmatch.MatchError:
            print(sInfoHint + ": nomatch")
            return False
        except pywinauto.timings.TimeoutError:
            print(sInfoHint + ": timeout")
            return False
        except pywinauto.findwindows.ElementNotFoundError:
            print(sInfoHint + ": notfound")
            return False
    def isAfxVisible(self, mainW):
        try:
            testW = mainW.child_window(best_match="Afx:400000:0:0:")
            if testW.is_visible():
                return True 
        except pywinauto.findbestmatch.MatchError:
            print("Afx: nomatch")
            return False
        except pywinauto.timings.TimeoutError:
            print("Afx: timeout")
            return False
        except pywinauto.findwindows.ElementNotFoundError:
            print("Afx: notfound")
            return False

    def waitForAfxReady(self):
        #wait some time to makre sure xiadan is running
        time.sleep(settings.WaitsAfterStartingProc)
        #connect to xiadan.exe
        self.app = pywinauto.Application().connect(path=settings.sXiaDanFile, timeout=10)
        #loop all top_windows until the main window is visible
        while True:
            mainW = self.app.top_window()
            mainW.print_ctrl_ids(depth=1)
            mainW.wait("exists enabled visible ready")
    
            if self.isAfxVisible(mainW):
                return mainW
            else:
                #remove the infohint window if it is ready
                self.isInfoHintVisible(mainW)
    
                #remove the bulletin window if it is ready
                self.isBulletinVisible(mainW)
                continue

    def restore(self):
        afxW = self.waitForAfxReady()
        ctrls = XiaDanCtrls(afxW)
        #click 还原
        ctrls.restoreWindow.click()

    def quit(self):
        afxW = self.waitForAfxReady()
        ctrls = XiaDanCtrls(afxW)
        #click 退出
        ctrls.quitWindow.click()

    def kill(self):
        afxW = self.waitForAfxReady()
        self.app.kill()


