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
