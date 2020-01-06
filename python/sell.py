import sys
import settings
import easytrader
from easytrader import grid_strategies

def marketSell(stockCode, marketPrice, amount):
    marketPrice = float(1-0.07)*float(marketPrice)
    d = user.sell(stockCode, price=marketPrice, amount=amount)
    return d

user = easytrader.use('ths')
user.grid_strategy = grid_strategies.Xls
user.connect(settings.sXiaDanFile)

stockCode=sys.argv[1] #'600030'
price=sys.argv[2]
amount=sys.argv[3]
if len(sys.argv) == 5:
    marketPrice=sys.argv[4]

if 'marketPrice' not in locals():
    d = user.sell(stockCode, price=price, amount=amount)
else:
    d = marketSell(stockCode, marketPrice, amount)
print(d['entrust_no'])

