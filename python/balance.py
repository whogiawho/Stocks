#coding=utf-8

import sys 
import settings
import easytrader
from easytrader import grid_strategies

user = easytrader.use('ths')
user.grid_strategy = grid_strategies.Xls
user.connect(settings.sXiaDanFile)

if len(sys.argv) == 2:
    key=sys.argv[1]

bDict=user.balance
if 'key' not in locals(): 
    print(bDict)
else:
    print(bDict[key])
