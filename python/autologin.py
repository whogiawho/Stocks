import os
import re
import sys
import time
import tempfile
import subprocess
import pywinauto
import settings


def getCaptchaString(ctrls):
    #get the CAPTCHA image
    img = ctrls.captchaWindow.capture_as_image()
    tempIn_path = tempfile.mktemp(suffix=".png")
    img.save(tempIn_path, dpi=(96,96))
    print(tempIn_path)
    
    tempOut_path = tempfile.mktemp()
    print(tempOut_path)
    
    #run tessract to turn CAPTCHA image into txt
    subprocess.run([settings.tessractFile, tempIn_path, tempOut_path])
    #get the string of tempOut_path's 1st line
    tempOutTxt_path = tempOut_path+".txt"
    sCompletion=subprocess.run([settings.headFile, "-n 1", tempOutTxt_path], capture_output=True)
    sCaptcha=sCompletion.stdout.decode("utf-8")

    os.remove(tempIn_path)
    os.remove(tempOutTxt_path)

    return sCaptcha

def inputLoginInfo(sCaptcha, ctrls):
    ctrls.accountInputWindow.set_edit_text(settings.sAccount);
    ctrls.passwordInputWindow.set_edit_text(settings.sPassword);
    
    #remove \n
    sCaptcha = sCaptcha.rstrip()
    print("match:" + sCaptcha)
    ctrls.captchaInputWindow.set_edit_text(sCaptcha)

def quitHeXin(ctrls):
    #click the close button
    ctrls.closeWindow.click()

def loginOk(sCaptcha, app, ctrls):
    #if txt is invalid, kill hexin.exe and loop until a valid one is returned
    pattern = re.compile("^[0-9]{4}\n$")
    match = pattern.match(sCaptcha)
    if match!=None:
        inputLoginInfo(sCaptcha, ctrls)
        #click login
        ctrls.loginWindow.click()
        try:
            app.wait_for_process_exit(timeout=10)
        except pywinauto.timings.TimeoutError:
            print("incorrect:" + sCaptcha)
            quitHeXin(ctrls)
            return False
        return True 
    else:
        print("nomatch:" + sCaptcha)
        quitHeXin(ctrls)
        return False

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

class XiaDanCtrls:
    def __init__(self, mainW):
        self.restoreWindow = mainW.child_window(control_id=settings.iRestore_Control_ID, 
                class_name="Button",)

def isBulletinVisible(mainW):
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
def isInfoHintVisible(mainW):
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
def isAfxVisible(mainW):
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

def autoXiaDan():
    #wait some time to makre sure xiadan is running
    time.sleep(settings.WaitsAfterStartingProc)
    #connect to xiadan.exe
    app = pywinauto.Application().connect(path=settings.sXiaDanFile, timeout=10)
    #loop all top_windows until the main window is visible
    while True:
        mainW = app.top_window()
        mainW.print_ctrl_ids(depth=1)
        mainW.wait("exists enabled visible ready")

        if isAfxVisible(mainW):
            break
        else:
            #remove the infohint window if it is ready
            isInfoHintVisible(mainW)

            #remove the bulletin window if it is ready
            isBulletinVisible(mainW)
            continue

    ctrls = XiaDanCtrls(mainW)
    #click 还原
    ctrls.restoreWindow.click()

def autoHexin():
    while True:
        #start qs hexin.exe
        os.startfile(settings.sHexinFile)
        time.sleep(settings.WaitsAfterStartingProc)
    
        app = pywinauto.Application().connect(path=settings.sHexinFile, timeout=10)
        mainW = app.top_window()
        ctrls = HexinLoginCtrls(mainW)
        sCaptcha = getCaptchaString(ctrls)
    
        bOK = loginOk(sCaptcha, app, ctrls)
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
