package com.westsword.stocks.tools.helper;


import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.mathworks.engine.MatlabEngine;

import com.westsword.stocks.tools.GetSettings;

public class MatlabConsoleDemo {
    public static void runA() {
        try {
            MatlabEngine eng = MatlabEngine.startMatlab();

            double[][] m = GetSettings.getAmMatrix("600030", "092500_145500", "20090105");
            System.out.format("m.height=%d m.width=%d\n", m.length, m[0].length);

            long start = System.currentTimeMillis();
            double[][] rm = eng.feval("corrcoef", (Object)m);
            System.out.format("rm.height=%d rm.width=%d\n", rm.length, rm[0].length);
            long end = System.currentTimeMillis();
            System.out.format("MatlabEngine.runA: matlab.corrcoef duration=%4d\n", 
                    end-start);

            eng.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void run() {
        try {
         //Start MATLAB asynchronously
            Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();

         // Get engine instance from the future result
            MatlabEngine ml = eng.get();

        /*
         * Find elements greater than 5
         * 1. Create input matrix
         * 2. Put variable matrix in the MATLAB base workspace
         * 3. Solve A(A>5) in MATLAB
         * 4. Return results and display
         * 5. Call the power function in MATLAB on the returned matrix
         * 6. Display results
         */
            double[][] input = new double[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    double num = Math.random() * 10;
                    input[i][j] = num;
                }
            }
            System.out.println("\nFind numbers from a matrix that are greater than five and square them:\n");
            System.out.println("Input matrix: ");
            for (int i = 0; i < 4; i++) {
                {
                    for (int j = 0; j < 4; j++) {
                        System.out.print(String.format("%.2f", input[i][j]) + "\t");
                    }
                    System.out.print("\n");
                }
            }

            // Put the matrix in the MATLAB workspace
            ml.putVariableAsync("A", input);

            // Evaluate the command to search in MATLAB
            ml.eval("B=A(A>5);");

            // Get result from the workspace
            Future<double[]> futureEval = ml.getVariableAsync("B");
            double[] output = futureEval.get();

            // Display result
            System.out.println("\nElements greater than 5: ");
            for (int i = 0; i < output.length; i++) {
                System.out.print(" " + String.format("%.2f", output[i]));
            }

            // Square the returned elements using the power function in MATLAB
            double[] powResult = ml.feval("power", output, Double.valueOf(2));
            System.out.println("\n\nSquare of numbers greater than 5:");
            for (int i = 0; i < powResult.length; i++) {

                //Set precision for the output values
                System.out.print(" " + String.format("%.2f", powResult[i]));
            }
            System.out.println("\n");

            // Disconnect from the MATLAB session
            ml.disconnect();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
