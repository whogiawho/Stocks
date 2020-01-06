#coding=utf-8

import sys 
import settings
import easytrader
from easytrader import grid_strategies

user = easytrader.use('ths')
user.grid_strategy = grid_strategies.Xls
user.connect(settings.sXiaDanFile)

entrustno = sys.argv[1] 
if len(sys.argv) == 3:
    key=sys.argv[2]

eList = user.today_entrusts
for i in eList:
    if i[settings.sEntrustIDKey] == entrustno:
        if 'key' not in locals(): 
            print(i)
        else:
            print(i[key])
