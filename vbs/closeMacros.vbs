Option Explicit

On Error Resume Next

Dim xlApp 
Set xlApp = GetObject("d:\Stocks\doc\macros.xlsm").Application
xlApp.Visible = True
xlApp.DisplayAlerts = False

xlApp.quit

Set xlApp = Nothing 
