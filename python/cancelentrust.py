import sys 
import settings
import easytrader
from easytrader import grid_strategies

user = easytrader.use('ths')
user.grid_strategy = grid_strategies.Xls
user.connect(settings.sXiaDanFile)

entrustno = price=sys.argv[1] 
d = user.cancel_entrust(entrustno)
print(d['message'])
