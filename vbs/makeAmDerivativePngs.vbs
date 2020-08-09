Option Explicit

On Error Resume Next

Dim xlApp 
Set xlApp = CreateObject("Excel.Application") 
xlApp.Visible = True
xlApp.DisplayAlerts = False

Dim objArgs
set objArgs = Wscript.Arguments
Dim sAmDerivativeDir
sAmDerivativeDir = objArgs(0)

Dim xlMacros
Set xlMacros = xlApp.Workbooks.Open("d:\Stocks\doc\macros.xlsm", 0, True) 
xlApp.Run "macros.xlsm!AmDerivative.makePngs_", ""+sAmDerivativeDir

'close all workbooks
Dim workbooks
Dim wb
workbooks = xlApp.Workbooks
Dim j
For j = 1 To workbooks.Count
    wb = workbooks(j)
    xlApp.DisplayAlerts = False
    wb.Close
Next

xlApp.Quit 

Set xlMacros = Nothing 
Set xlApp = Nothing 

