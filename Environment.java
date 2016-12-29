import java.util.*;
import java.io.*;

public class Environment {
    public static int INPUT_FILE_LINE_COUNT = 5;
    public static int ESCAPE_SCORE = 15;
    public static int PONY_SCORE = 10;
    public static int TROLL_SCORE = -15;
    public static int NEUTRAL_SCORE = 2;
    public static int NEIGHBOR_COUNT = 8;
    //Int arrays are x, y coordinates
    
    //Immutable parts of the Environment
    private int size;
    private int[] escape;
    private int initialPonyCount;
    private List<int[]> ponies;
    private List<int[]> trolls; //Will move soon
    private List<int[]> obstructions;
    
    //Changing aspects of the environment
    private int[] burgler;
    private List<int[]> burglerPath;
    private int score;
    
    /**
     * Parses an input file as a description of an environment.
     * @param sc The scanner over the input file to parse
     */
    public Environment(Scanner sc) {
        //Line 1: board size, troll count, pony count
        //Line 2: escape location
        //Line 3: pony locations
        //Line 4: obstruction locations
        //Line 5: troll locations
        for (int i=1; i<=INPUT_FILE_LINE_COUNT; i++) {
            if (!sc.hasNextLine()) this.error("inputLen");
            String line = sc.nextLine();
            parseLine(line, i);
        }
        this.initBurgler(new Random());
        this.initBurglerPath();
        this.score = 0;
    }
    
    /**
     * Initializes the burgler to a random unoccupied spot. Implemented lazily
     * @param r The Random object to use
     */
    private void initBurgler(Random r) {        
        while(this.burgler==null) {
            int proposedX = r.nextInt(size);
            int proposedY = r.nextInt(size);
            if (!this.hasAnything(proposedX, proposedY))
                this.burgler = new int[]{proposedX, proposedY};
        }
    }
    
    /**
     * Initializes burgler path. At first only contains the burgler's start.
     */
    private void initBurglerPath() {
        burglerPath = new ArrayList<>();
        burglerPath.add(burgler);
    }   
    
    /**
     * Moves the burgler in the given direction, as long as it's valid.
     * @param dir 0-7, a Moore neighborhood move numbered clockwise from north
     * @return null if invalid, otherwise array of feedback for the Agent:
     *             fb[0] = (int) reward; 
     *             fb[1] = (int[]) resultLocation
     */
    public Object[] makeMove(int dir) {
        if (validMove(dir, burgler)) {
            burgler = getMoveResult(dir, burgler);
            burglerPath.add(burgler);
            int reward = this.processScore();
            return new Object[]{reward, burgler};
        } else return null;
    }
    
    /**
     * Determines whether or not the burgler is at the escape or dead.
     * @return if the burgler is at the escape
     */
    public boolean hasTerm() {
        boolean win = hasBurgler(escape[0], escape[1]);
        boolean lose = hasTroll(burgler[0], burgler[1]);
        return win||lose;
    }
    
    /**
     * Gets the burgler's current score.
     * @return the burgler's score
     */
    public int getScore() {
        return this.score;
    }
    
    /**
     * Gets the burgler's current location
     * @return the burgler's location
     */
    public int[] getBurgler() {
        return this.burgler.clone();
    }
    
    /** 
     * Determines the number of ponies on the board.
     * @return pony count
     */
    public int ponyCount() {
        return this.ponies.size();
    }
    
    /**
     * Determines whether or not there's a pony here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @return if there is a pony here
     */
    public boolean hasPony(int x, int y) {
        return this.hasThing(x, y, this.ponies);
    }
     
    /**
     * Determines whether or not there's a troll here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @return if there is a troll here
     */
    public boolean hasTroll(int x, int y) {
        return this.hasThing(x, y, this.trolls);
    }
    
    /**
     * Determines whether or not there's an obstruction here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @return if there is an obstruction here
     */
    public boolean hasObstruction(int x, int y) {
        return this.hasThing(x, y, this.obstructions);
    }
    
    /**
     * Determines whether or not the burgler is here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @return if the burgler is here
     */
    public boolean hasBurgler(int x, int y) {
        return this.hasThing(x, y, pointAsList(this.burgler));
    }
    
    /**
     * Determines whether or not the escape is here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @return if the escape is here
     */
    public boolean hasEscape(int x, int y) {
        return this.hasThing(x, y, pointAsList(this.escape));
    }
    
    /**
     * Determines whether or not the burgler has been here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @return if the burgler's path contains this location
     */
    public boolean hasPath(int x, int y) {
        return this.hasThing(x, y, this.burglerPath);
    }
    
    /**
     * Determines whether or not the dir move from src is valid.
     * @param dir 0-7, a Moore neighborhood move numbered clockwise from north
     * @param src The starting location for the proposed move
     * @return whether or not it's valid
     */
    private boolean validMove(int dir, int[] src) {
        int[] res = getMoveResult(dir, src);
        for (int i=0; i<res.length; i++) 
            if (res[i]<0||res[i]>=size) return false;
        if (this.hasObstruction(res[0], res[1])) return false;
        return true;
    }
    
    /**
     * Gets the result int[x, y] of moving dir from src.
     * @param dir 0-7, a Moore neighborhood move numbered clockwise from north
     * @param src The starting location for the proposed move
     * @return the resulting point as an int array
     */
    private static int[] getMoveResult(int dir, int[] src) {
        //Otherwise get its result
        int[] res = null;
        switch(dir) {
            case 0: res = new int[]{src[0], src[1]+1};
            break;
            case 1: res = new int[]{src[0]+1, src[1]+1};
            break;
            case 2: res = new int[]{src[0]+1, src[1]};
            break;
            case 3: res = new int[]{src[0]+1, src[1]-1};
            break;
            case 4: res = new int[]{src[0], src[1]-1};
            break;
            case 5: res = new int[]{src[0]-1, src[1]-1};
            break;
            case 6: res = new int[]{src[0]-1, src[1]};
            break;
            case 7: res = new int[]{src[0]-1, src[1]+1};
            break;
        }
        return res;
    }
    
    /**
     * Changes the score based on the burgler's location. To be called just 
     * after moving the burgler. Removes captured ponies
     * @return reward that was added
     */
    private int processScore() {
        int add = 0;
        if (this.hasEscape(burgler[0], burgler[1])) add = ESCAPE_SCORE;
        else if (this.hasPony(burgler[0], burgler[1])) {
            add = PONY_SCORE;
            this.removePony(burgler);
        } else if (this.hasTroll(burgler[0], burgler[1])) add = TROLL_SCORE;
        else add = NEUTRAL_SCORE;
        
        score += add;
        return add;
    }
    
    /**
     * Gets the percentage of ponies that have currently been saved.
     * @return the percentage
     */
    private int getPonyPercent() {
        int poniesSaved = initialPonyCount - this.ponyCount();
        double percent = 100*((double)poniesSaved)/initialPonyCount;
        return (int)percent;
    }
    
    /**
     * Removes a pony from the board.
     * @param killSpot the int[x,y] location from which to remove a pony
     */
    private void removePony(int[] killSpot) {
        for (Iterator<int[]> i = ponies.iterator(); i.hasNext();) {
            int[] pony = i.next();
            if (pony[0]==killSpot[0]&&pony[1]==killSpot[1]) {
                i.remove();
                return;
            }
        }
    }    
    
    /**
     * Returns whether or not this spot is occupied. For initializing burgler
     * @param x the x coordinate
     * @param y the y coordinate
     * @return if there's something here
     */
    private boolean hasAnything(int x, int y) {
        for (List<int[]> list : this.getLists())
            if (this.hasThing(x, y, list)) return true;
        return false;
    }

    /**
     * Determines whether or not there's a thing from the list here.
     * @param x The x coordinate
     * @param y the y coordinate
     * @param list The big list of things
     * @return if there is an element of list here
     */
    private boolean hasThing(int x, int y, Collection<int[]> list) {
        for (int[] item : list)  {
            List<Integer> spotList = intArrayToObjList(item);
            if (item[0]==x&&item[1]==y) return true;
        }
        return false;
    }
    
    /**
     * Gets a set of lists of things that can occupy board squares.
     * @return a Set of Lists of int[]s representing occupied board spots.
     */
    private Set<List<int[]>> getLists() {
        Set<List<int[]>> lists = new HashSet<>();
        lists.add(this.ponies);
        lists.add(this.trolls);
        lists.add(this.obstructions);
        lists.add(pointAsList(this.escape));
        //This could be called to initialize burgler, so check if he's around
        if (burgler!=null) lists.add(pointAsList(this.burgler));
        return lists;
    }
    
    /**
     * Returns the given location as a list of int[]s. For using the 
     * point as an list of arbitrary things that occupy space on the board.
     * Eg, the burgler or escape location.
     * @return the point as a list
     */
    private ArrayList<int[]> pointAsList(int[] point) {
        ArrayList<int[]> pointList = new ArrayList<>();
        pointList.add(point);
        return pointList;
    }
    
    /**
     * Gets the set of valid moves for the burgler. Do not use, it's cheating.
     * @return a set of integers representing moves:
     *         7 0 1    "B" is burgler location and the integers are the
     *         6 B 2    encodings for moving to the squares in which the  
     *         5 4 3    integers reside.
     */
    public List<Integer> getValidMoves() {
        List<Integer> moves = new ArrayList<>();
        for (int i=0; i<8; i++) 
            if (validMove(i, burgler)) 
                moves.add(i);
        return moves;
    }

    /**
     * Parses an arbitrary input file line. Splits the line into useful tokens
     * passes it along to specific line handlers.
     * @param line The string to parse as a line
     * @param lineNum the number of this specific line
     */
    private void parseLine(String line, int lineNum) {
        //Split line and parse each token as an int
        String[] splitLine = line.split(" ");
        int[] splitLineInts = new int[splitLine.length];
        for (int i=0; i<splitLine.length; i++) 
            splitLineInts[i] = Integer.parseInt(splitLine[i]);
        
        //Pass to specific line handlers
        switch (lineNum) {
            case 1:
                parseSizeTrollPonyCount(splitLineInts);
                break;
            case 2:
                parseEscape(splitLineInts);
                break;
            case 3:
                parsePonies(splitLineInts);
                break;
            case 4:
                parseObstructions(splitLineInts);
                break;
            case 5:
                parseTrolls(splitLineInts);
                break;
        }
    }
    
    /**
     * Parses the first line. Sets the size of the board and initializes 
     * troll/pony list sizes.
     * @param splitLine the line to parse, split into tokens
     */
    private void parseSizeTrollPonyCount(int[] splitLine) {
        this.size = splitLine[0];
        //Get troll and pony counts
        int trollCount = splitLine[1];
        int ponyCount = splitLine[2];
        //Init size of troll/pony lists
        this.trolls = new ArrayList<>(trollCount);
        this.ponies = new ArrayList<>(ponyCount);
        this.initialPonyCount = ponyCount;
        this.obstructions = new ArrayList<>();
    }
    
    /**
     * Parses the second line. Sets the escape location
     * @param splitLine the line to parse, split into integer tokens
     */
    private void parseEscape(int[] splitLine) {
        this.escape = new int[]{splitLine[0], splitLine[1]};
    }
    
    /**
     * Parses the third line. Sets pony locations
     * @param splitLine the line to parse, split into integer tokens
     */
    private void parsePonies(int[] splitLine) {
        parsePairs(splitLine, ponies);
    }
    
    /**
     * Parses the fourth line. Sets obstruction locations
     * @param splitLine the line to parse, split into integer tokens
     */
    private void parseObstructions(int[] splitLine) {
        parsePairs(splitLine, obstructions);
        //If the pair -1 -1 was added, it means there aren't any obstructions
        for (int[] i : obstructions) if (i[0]==-1) {
            obstructions = new ArrayList<>();
            break;
        }
    }
    
    /**
     * Parses the fifth line. Sets troll locations
     * @param splitLine the line to parse, split into integer tokens
     */
    private void parseTrolls(int[] splitLine) {
        parsePairs(splitLine, trolls);
    }
    
    /**
     * Parses a line that is a series of pairs that match in order. Adds it to
     * a specified ArrayList of pairs (int[2]s).
     * @param splitLine the line to parse, split into integer tokens
     * @param list The list of pairs to add pairs to
     */
    private void parsePairs(int[] splitLine, List<int[]> list) {
        for (int i=0; i<splitLine.length; i+=2)
            list.add(new int[]{splitLine[i], splitLine[i+1]});
    }
    
    /**
     * Prints a fancy fancy ASCII representation of the board with a score and 
     * pony report. New line after.
     * @param outs An array of PrintStreams to print to
     */
    public void print(PrintStream[] outs) {
        this.printBorder(outs);
        for (int i=this.size-1; i>=0; i--)
            this.printRow(i, outs);
        this.printBorder(outs);
        multiPrintln("Burgler score: " + this.getScore()  +"; Pony report: " + 
                        this.getPonyPercent() + "% of ponies saved!", outs);
    }
    
    /**
     * Prints a really fancy border row for the board. Ends the line.
     * @param outs An array of PrintStreams to print to
     */
    private void printBorder(PrintStream[] outs) {
        printAbstractRow(0, outs, true);
    }
    
    /**
     * Prints an extra fancy row for the board. Ends the line.
     * @param row The row to print
     * @param outs An array of PrintStreams to print to
     */
    private void printRow(int row, PrintStream[] outs) {
        printAbstractRow(row, outs, false);
    }
    
    /**
     * Prints a very fancy row for the board, but it could be a border. 
     * Ends the line.
     * @param row The row to print
     * @param outs An array of PrintStreams to print to
     */
    private void printAbstractRow(int row, PrintStream[] outs, boolean border) {
        //The following prints end with a space
        this.printEdge(outs, border);
        this.printCells(row, outs, border);
        this.printEdge(outs, border);
        multiPrintln(outs);
    }
    
    /** 
     * Prints a super fancy left-or-right edge piece. Ends with a space.
     * @param outs An array of PrintStreams to print to
     * @param border Whether or not this is an edge surroudning a border
     */
    private void printEdge(PrintStream[] outs, boolean border) {
        String s = null;
        if (border) s = "###";
        else s = "## ";
        multiPrint(s, outs);
    }
    
    /**
     * Prints a pretty fancy sequence of cells in the row. Ends with a space.
     * @param row The row number
     * @param outs The array of PrintStreams to print to
     * @param border A flag to make this print the same length, but all #s.
     */
    private void printCells(int row, PrintStream[] outs, boolean border) {
        char[] cells = this.getRow(row);
        String s = null;
        for (int i=0; i<cells.length; i++) {
            if (border) s = "##"; 
            else s = cells[i]+" ";
            multiPrint(s, outs);
        }
        
    }
    
    /**
     * Gets a row as an array of characters for printing with path. Path is 
     * seen over trolls and ponies.
     * @param row The row to get
     */
    private char[] getRow(int row) {
        char[] array = new char[size];
        for (int col=0; col<array.length; col++) {
            if (this.hasBurgler(col, row)) array[col] = 'B';
            else if (this.hasObstruction(col, row)) array[col] = '1';
            else if (this.hasEscape(col, row)) array[col] = 'E';
            else if (this.hasPath(col, row)) array[col] = 'X';
            else if (this.hasTroll(col, row)) array[col] = 'T';
            else if (this.hasPony(col, row)) array[col] = 'P';
            else array[col] = '-';
        }
        return array;
    }
    
    /**
     * Turns an int array into a list of Integers, which is apparently one too
     * many steps for Arrays.asList()
     * @param array The array to convert
     * @return The array as a list
     */
    public static List<Integer> intArrayToObjList(int[] array) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i=0; i<array.length; i++) list.add(array[i]);
        return list;
    }
    
    /**
     * Prints a mega fancy string, followed by newline, to the PrintStreams.
     * @param s The string
     * @param outs The array of PrintStreams to print to
     */
    public static void multiPrintln(String s, PrintStream[] outs) {
        multiPrint(s+"\n", outs);
    }
    
    /**
     * Prints a mega fancy newline to the PrintStreams.
     * @param outs The array of PrintStreams to print to
     */
    public static void multiPrintln(PrintStream[] outs) {
        multiPrint("\n", outs);
    }
    
    /**
     * Prints the ultra fancy string to each PrintStream in the array.
     * @param s The string
     * @param outs The array
     */
    public static void multiPrint(String s, PrintStream[] outs) {
        for (int i=0; i<outs.length; i++) outs[i].print(s);
    }
    
    //Prints the error message associated with err and exits the program.
    private static void error(String err) {
        String message = null;
        switch (err) {
            case "inputLen":
                message = "Input file doesn't have enough lines. See the" +
                            "README for the input file format.";
                break;
        }
        System.out.println(message);
        System.exit(1);
    }
}