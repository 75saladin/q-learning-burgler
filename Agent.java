import java.util.*;

public class Agent {
    private static double INITIAL_Q = 0;
    private static double EPSILON = 0.1;
    private static double alpha;
    private static double gamma;
    private static int NEIGHBOR_COUNT;
    /* double qValue = q.get(x).get(y).get(moveCode)
     * q.get(x).get(y) will only exist if we've been there. When we get to a 
     * place, we must ensure that it exists in q before doing anything.
     */
    private Map<Integer, Map<Integer, Map<Integer, Double>>> q;
    private int[] location;
    private int lastMove;
    
    /**
     * Initializes a new Q Learning agent. Needs to know its start location 
     * since it will be starting at a new spot each time it wins/dies. 
     * @param loc The agent's starting location
     * @param alpha The agent's learning rate
     * @param gamma The agent's dicount factor
     * @param moveCount comes from the environment; it is the number of 
     * directions the agent can try to move on this board.
     */
    public Agent(int[] loc, double alpha, double gamma, int moveCount) {
        this.q = new HashMap<>();
        this.location = loc;
        this.alpha = alpha;
        this.gamma = gamma;
        this.NEIGHBOR_COUNT = moveCount;
        this.lastMove = -1;
        ensureExists(loc[0], loc[1]);
    }
    
    /**
     * Agent selects a move based on the Q Learning algorithm.
     * @param r The random object to use when selection needs randomness
     * @param type The type of move selection to use. Options:
     *               random:  Completely random selection
     *               greedy:  Picks best Q moves. For exploiting learned policy
     *               explore: Picks in a way that facilitates exploration.
     * @return the move code for the move selected
     */
    public int selectMove(Random r, String type) {
        int move = -1;
        switch (type) {
            case "random":
                move = r.nextInt(NEIGHBOR_COUNT);
                break;
            case "greedy":
                move = greedyAction(r);
                break;
            case "explore":
                move = exploreAction(r);
                break;
        }
        this.lastMove = move;
        return move;
    }
    
    /**
     * Agent receives feedback from the environment. Updates Q value for the 
     * move it just made, then updates state (ie location)
     * @param fb The feedback array: 
     *             fb[0] = (int) reward; 
     *             fb[1] = (int[]) resultLocation
     */
    public void giveFeedback(Object[] fb) {
        if (fb==null) { //The last move hit a wall, so make that move invalid
            invalidateLastMove();
            return;
        }
        int reward = (int) fb[0];
        int[] newLoc = (int[]) fb[1];
        int[] lastLoc = this.location; //store before moving to use in q update
        moveAgent(newLoc);
        
        //Update Q value for move we just made. Equation from textbook (9.1)
        Map <Integer, Double> lastSpot = q.get(lastLoc[0]).get(lastLoc[1]);
        double lastMoveQ = lastSpot.get(lastMove);
        lastMoveQ += alpha*(reward+gamma*maxQ(newLoc)-lastMoveQ);
        lastSpot.put(lastMove, lastMoveQ);
    }
    
    /**
     * Puts the burgler at the new location. To be called when the burgler has 
     * won or lost, but needs to continue for more epochs.
     * @param loc The agent's starting location
     */
    public void startOver(int[] loc) {
        moveAgent(loc);
    }
    
    
    /**
     * Puts the burgler at the new location. Ensures that q has an entry for 
     * that location.
     * @param loc The agent's new location
     */
    private void moveAgent(int[] loc) {
        this.location = loc;
        ensureExists(loc[0], loc[1]);
    }
    
    /**
     * Gets the highest Q value among all actions from the given spot.
     * @param loc the location to check
     * @return The biggest Q value from loc
     */
    private double maxQ(int[] loc) {
        Map<Integer, Double> spot = q.get(loc[0]).get(loc[1]);
        double maxQ = -1;
        for (Integer i : spot.keySet())
            if (spot.get(i)>maxQ) maxQ = spot.get(i);
        return maxQ;
    }
    
    /**
     * Removes from q the option to make the move the agent made previously. To
     * be called when the agent gets feedback that it hit a wall.
     */
    private void invalidateLastMove() {
        Map<Integer, Double> spot = getCurrentSpot();
        spot.remove(lastMove);
    }
    
    /**
     * Returns the move from the agent's current position with the highest Q.
     * @param r The random object to use
     * @return the move code for the action selected
     */
    private int greedyAction(Random r) {
        Map<Integer, Double> moves = getCurrentSpot();
        Set<Integer> bestMoves = new HashSet<>();
        double bestQ = 0;
        for (Integer i : moves.keySet()) {
            if (moves.get(i) > bestQ) { //if we found a new best Q
                //set bestQ and restart set of best moves
                bestQ = moves.get(i);
                bestMoves = new HashSet<>();
                bestMoves.add(i);
            } else if (moves.get(i) == bestQ) //if we found an equal q
                //add it to the running best moves
                bestMoves.add(i);
        }
        //Pick a random best move
        int move = randomSelection(r, bestMoves);
        //System.out.println("Picking move " + move + " with Q value " + bestQ);
        return move;
    }
    
    /**
     * Returns the agent's exploratory move selection. Currently picks a random
     * action epsilon% of the time. 
     * @param r The random object to use
     * @return the move code for the action selected
     */
    private int exploreAction(Random r) {
        if (r.nextDouble()<EPSILON) return randomAction(r);
        else return greedyAction(r);
    }
    
    /**
     * Returns a random move.
     * @param r The random object to use
     * @return the move code for the action selected
     */
    private int randomAction(Random r) {
        return r.nextInt(NEIGHBOR_COUNT);
    }
    
    /**
     * Returns a random move from the given set of moves
     * @param r The random object to use
     * @param moves The set of moves to be selected from
     * @return the selection
     */
    private int randomSelection(Random r, Set<Integer> moves) {
        int selection = r.nextInt(moves.size());
        int i=0;
        for (Integer m : moves) {
            if (i==selection) return m;
            i++;
        }
        //We should never get here
        return -1;
    }
    
    /**
     * Gets the move map for the agent's current location.
     * @return the move map
     */
    private Map<Integer, Double> getCurrentSpot() {
        return q.get(location[0]).get(location[1]);
    }
    
    /**
     * Makes sure that q has an entry for the spot (x,y). If not, it creates
     * one and initializes the q value for each move from there.
     * @param x The spot's x coordinate
     * @param y The spot's y coordinate
     */
    private void ensureExists(int x, int y) {
        //Get the <int, doub> map of the spot (x,y). Init what's needed
        Map<Integer, Map<Integer, Double>> col = q.get(x);
        if (col==null) {
            q.put(x, new HashMap<>());
            col = q.get(x);
        }
        Map<Integer, Double> spot = col.get(y);
        if (spot==null) {
            col.put(y, new HashMap<>());
            spot = col.get(y);
            //If we get here, the spot was new. Init its actions' Q values.
            for (int i=0; i<NEIGHBOR_COUNT; i++)
                spot.put(i, INITIAL_Q);
        }
    }
}