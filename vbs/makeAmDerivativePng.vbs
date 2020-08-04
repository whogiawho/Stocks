Option Explicit

On Error Resume Next

Dim xlApp 
Set xlApp = GetObject("d:\stocks\doc\macros.xlsm").Application
xlApp.Visible = True
xlApp.DisplayAlerts = False

Dim objArgs
set objArgs = Wscript.Arguments
Dim sAmDerivativeFile
Dim sPngFile
sAmDerivativeFile = objArgs(0)
sPngFile = objArgs(1)

Dim xlMacros
Set xlMacros = xlApp.Workbooks.Open("d:\Stocks\doc\macros.xlsm", 0, True) 
xlApp.Run "macros.xlsm!AmDerivative.makePng", ""+sAmDerivativeFile, ""+sPngFile

Set xlApp = Nothing 
