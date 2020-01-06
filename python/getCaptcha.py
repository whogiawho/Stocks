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
