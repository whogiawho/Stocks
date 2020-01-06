#coding=utf-8
import os
import sys

qsIdx=0
if 'qsIdx' in os.environ:
    qsIdx=int(os.environ['qsIdx'])
print('qsIdx = '+str(qsIdx), file=sys.stderr)

WaitsAfterStartingProc=10
headFile="d:\\cygwin64\\bin\\head.exe"
jyXiadanFile=r'd:\Program Files (x86)\jyzqths\xiadan.exe'
zxXiadanFile=r'd:\Program Files (x86)\中信证券至胜全能版\xiadan.exe'
jyHexinFile=r'd:\Program Files (x86)\jyzqths\hexin.exe'
zxHexinFile=r'd:\Program Files (x86)\zxzqzs\hexin.exe'
tesseractFile="e:\\cygwin64\\bin\\tesseract.exe"
tesseractOptions="--oem 0 --psm 7"

qsNames = ['jyzq', 'zxzq']
qsXiadanFilePaths = [jyXiadanFile, zxXiadanFile]
qsHexinFilePaths = [jyHexinFile, zxHexinFile]
qsEntrustIDKeys = ['合同编号', '委托编号']
qsStockCodeKeys = ['证券代码', '证券代码']

sXiaDanFile=qsXiadanFilePaths[qsIdx]
sHexinFile=qsHexinFilePaths[qsIdx]
sEntrustIDKey=qsEntrustIDKeys[qsIdx]
sStockCodeKey=qsStockCodeKeys[qsIdx]

qsAccount = ['53000065', '880000825911']
qsPassword = ['511388', '769301']
sAccount=qsAccount[qsIdx]
sPassword=qsPassword[qsIdx]

qsLOGIN_TITLE = ["登录", "登录"]
qsCLOSE_CONTROL_ID = [22269, 22269]
qsCAPTCHA_CONTROL_ID = [22202, 22202]
qsCAPTCHA_INPUT_CONTROL_ID = [22201, 22201]
qsACCOUNT_INPUT_CONTROL_ID = [1001, 1001]
qsPASSWORD_INPUT_CONTROL_ID = [21812, 21812]
qsRESTORE_CONTROL_ID = [32812, 32812]
qsQuit_CONTROL_ID = [32820, 32820]
qsCLOSE_INFOHINT_ID = [1003, 1003]

sLogin_Title = qsLOGIN_TITLE[qsIdx]
iClose_Control_ID=qsCLOSE_CONTROL_ID[qsIdx]
iCaptcha_Control_ID=qsCAPTCHA_CONTROL_ID[qsIdx]
iCaptcha_Input__Control_ID=qsCAPTCHA_INPUT_CONTROL_ID[qsIdx]
iAccount_Input_Control_ID=qsACCOUNT_INPUT_CONTROL_ID[qsIdx]
iPassword_Input_Control_ID=qsPASSWORD_INPUT_CONTROL_ID[qsIdx]
iRestore_Control_ID=qsRESTORE_CONTROL_ID[qsIdx]
iQuit_Control_ID=qsQuit_CONTROL_ID[qsIdx]
iClose_InfoHint_ID=qsCLOSE_INFOHINT_ID[qsIdx]

#automate getLoginParms
idagFile="d:\hri61pa\Hex-Rays IDA 6.1 Pro Andvanced\Hex-Rays.IDA.Pro.Advanced.v6.1.Windows.incl.Hex-Rays.x86.Decompiler.v1.5.READ.NFO-RDW\ida61\idag.exe"
hexinIdb="d:\HexinSoftware\Hexin\hexin.idb"
idcGetLoginParms="d:\stocks\scripts\getLoginParms.idc"
thsHexinFile=r'd:\HexinSoftware\Hexin\hexin.exe'
scriptDlg_FileName_Edit_Control_ID=1148
scriptDlg_Ok_Button_Control_ID=1
thsLoginDlg_Login_Button_Control_ID=1
timeout_Ida_GetLoginParms=300

