#coding=utf-8
#
# Copyright (C) 2019-2050 WestSword, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3, or (at your option)
# any later version.
#
# TThis program is distributed in the hope that it will be useful,
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

user = easytrader.use('ths')
user.grid_strategy = grid_strategies.Xls
user.connect(settings.sXiaDanFile)

entrustno = sys.argv[1] 
if len(sys.argv) == 3:
    key=sys.argv[2]

eList = user.history_trades
for i in eList:
    if i[settings.sEntrustIDKey] == entrustno:
        if 'key' not in locals(): 
            print(i)
        else:
            print(i[key])
