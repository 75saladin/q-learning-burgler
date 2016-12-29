/**
 *  Name: Lucas Saladin
 *  Course: CIS 421 AI
 *  Assignment: 5
 *  Due:  12/7/2016
 */

import java.util.*;
import java.io.*;

public class QLearn {
    public static String OUTPUT_FILENAME = "output.txt";

    public static void main(String[] args) {
        //Set up helper variables
        PrintStream[] outs = cmdAndFile();
        Random r = new Random();
        //Set up environment, agent, and learning parameters
        Environment burglerWorld = new Environment(paramCheck(args));
        double alpha = Double.parseDouble(args[1]);
        double gamma = Double.parseDouble(args[2]);
        int epochs = Integer.parseInt(args[3]);
        Agent burgler = new Agent(burglerWorld.getBurgler(), alpha, gamma, 
            burglerWorld.NEIGHBOR_COUNT);
        
        //Main loop. Prints board before and when the burgler wins/dies
        burglerWorld.print(outs);
        for (int epoch=0; epoch<epochs; epoch++) 
            runIteration(burglerWorld, burgler, r, "explore", outs, args[0]);
            
        //We're done learning. Start over and set Agents to "kill" (ie greedy)
        burglerWorld = startOver(burgler, args[0]);
        while (!burglerWorld.hasTerm())
            runIteration(burglerWorld, burgler, r, "greedy", outs, args[0]);
        burglerWorld.print(outs);
    }
    
    /**
     * Runs an iteration (aka a turn) of the burgler world.
     * @param burglerWorld The Environment to run
     * @param burgler The Agent in that Environment
     * @param r The Random object to use for random things
     * @param type The type of action selection for the Agent
     * @param outs The PrintStreams to print output to
     * @param f The filename of the input file for resetting the Environment
     */
    public static void runIteration(Environment burglerWorld, Agent burgler,
                        Random r, String type, PrintStream[] outs, String f) {
        if (burglerWorld.hasTerm()) { //if burgler has won/died
            //burglerWorld.print(outs);
            burglerWorld = startOver(burgler, f);
        }
        /* Agent makes a move on the board. Since agent knows nothing about
         * the rules or board other than arbitrary action choices, the 
         * board must feed result location and reward back to the agent. */
        int move = burgler.selectMove(r, type);
        Object[] feedback = burglerWorld.makeMove(move);
        burgler.giveFeedback(feedback);
    }
    
    /**
     * Resets the Environment and tells the burgler its new random start spot.
     * @param burgler The Agent in that Environment
     * @param f The filename of the input file for fresh Environment rebuilding
     * @return the fresh Environment
     */
    public static Environment startOver(Agent burgler, String f) {
        Environment fresh = new Environment(getScanner(f));
        burgler.startOver(fresh.getBurgler());
        return fresh;
    }
    
    /**
     * Checks if the command line parameters are as expected. If not, prints 
     * error message and terminates. Currently, there should be four params, 
     * an input file name followed by three parameters to the Q Learning 
     * algorithm. The file is expected to be an existing, readable file.
     * Use this method when initializing the first Environment. Later, use 
     * getScanner() instead to skip parameter checking.
     * @param args The command line arguments
     * @return a scanner over the input file.
     */
    private static Scanner paramCheck(String[] args) {
        if (args.length!=4) error("cmd");
        Scanner sc = getScanner(args[0]);
        //See if parameters are the right type of value
        try {
            Double.parseDouble(args[1]); //alpha
            Double.parseDouble(args[2]); //gamma
            Integer.parseInt(args[3]); //Epochs
        } catch (NumberFormatException e) {
            error("param");
        }
        return sc;
    }
    
    /**
     * Gets a Scanner over the given filename, processing errors.
     * @param filename
     * @return a Scanner over that filename
     */
    public static Scanner getScanner(String filename) {
        File f = new File(filename);
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
            case "cmd":
                message = "Please provide four command line parameters: An " +
                            "File name, a learning rate, a discount factor, " +
                            "and a number of epochs to run for.";
                break;
            case "param":
                message = "Please make sure that the learning rate and " +
                             "discount factors are expressed as real numbers" + 
                             " and that the number of epochs is an integer.";
                break;
            case "out":
                message = "Output file could not be created. Check that you" +
                            "have the right permissions for this location.";
                break;
        }
        System.out.println(message);
        System.exit(1);
    }
}
