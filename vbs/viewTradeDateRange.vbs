Option Explicit

'On Error Resume Next




Dim xlApp 
Set xlApp = CreateObject("Excel.Application") 
xlApp.Visible = True
xlApp.DisplayAlerts = False

Dim objArgs
set objArgs = Wscript.Arguments
Dim sStockCode 
Dim sTradeDate
Dim startHexTp
Dim endHexTp
Dim startHMS
Dim endHMS
sStockCode = objArgs(0)
sTradeDate = objArgs(1)
startHexTp = objArgs(2)
endHexTp = objArgs(3)
startHMS = objArgs(4)
endHMS = objArgs(5)
'Wscript.Echo "stockCode=" + sStockCode
'Wscript.Echo "tradeDate=" + sTradeDate
'Wscript.Echo "startHexTp=" + startHexTp
'Wscript.Echo "endHexTp=" + endHexTp

'open macros.xlsm
Dim xlMacros
Set xlMacros = xlApp.Workbooks.Open("d:\Stocks\doc\macros.xlsm", 0, True) 
Dim sDailyXlsx
sDailyXlsx = xlApp.Run("utils.getDailyXlsx", ""+sStockCode, ""+sTradeDate)
'Wscript.Echo "dailyXlsx=" + sDailyXlsx

'new a workbook, and import [stockCode]/analysis.txt
Dim xlNew
set xlNew = xlApp.Workbooks.Add()
xlNew.Activate
xlNew.Worksheets("Sheet1").Activate
xlApp.Run "macros.xlsm!import", ""+sStockCode, ""+sTradeDate

'search the row where startHexTp&endHexTp are, and add chart
xlApp.Run "macros.xlsm!AmCurve.addChart", _
    ""&sTradeDate, ""&startHexTp, ""&endHexTp, ""&startHMS, ""&endHMS



'close some workbooks
Dim w
For Each w In xlApp.Workbooks
    'Wscript.echo w.Name
    if InStr(w.Name, "macros.xlsm") Then
        w.Close 
    end if
Next 

'xlApp.Quit 

Set xlMacros = Nothing 
Set xlApp = Nothing 




'excel this version does not support Match, below code prove it
'Dim myRange
'Set myRange = xlNew.Worksheets(sTradeDate).Range("C1:C5000") 
'Dim hms
'hms = xlApp.WorksheetFunction.Index(myRange, 1)                               'does work
'hms = xlApp.WorksheetFunction.Match(TimeValue(startHMS), myRange, 1)          'does not work
'Wscript.echo "hms= " &hms

