import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.lang.*;

public class FibMatrixExperiment {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static int numberOfTrials = 1000000;
    static int MAXINPUTSIZE  = 91;
    static int MININPUTSIZE  =  0;
    static String ResultsFolderPath = "/home/codyschroeder/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("FibMatrix-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("FibMatrix-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("FibMatrix-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName) {

        long answer = 0;

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);

        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#    X(Input value)      N(Input size)   T(avg runtime)    Fib(x)(result)"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize++) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");
            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            System.out.print("    Running trial batch...");
            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();
            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {

                /* force garbage collection before each trial run so it is not included in the time */
                //System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                answer = FibMatrix(inputSize);
                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }

            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            //calculate N(input size)
            double N;
            if (inputSize == 0)
                N = 1;
            else {
                N = Math.floor(Math.log(inputSize) / Math.log(2)) + 1;
            }
            /* print data for this size of input */
            resultsWriter.printf("%12d      %15.0f    %15.2f     %12d\n", inputSize, N, averageTimePerTrialInBatch, answer); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    //wrapper function
    public static long FibMatrix(long x){

        //declare result variable
        long result;

        //initialize  matrices
        long originalMatrix[][] = {{1, 1},
                                   {1, 0}};
        long lastMatrix[][] = {{1, 0},
                               {0, 1}};

        //call helper function
        MatrixPower(originalMatrix, lastMatrix, x);

        //find and return result
        result = lastMatrix[0][0];
        return result;
    }

    //use matrix multiplication to find result function
    static void MatrixPower(long matrix[][], long resultMatrix[][], long x) {

        //declare temp variable
        long temp;

        //create temp matrix
        long tempMatrix[][] = {{matrix[0][0], matrix[0][1]},
                {matrix[1][0], matrix[1][1]}};

        //declare variables to hold values
        long topLeft, topRight, bottomLeft, bottomRight;

        for (temp = x; temp > 0; temp = temp / 2) {

            //check if leftmost bit is 1
            if (temp % 2 == 1) {
                //multiply matrices
                topLeft = resultMatrix[0][0] * tempMatrix[0][0] + resultMatrix[0][1] * tempMatrix[1][0];
                topRight = resultMatrix[0][0] * tempMatrix[0][1] + resultMatrix[0][1] * tempMatrix[1][1];
                bottomLeft = resultMatrix[1][0] * tempMatrix[0][0] + resultMatrix[1][1] * tempMatrix[1][0];
                bottomRight = resultMatrix[1][0] * tempMatrix[0][1] + resultMatrix[1][1] * tempMatrix[1][1];
                //set values
                resultMatrix[0][0] = topLeft;
                resultMatrix[0][1] = topRight;
                resultMatrix[1][0] = bottomLeft;
                resultMatrix[1][1] = bottomRight;
            }

            //square temp matrix
            topLeft = tempMatrix[0][0] * tempMatrix[0][0] + tempMatrix[0][1] * tempMatrix[1][0];
            topRight = tempMatrix[0][0] * tempMatrix[0][1] + tempMatrix[0][1] * tempMatrix[1][1];
            bottomLeft = tempMatrix[1][0] * tempMatrix[0][0] + tempMatrix[1][1] * tempMatrix[1][0];
            bottomRight = tempMatrix[1][0] * tempMatrix[0][1] + tempMatrix[1][1] * tempMatrix[1][1];
            //set values
            tempMatrix[0][0] = topLeft;
            tempMatrix[0][1] = topRight;
            tempMatrix[1][0] = bottomLeft;
            tempMatrix[1][1] = bottomRight;
        }
    }
}
