/**
 *  Name: Lucas Saladin
 *  Course: CIS 421 AI
 *  Assignment: 5
 *  Due:  11/21/2016
 */

import java.util.*;
import java.io.*;

public class QLearn {
    public static String OUTPUT_FILENAME = "output.txt";

    public static void main(String[] args) {
        //Check if the command line params are right
        PrintStream[] outs = cmdAndFile();
        Environment burglerWorld = new Environment(paramCheck(args));
        Random r = new Random();
        
        burglerWorld.print(outs);
        while(!burglerWorld.hasTerm()) {
            List<Integer> moves = burglerWorld.getValidMoves();
            burglerWorld.makeMove(moves.get(r.nextInt(moves.size())));
        }
        burglerWorld.print(outs);
    }
    
    /**
     * Checks if the command line parameters are as expected. If not, prints 
     * error message and terminates. Currently, there should be one param, an 
     * input file name. It is expected to be an existing, readable file.
     * @param args The command line arguments
     * @return a scanner over the input file.
     */
    private static Scanner paramCheck(String[] args) {
        if (args.length==0) error("cmd0");
        else if (args.length>1) error("cmd+:");
        File f = new File(args[0]);
        Scanner sc = null;
        try {
            sc = new Scanner(f);
        } catch (FileNotFoundException e) {
            error("file");
        }
        return sc;
    }
    
    /**
     * Gets the array of PrintsStreams to print to. 
     * @return an array of two PrintStreams: System.out and the output file
     */
    private static PrintStream[] cmdAndFile() {
        PrintStream[] outs = new PrintStream[2];
        try {
            outs = new PrintStream[]{
                System.out, 
                new PrintStream(new File(OUTPUT_FILENAME))
            };
        } catch (FileNotFoundException e) {
            error("out");
        }
        return outs;
    }
    
    //Prints the error message associated with err and exits the program.
    private static void error(String err) {
        String message = null;
        switch (err) {
            case "cmd0":
                message = "Please provide an input file name as a command " +
                            "line argument.";
                break;
            case "cmd+":
                message = "Please provide only one command" +
                            "line argument: an input file name";
                break;
            case "out":
                message = "Output file could not be created. Check that you" +
                            "have the right permissions for this location";
                break;
        }
        System.out.println(message);
        System.exit(1);
    }
}
