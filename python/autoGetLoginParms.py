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
import subprocess
import pywinauto
import settings



def startIdagHexin():
    subprocess.Popen([settings.idagFile, settings.hexinIdb]);
    time.sleep(settings.WaitsAfterStartingProc);
    app = pywinauto.Application().connect(path=settings.idagFile, timeout=10);
    return app

def runGetLoginParmsIdc(app):
    mainW = app['IDA - ' + settings.hexinIdb + ' (hexin.exe)'];
    mainW.wait('ready', timeout=15);
    mainW.send_keystrokes('%{F7}');

    scriptDlg = app['Please enter the script file name to execute'];
    scriptDlg.wait('ready', timeout=15);
    editCtrl = scriptDlg.child_window(control_id=settings.scriptDlg_FileName_Edit_Control_ID, 
            class_name="Edit",); 
    editCtrl.set_edit_text(settings.idcGetLoginParms);
    okCtrl = scriptDlg.child_window(control_id=settings.scriptDlg_Ok_Button_Control_ID, 
            class_name="Button",);
    okCtrl.click();

def getThsApp():
    time.sleep(settings.WaitsAfterStartingProc);
    app = pywinauto.Application().connect(path=settings.thsHexinFile, timeout=10);
    return app;

def clickHexinLogin(app):
    mainW = app['登录到中国电信行情主站']
    mainW.wait('ready', timeout=15);
    loginCtrl = mainW.child_window(control_id=settings.thsLoginDlg_Login_Button_Control_ID, class_name="Button",);
    loginCtrl.click();

def waitThs2Quit(app):
    app.wait_for_process_exit(timeout=settings.timeout_Ida_GetLoginParms);
    print(settings.thsHexinFile + " quited\n");
    
def closeIdagHexin(app):
    mainW = app['IDA - ' + settings.hexinIdb + ' (hexin.exe)'];
    mainW.wait('ready', timeout=15);
    mainW.close_alt_f4();
    saveDbDlg = app['Save database'];
    saveDbDlg.wait('ready', timeout=15);
    okCtrl=saveDbDlg.child_window(title="O&K", class_name="TButton");
    okCtrl.click();

def main():
    appIda = startIdagHexin();
    runGetLoginParmsIdc(appIda);

    appThs = getThsApp();
    clickHexinLogin(appThs);
    waitThs2Quit(appThs);

    closeIdagHexin(appIda);
    

main()

