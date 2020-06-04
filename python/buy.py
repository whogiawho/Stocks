#coding=utf-8
#
# Copyright (C) 2019-2050 WestSword, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3, or (at your option)
# any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, see <https://www.gnu.org/licenses/>.
#
# Written by whogiawho <whogiawho@gmail.com>.


import sys
import settings
import easytrader
from easytrader import grid_strategies

def marketBuy(stockCode, marketPrice, amount):
    marketPrice = float(1+0.07)*float(marketPrice)
    d = user.buy(stockCode, price=marketPrice, amount=amount)
    return d

user = easytrader.use('ths')
user.connect(settings.sXiaDanFile)
user.grid_strategy = grid_strategies.Xls

stockCode=sys.argv[1] #'600030'
price=sys.argv[2]
amount=sys.argv[3]
if len(sys.argv) == 5:
    marketPrice=float(sys.argv[4])

if 'marketPrice' not in locals():
    d = user.buy(stockCode, price=price, amount=amount)
else:
    d = marketBuy(stockCode, marketPrice, amount)
print(d['entrust_no'])

