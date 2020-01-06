#coding=utf-8

import sys 
import settings
import easytrader
from easytrader import grid_strategies

user = easytrader.use('ths')
user.grid_strategy = grid_strategies.Xls
user.connect(settings.sXiaDanFile)

user.refresh()
