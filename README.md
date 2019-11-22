# Stocks
Analyze level2 data of Chinese stockA and auto trade  
It uses the output of thsHack(a project to retrieve leve2 data) to analyze a specific stockCode, and decide the time to buy and sell

# Assumption
The future may be decided by the past.  

# Overview  
All trade dates are lined and their trade time are coded to a series of number.  
A search is done to group similar segments for this time line.  
The segment endtime is the timepoint to buy.  
A specific time length following these segments are the window to sell.  
Another search is done to get the offset of the exact time point in the window to get max profit for these segments.  
So next time when a similar segment appears, a buy should be done, and it should be sold after the offset.  

# Investment and Cooperation are Welcome
 contact me with whogiawho@gmail.com, thanks!
