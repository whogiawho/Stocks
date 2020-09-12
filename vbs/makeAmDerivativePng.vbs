Option Explicit

On Error Resume Next

Dim xlApp 
xlApp.Visible = False
Set xlApp = GetObject("d:\Stocks\doc\macros.xlsm").Application
xlApp.DisplayAlerts = False

Dim objArgs
set objArgs = Wscript.Arguments
Dim sAmDerivativeFile
Dim sPngFile
sAmDerivativeFile = objArgs(0)
sPngFile = objArgs(1)

xlApp.ScreenUpdating = False
  Dim xlMacros
  Set xlMacros = xlApp.Workbooks.Open("d:\Stocks\doc\macros.xlsm", 0, True) 
  xlApp.Run "macros.xlsm!AmDerivative.makePng", ""+sAmDerivativeFile, ""+sPngFile
xlApp.ScreenUpdating = True

Set xlApp = Nothing 
