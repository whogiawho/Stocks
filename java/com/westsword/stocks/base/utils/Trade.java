 /*
 Copyright (C) 2019-2050 WestSword, Inc.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.  */
 
 /* Written by whogiawho <whogiawho@gmail.com>. */
 
 
package com.westsword.stocks.base.utils;


import com.westsword.stocks.base.Parms;
import com.westsword.stocks.base.Settings;
import com.westsword.stocks.base.time.NatureDates;

public class Trade {

    public final static double MINIMUM_HANDLING_CHARGE = 5;
    public static double getMinBuyFee(double inPrice, int amount) {
        double cargoFee = inPrice*(double)amount;
        double normalBHC = cargoFee*Parms.BuyStockServiceRate;
        double bhc = normalBHC>MINIMUM_HANDLING_CHARGE ? normalBHC:MINIMUM_HANDLING_CHARGE;
        return cargoFee + bhc;
    }

    //with considering amount
    public static double getTradeCost(double buyPrice, double sellPrice, int amount) {
        double cost = 0.0;

        double normalBHC = buyPrice*amount*Parms.BuyStockServiceRate;
        double normalSHC = sellPrice*amount*Parms.SellStockServiceRate;
        double bhc = normalBHC>MINIMUM_HANDLING_CHARGE ? normalBHC:MINIMUM_HANDLING_CHARGE;
        double shc = normalSHC>MINIMUM_HANDLING_CHARGE ? normalSHC:MINIMUM_HANDLING_CHARGE;
        bhc = bhc/amount;
        shc = shc/amount;

        cost += bhc + shc;
        cost += sellPrice*Parms.SellStockTaxRate;

        return cost;
    }
    public static double getNetProfit(double buyPrice, double sellPrice, int amount) {
        return sellPrice - buyPrice - getTradeCost(buyPrice, sellPrice, amount);
    }
    //without considering amount
    public static double getTradeCost(double buyPrice, double sellPrice) {
        double cost = 0.0;

        cost += buyPrice*Parms.BuyStockServiceRate + sellPrice*Parms.SellStockServiceRate;
        cost += sellPrice*Parms.SellStockTaxRate;

        return cost;
    }
    public static double getNetProfit(double buyPrice, double sellPrice) {
        return sellPrice - buyPrice - getTradeCost(buyPrice, sellPrice);
    }

    public static double getMaxBuyPrice(double sellPrice, int amount, double targetYearRate,
            String inDate, String outDate) {
        NatureDates dates = new NatureDates(inDate, outDate);
        int dist = dates.getDistance(inDate, outDate);
        double expProfit = targetYearRate*dist*sellPrice/360;

        double normalSHC = sellPrice*amount*Parms.SellStockServiceRate;
        double shc = normalSHC>MINIMUM_HANDLING_CHARGE ? normalSHC:MINIMUM_HANDLING_CHARGE;
        shc = shc/amount;
        double sum = sellPrice*(1 - Parms.SellStockTaxRate) - expProfit - shc;

        double price0 = sum/(1 + Parms.BuyStockServiceRate);
        double price1 = sum - MINIMUM_HANDLING_CHARGE/amount;

        double tPrice = MINIMUM_HANDLING_CHARGE/(amount*Parms.BuyStockServiceRate);
        //System.out.format("%8.3f %8.3f %8.3f\n", price0, price1, tPrice);

        if(price0 > tPrice)
            return price0;
        else
            return Math.min(price1, tPrice);
    }
    public static double getMinSellPrice(double buyPrice, int amount, double targetYearRate, 
            String inDate, String outDate) {
        NatureDates dates = new NatureDates(inDate, outDate);
        int dist = dates.getDistance(inDate, outDate);
        double expProfit = targetYearRate*dist*buyPrice/360;

        double normalBHC = buyPrice*amount*Parms.BuyStockServiceRate;
        double bhc = normalBHC>MINIMUM_HANDLING_CHARGE ? normalBHC:MINIMUM_HANDLING_CHARGE;
        bhc = bhc/amount;
        double sum = expProfit + buyPrice + bhc;

        double price0 = sum/(1-Parms.SellStockServiceRate-Parms.SellStockTaxRate);
        double price1 = (sum+MINIMUM_HANDLING_CHARGE/amount)/(1-Parms.SellStockTaxRate);

        double tPrice = MINIMUM_HANDLING_CHARGE/(amount*Parms.SellStockServiceRate);
        //System.out.format("%8.3f %8.3f %8.3f\n", price0, price1, tPrice);

        if(price1 < tPrice)
            return price1;
        else
            return Math.max(price0, tPrice);
    }






    //2 kinds of scenratios:
    //  targetRate<=MaxGrowthRate          [0, MaxGrowthRate], percent>MaxGrowthRate is not allowed
    //  targetRate>MaxGrowthRate           (0, ...)
    //Parms:
    //  dist - distance by days
    public static double getTargetProfit(double targetRate, double inPrice, int dist) {
        double targetProfit = inPrice*targetRate*dist/360;

        double maxGrowthRate = Settings.getMaxGrowthRate();
        if(targetRate>maxGrowthRate) {
            targetProfit = targetRate - maxGrowthRate;
        }

        return targetProfit;
    }
    //in one year
    public static double getTargetProfit(double targetRate, double inPrice) {
        return getTargetProfit(targetRate, inPrice, 360);
    }
}
