package eternity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Eternity
{
    private static int ROW = 0;
    private static int COL = 0;
    private static String PUZZLES = "puzzles.txt";
    //private static final Integer EDGE = new Integer(0);
    private static final int EDGE = 0;
    private static final String FULL_SOLUTION = "solutions.txt";
    private static final String CLUE_SOLUTION = "clueSolutions.txt";

    private static Table table = null;
    private static HashMap<Integer, Puzzle> elemek  = new HashMap<Integer, Puzzle>();
    private static List<Integer> cornersElems  = new ArrayList<Integer>();
    private static List<Integer> edgesElems  = new ArrayList<Integer>();
    private static List<Integer> innerElems  = new ArrayList<Integer>();
    private static HashMap<Integer, Set> sameElems  = new HashMap<Integer, Set>();

    private static int step = 0;
    private static int startRow = 0;
    private static int startCol = 0;
    private static String stateFile = null;
    //private static StringBuilder writeToConsole = new StringBuilder();
    private static long numPiecesPlaced = 0;
    private static int solutionNumber = 0;
    private static int clueSolutionNumber = 0;
    private static int maxClueSolve = 0;
    private static boolean autoSaveStatus = true;
    private static final int numberOfMillisecondsInTheFuture = 3600000;     // 1 hour
    private static Timer timer = null;
    private static long startTime;

    private final static MemoryQueue wmq = new MemoryQueue();               //write message queue
    private final static MemoryQueue pmq = new MemoryQueue();               //print message queue
    private static WriteFileQueue twmq = new WriteFileQueue(wmq);       //write messages thread
    private static PrintMessageQueue tpmq = new PrintMessageQueue(pmq); //prtint messages thread

    /*private static long _nums[][] = new long[6][2];
    private static long _startTime[] = new long[6];
    static void setStartTime(int ind) { _startTime[ind] = System.nanoTime(); }
    static long usedTime(int ind) { return System.nanoTime() - _startTime[ind]; }
    static long usedMsecs(int ind) { return usedTime(ind); }*/

    static class saveStatusTask extends TimerTask
    {
        public void run()
        {
            autoSaveStatus = true;
        }
    }

    private static StringBuilder printSolutionFlat(int num)
    {
        final String space = "    ";
        //String row = num + ". ";
        StringBuilder row = new StringBuilder();
        row.append(num).append(". ");

        for (int r = 0; r < ROW; r++)
        {
            for (int c = 0; c < COL; c++)
            {
                String p = null;
                try
                {
                    p = table.getElem(r, c).getID().toString();
                }
                catch (NullPointerException e)
                {
                    p = "0";
                }
                String sn = space + p;
                row.append(sn.substring( p.length(), sn.length() ));
            }
        }

        row.append("\n");
        
        return row;
    }

    private static String printSolutionTable(int num)
    {
        final String space = "   ";
        String tableRow = num + ".\n";

        String sepLine = "+";
        for (int i = COL; --i >= 0; )
            sepLine += "-----+";

        //for (int i=0; i < COL; i++)
        //    sepLine += "-----+";

        for (int r = 0; r < ROW; r++)
        {
            tableRow += sepLine + "\n";
            String Line = "|";
            for (int c = 0; c < COL; c++)
            {
                String p = null;
                try
                {
                    p = table.getElem(r, c).getID().toString();
                }
                catch (NullPointerException e)
                {
                    p = "";
                }
                String sn = space + p;
                String s = sn.substring( p.length(), sn.length() );
                Line += (" " + s + " |");
            }
            tableRow += Line + "\n";
        }
        tableRow += sepLine + "\n";

        return tableRow;
    }

    private static void setStartTime()
    {
        startTime = System.currentTimeMillis();
    }

    private static long usedTime()
    {
        return System.currentTimeMillis() - startTime;
    }

    private static String calcTime(long timeInMillis)
    {
        final int days = (int) ( timeInMillis / ( 24L * 60 * 60 * 1000 ) );
        int remdr = (int) ( timeInMillis % ( 24L * 60 * 60 * 1000 ) );
        final int hours = remdr / ( 60 * 60 * 1000 );
        remdr %= 60 * 60 * 1000;
        final int minutes = remdr / ( 60 * 1000 );
        remdr %= 60 * 1000;
        final int seconds = remdr / 1000;
        final int ms = remdr % 1000;

        return ("elapsed time: " + days + "d  " + hours +"h " + minutes + "m " + seconds + "s " + ms + "ms");
    }

    private static void startTimer()
    {
        timer = new Timer();
        Date timeToRun = new Date(System.currentTimeMillis() + numberOfMillisecondsInTheFuture);
        timer.scheduleAtFixedRate(new saveStatusTask(), timeToRun, numberOfMillisecondsInTheFuture);
    }

    private static boolean readStatus(String file)
    {
        boolean result = false;
        ProcessStatus ps = new ProcessStatus();

        String toConsole = "";

        if (ps.readStateFromFile(file))
        {
            try
            {
                startRow = ps.getRow();
                startCol = ps.getCol();

                Integer[][] t = ps.getTable();
                boolean[][] f = ps.getFixes();
                Set<Integer>[][] p = ps.getPotentials();
                Set<Integer>[][] c = ps.getChecked();

                result = table.restoreTable(elemek, t, f, p, c);

                if (result)
                {
                    for (int x = 0; x < t.length; x++)
                    {
                        for (int y = 0; y < t[x].length; y++)
                        {
                            Integer id = t[x][y];
                            if (id.intValue() <= 0)
                                continue;

                            //be kell forgatni a megfelelő oldalakat.
                            try
                            {
                                toConsole += (++step + ". ("+ x + "," + y + "): ");
                                Puzzle puzzle = elemek.get(id);
                                if ( !table.isFixed(x, y) )
                                {
                                    if (table.fitElem(x, y, puzzle))
                                    {
                                        toConsole += (id + " => " + Arrays.asList(puzzle.getElem()) + "\n");
                                    }
                                    else
                                    {
                                        toConsole += ("ERROR\n");
                                    }
                                }
                                else
                                {
                                    toConsole += (id + " => " + Arrays.asList(puzzle.getElem()) + "\n");
                                }
                            }
                            catch (Exception e) { }

                            try
                            {
                                innerElems.remove(innerElems.indexOf(id));
                                continue;
                            } catch (Exception e) { }
                            try
                            {
                                edgesElems.remove(edgesElems.indexOf(id));
                                continue;
                            } catch (Exception e) { }
                            try
                            {
                                cornersElems.remove(cornersElems.indexOf(id));
                            } catch (Exception e) { }
                        }
                    }

                    pmq.send(toConsole);
                }
            }
            catch (Exception e) { }
        }

        ps = null;
        return result;
    }

    private static void saveSolutionToFile(String filename, StringBuilder solution)
    {
        try
        {
            wmq.send(new FileToWrite(filename, true, solution));
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private static void saveFullSolution(StringBuilder text)
    {
        try
        {
            saveSolutionToFile(FULL_SOLUTION, text);
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private static void saveClueSolution(StringBuilder text)
    {
        try
        {
            saveSolutionToFile(CLUE_SOLUTION, text);
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private static void saveStatus(int row, int col)
    {
        Table cloneTable = (Table)table.clone();

        ProcessStatus ps = new ProcessStatus(wmq);
        ps.saveStatus(row, col, cloneTable);

        ps = null;
        cloneTable = null;
        System.gc();
    }

    private static Set difference(Set setA, Set setB)
    {
        Set tmp = new HashSet(setA);
        if (setB != null && !setB.isEmpty())
            tmp.removeAll(setB);
        return tmp;
    }

    private static boolean isSameElems(Integer[] e1, Integer[] e2)
    {
        int e1L = e1.length;
        for (int i = e1L; --i >= 0; )
        //for (int i = 0; i < L; i++)
        {
            if ( Arrays.equals(e1, e2) )
            {
                return true;
            }
            else
            {
                Collections.rotate(Arrays.asList(e2), -1);
            }
        }

        return false;
    }

    private static void clearingCorners()
    {
        HashMap<Integer, Set> pages = null;
        for (Iterator itr = cornersElems.iterator(); itr.hasNext();)
        {
            Integer elem = (Integer)itr.next();
            pages = table.getElemPotentials(elem);

            HashMap<Integer, Set> newPage = new HashMap<Integer, Set>();
            Iterator it = pages.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                Integer key = (Integer)entry.getKey();
                Set<Integer> value = (Set)entry.getValue();
                value.retainAll(edgesElems);                
                newPage.put(key, value);
            }
            table.setElemPotentials(elem, newPage);
        }
    }

    private static void clearingRelations()
    {
        clearingCorners();
    }

    private static void checkSameElements()
    {
        Iterator it1 = elemek.entrySet().iterator();
        while (it1.hasNext())
        {
            Map.Entry entry1 = (Map.Entry)it1.next();
            Integer key1 = (Integer)entry1.getKey();
            Puzzle value1 = (Puzzle)entry1.getValue();
            Integer[] elem1 = value1.getElem();

            Set<Integer> same = new HashSet();
            Iterator it2 = elemek.entrySet().iterator();
            while (it2.hasNext())
            {
                Map.Entry entry2 = (Map.Entry)it2.next();
                Integer key2 = (Integer)entry2.getKey();
                Puzzle value2 = (Puzzle)entry2.getValue();
                Integer[] elem2 = value2.getElem();

                if (isSameElems(elem1, elem2))
                {
                    same.add(key2);
                }
            }

            sameElems.put(key1, same);
        }
    }

    private static void sortingElem(Integer id, Puzzle elem)
    {
        int edgeNR = 0;
        //List<Integer> sides = new ArrayList(Arrays.asList(elem.getElem()));
        List<Integer> sides = Arrays.asList(elem.getElem());

        Integer side = null;
        for (Iterator itr = sides.iterator(); itr.hasNext();)
        {
            side = (Integer)itr.next();
            if (side == EDGE)
            {
                edgeNR++;
            }
        }

        if (edgeNR == 0)
        {
            innerElems.add(id);
        }
        else if (edgeNR == 1)
        {
            edgesElems.add(id);
        }
        else
        {
            cornersElems.add(id);
        }
    }

    private static void sortingElements()
    {
        Integer key = null;
        Puzzle value = null;
        Iterator it = elemek.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry)it.next();
            key = (Integer)entry.getKey();
            value = (Puzzle)entry.getValue();

            sortingElem(key, value);
        }
    }

    private static void putBackElem(Puzzle elem)
    {
        sortingElem(elem.getID(), elem);
    }

    private static Set<Integer> getAvailableElems(int row, int col, List<Integer> list, Set<Integer> pot)
    {
        Set<Integer> result = new HashSet(list);
        Set<Integer> checked = new HashSet();

        //setStartTime(3);

        try
        {
            result.retainAll(pot);

            /* ha alkalmazzuk a fix elem lehetőséget */
            if (row+1 < ROW)
            {
                if (table.isFixed(row+1, col))
                {
                    result.retainAll(table.getElemPotentialTOP(row+1, col));
                }
            }
            if (col+1 < COL)
            {
                if (table.isFixed(row, col+1))
                {
                    result.retainAll(table.getElemPotentialLEFT(row, col+1));
                }
            }
            /* eddig */

            checked = table.getChecked(row, col);

            result = difference(result, checked);
        }
        catch (Exception e)
        {
            result = null;
        }
        finally
        {
            checked = null;
        }

        //_nums[3][0] += usedMsecs(3);
        //_nums[3][1]++;

        return result;
    }

    private static void removeElem(Integer id)
    {
        try
        {
            innerElems.remove(innerElems.indexOf(id));
            return;
        } catch (Exception e) { }
        try
        {
            edgesElems.remove(edgesElems.indexOf(id));
            return;
        } catch (Exception e) { }
        try
        {
            cornersElems.remove(cornersElems.indexOf(id));
        } catch (Exception e) { }
    }

    private static void addCheckedElem(int row, int col, Integer id)
    {
        Set<Integer> elems = sameElems.get(id);
        table.addCheckedElems(row, col, elems);
        //table.addCheckedElem(row, col, id);
    }

    private static Puzzle selectElem(int row, int col, Set pot)
    {
        Integer id = null;
        Puzzle result = null;
        Set<Integer> tmp = new HashSet();

        if (((row == 0) && (col == 0)) ||
            ((row == 0) && (col == COL-1)) ||
            ((row == ROW-1) && (col == 0)) ||
            ((row == ROW-1) && (col == COL-1)))
        {
            if ( !cornersElems.isEmpty() )
            {
                if ( (pot != null) && (!pot.isEmpty()) )
                {
                    tmp = getAvailableElems(row, col, cornersElems, pot);

                    if ( (tmp != null) && (!tmp.isEmpty()) )
                    {
                        id = (Integer)tmp.iterator().next();
                        cornersElems.remove(cornersElems.indexOf(id));
                    }
                }
                else
                {
                    id = cornersElems.get(0);
                    cornersElems.remove(0);
                }
            }
        }
        else if (((row == 0) && (col > 0) && (col < COL-1)) ||
                 ((row == ROW-1) && (col > 0) && (col < COL-1)) ||
                 ((row > 0) && (row < ROW-1) && (col == 0)) ||
                 ((row > 0) && (row < ROW-1) && (col == COL-1)))
        {
            if ( !edgesElems.isEmpty() )
            {
                if ( (pot != null) && (!pot.isEmpty()) )
                {
                    tmp = getAvailableElems(row, col, edgesElems, pot);

                    if ( (tmp != null) && (!tmp.isEmpty()) )
                    {
                        id = (Integer)tmp.iterator().next();
                        edgesElems.remove(edgesElems.indexOf(id));
                        //int ind = new Random().nextInt(tmp.size()) + 1;
                        //edgesElems.remove(edgesElems.get(ind));
                    }
                    /*else
                    {
                        System.out.println("checked: " + Arrays.asList(table.getChecked(row, col)));
                        System.out.println("potentials: " + Arrays.asList(table.getPotentials(row, col)));
                    }*/
                }
                else
                {
                    id = edgesElems.get(0);
                    edgesElems.remove(0);
                }
            }
        }
        else
        {
            if ( !innerElems.isEmpty() )
            {
                if ( (pot != null) && (!pot.isEmpty()) )
                {
                    tmp = getAvailableElems(row, col, innerElems, pot);

                    if ( (tmp != null) && (!tmp.isEmpty()) )
                    {
                        id = (Integer)tmp.iterator().next();
                        innerElems.remove(innerElems.indexOf(id));
                    }
                }
                else
                {
                    id = innerElems.get(0);
                    innerElems.remove(0);
                }
            }
        }

        if (id != null)
        {
            result = elemek.get(id);
            addCheckedElem(row, col, id);
        }

        tmp = null;

        return result;
    }

    private static int getBackStepCol(int row, int col)
    {
        int result = --col;

        if (table.isFixed(row, col))
        {
            result = getBackStepCol(row, col);
        }

        return result;
    }

    private static void startSolve(int row, int col)
    {
        solve(row, col);
    }

    private static void printStats()
    {
        long elapsed = usedTime();
        String text = String.format("\nSolutions: %d, Max Depth = %d", solutionNumber, maxClueSolve);
        text += "\n Elapsed time: " + calcTime(elapsed);
        text += String.format("\n Number of placements : %d", numPiecesPlaced);
        text += String.format("\n Millions placements per sec: %f", ((double)(numPiecesPlaced / (elapsed + 1)) / 1000));
        pmq.send(text);
        //System.out.flush();
    }

    private static void solve(int row, int col)
    {
        //int newPoz[] = new int[2];

        int indRow = 0;
        int indRowCol = 0;
        while ((row >= 0) && (row < ROW))
        {
            indRow = (row * COL);

            while ((col >= 0) && (col < COL))
            {
                //writeToConsole.setLength(0);
                //writeToConsole.append(++step).append(". (").append(row).append(",").append(col).append("): ");

                if (autoSaveStatus)
                {
                    autoSaveStatus = false;
                    saveStatus(row, col);
                }

                if ( table.isFixed(row, col) )
                {
                    col++;
                }
                else
                {
                    //numPiecesPlaced++;
                    if( checkPosition(row, col) )
                    {
                        col++;

                        indRowCol = (indRow + col);
                        if (maxClueSolve < indRowCol)
                        {
                            maxClueSolve = indRowCol;
                            //pmq.send(printSolutionFlat(indRowCol));
                            printStats();
                            pmq.send(printSolutionTable(maxClueSolve));
                            //saveStatus(row, col);
                        }
                    }
                    else
                    {
                        /*newPoz = backStep(row, col);
                        row = newPoz[0];
                        col = newPoz[1];*/

                        backStep(row, col);
                        col = getBackStepCol(row, col);
                        /*if (col == 0 && row > 0)
                        {
                            //System.out.println("checked: " + Arrays.asList(table.getChecked(row, col)));
                            //System.out.println("potentials: " + Arrays.asList(table.getPotentials(row, col)));
                            do
                            {
                                backStep(row, col);
                                col = getBackStepCol(row, col);
                                if (col < 0)
                                {
                                    row--;
                                    col = COL - 1;
                                }
                                //writeToConsole.append(" | ");
                            } while (col > 0);
                            //pmq.send(printSolutionTable(indRowCol));
                        }
                        else
                        {
                            backStep(row, col);
                            col = getBackStepCol(row, col);
                        }*/
                    }
                }
                //pmq.send(printSolutionTable(++solutionNumber));
                //pmq.send(printSolutionFlat(++solutionNumber));
                //System.out.print(printSolutionFlat(++solutionNumber));
                //saveStatus(row, col);

                if ( (col == COL) && (row == ROW-1) )
                {
                    //StringBuilder solutionString = printSolutionFlat(++solutionNumber);
                    String solutionString = printSolutionTable(++solutionNumber);

                    pmq.send(solutionString);
                    //saveStatus(row, col);
                    //saveFullSolution(new StringBuilder(solutionString));
                    
                    col--;
                    putBackElem(table.getElem(row, col));
                    table.removeElem(row, col);
                }
            }
            
            if (col >= COL)
            {
                row++;
                col = 0;
            }
            else
            {
                row--;
                col = COL - 1;
            }
            //saveStatus(row, col);
        }
    }

    private static boolean checkPosition(int row, int col)
    {
        Puzzle p = null;
        Set pot = table.getPotentials(row, col);

        if (pot.isEmpty())
            return false;
        else
        {
            //setStartTime(0);
            numPiecesPlaced++;
            p = selectElem(row, col, pot);
            //_nums[0][0] += usedMsecs(0);
            //_nums[0][1]++;
            //System.out.print(String.format("\nselectElem(): %d msec", usedMsecs()));

            if (p == null) return false;

            //writeToConsole.append(p.getID()).append(" => ").append(Arrays.asList(p.getElem())).append(" check...");

            /*if ((row == 2) && (col == 1))
            {
                System.out.println("");
            }*/
            //setStartTime(1);
            //boolean teszt = table.fitElem(row, col, p);
            //_nums[1][0] += usedMsecs(1);
            //_nums[1][1]++;
            if (table.fitElem(row, col, p))
            {
                //writeToConsole.append("OK => put table ").append(Arrays.asList(p.getElem()));
                //pmq.send(writeToConsole + "\n");

                table.putElem(row, col, p);
                return true;
            }
            else
            {
                //writeToConsole.append("notOK | ");
                //setStartTime(2);
                putBackElem(p);
                //_nums[2][0] += usedMsecs(2);
                //_nums[2][1]++;
                return checkPosition(row, col);
            }
        }
    }

    private static int[] getPrevIndex(int row, int col)
    {
        int result[] = new int[] {0, 0};

        //if (row > 0 && col < 1)
        if (col < 1)
        {
            result[0] = --row;
            result[1] = COL - 1;
        }
        else
        {
            result[0] = row;
            result[1] = --col;
        }

        if (table.isFixed(result[0], result[1]))
        {
            result = getPrevIndex(result[0], result[1]);
        }

        return result;
    }

    private static void backStep(int row, int col)
    {
        //if (row < 0 || col < 0)
        //    System.out.println("");

        table.clearCheckedElem(row, col);

        int xy[] = getPrevIndex(row, col);

        /*writeToConsole.append("VISSZALÉPÉS ");
        try
        {
            writeToConsole.append(table.getElem(xy[0], xy[1]).getID()).append(" => ").append(Arrays.asList(table.getElem(xy[0], xy[1]).getElem())).append(" DELETE");
        }
        catch (Exception e) {}
        pmq.send(writeToConsole + "\n");*/

        try
        {
            Puzzle before = null;
            before = table.getElem(xy[0], xy[1]);
            putBackElem(before);
            table.removeElem(xy[0], xy[1]);
        }
        catch (Exception e)
        {
            return;
        }

        //return xy;
    }

    private static void setFixElem(int row, int col, Puzzle elem)
    {
        table.setFixElem(row, col, elem);
        removeElem(elem.getID());
        table.addCheckedElem(row, col, elem.getID());
    }

    private static boolean readPuzzlesFromFile(String filename)
    {
        boolean result = true;
        File file = new File(filename);

        String delims = "[,]";
        FileReader fr = null;
        BufferedReader reader = null;

        try
        {
            int id = 0;
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String line = reader.readLine();
            while (line != null)
            {
                String[] tokens = line.split(delims);
                Integer[] elems = new Integer[tokens.length];

                try
                {
                    for (int i = 0; i < tokens.length; i++)
                    {
                        elems[i] = Integer.parseInt(tokens[i].trim());
                    }
                }
                catch (NumberFormatException e)
                {
                    result = false;
                    System.out.println("Problem with elem: " + Arrays.asList(tokens));
                    break;
                }

                id++;
                elemek.put(id, new Puzzle(id, elems));

                line = reader.readLine();
            }
        }
        catch (FileNotFoundException e)
        {
            result = false;
            System.out.println("File not found! " + e);
        }
        catch (IOException ioe)
        {
            result = false;
            System.out.println("Exception while reading the file: " + ioe);
        }
        finally
        {
            try
            {
                if (fr != null)
                    fr.close();
                if (reader != null)
                    reader.close();
            }
            catch( IOException ioe)
            {
                result = false;
                System.out.println("Error while closing the stream: " + ioe);
            }
        }

        return result;
    }

    private static boolean parseArguments(String[] args)
    {
        Options options = new Options();
        options.addOption( "r", "row", true, "row numbers of table." );
        options.addOption( "c", "col", true, "col numbers of table." );
        options.addOption( "p", "puzzles", true, "file of puzzles" );
        options.addOption( "s", "state", true, "state file" );

        CommandLineParser parser = new PosixParser();
        try
        {
            CommandLine line = parser.parse( options, args );

            if( line.hasOption( "r" ) )
            {
                try
                {
                    ROW = Integer.parseInt(line.getOptionValue( "r" ));
                }
                catch (NumberFormatException e)
                {
                    ROW = 0;
                }
            }

            if( line.hasOption( "c" ) )
            {
                try
                {
                    COL = Integer.parseInt(line.getOptionValue( "c" ));
                }
                catch (NumberFormatException e)
                {
                    COL = 0;
                }
            }

            if( line.hasOption( "p" ) )
            {
                PUZZLES = line.getOptionValue( "p" ).trim();
            }

            if( line.hasOption( "s" ) )
            {
                stateFile = line.getOptionValue( "s" ).trim();
            }
        }
        catch( ParseException exp )
        {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
            return false;
        }

        return true;
    }

    public static void main(String[] args)
    {
        if (!parseArguments(args))
        {
            System.exit(0);
        }

        if (!readPuzzlesFromFile(PUZZLES))
        {
            System.exit(0);
        }

        sortingElements();
        checkSameElements();

        table = new Table(ROW, COL);
        table.setRelations(elemek);
        clearingRelations();

        if (stateFile != null && stateFile.length() > 0)
        {
            if (!readStatus(stateFile))
                System.exit(0);
        }
        else
        {
            try
            {
                startRow = 0;
                startCol = 0;

                //setFixElem(3, 2, elemek.get(31));

                /*setFixElem(2, 5, elemek.get(48));
                setFixElem(4, 3, elemek.get(43));*/

                setFixElem(0, 0, elemek.get(1));
                setFixElem(2, 2, elemek.get(208));
                setFixElem(2, 13, elemek.get(255));
                setFixElem(8, 7, elemek.get(139));
                setFixElem(13, 2, elemek.get(181));
                setFixElem(13, 13, elemek.get(249));

                if (!table.isFixed(startRow, startCol))
                {
                    table.initial(startRow, startCol, new HashSet(cornersElems));
                }
            }
            catch( Exception ex )
            {
                System.out.println( "Unexpected exception:" + ex.toString() );
                System.exit(0);
            }
        }

        tpmq.start();
        twmq.start();

        startTimer();
        setStartTime();

        startSolve(startRow, startCol);

        pmq.send("\n");
        //pmq.send("corner elems nr: " + cornersElems.size() + "\n");
        //pmq.send("edge elems nr:   " + edgesElems.size() + "\n");
        //pmq.send("inner elems nr:  " + innerElems.size() + "\n");
        String  elapsed = calcTime(usedTime()) + "\n";
        pmq.send(elapsed);

        /*System.out.println(String.format("selectElem() futott: %d db, összes idő: %d, átlagos idő: %d", _nums[0][1], _nums[0][0], (_nums[0][0] / _nums[0][1])));
        System.out.println(String.format("fitElem() futott: %d db, összes idő: %d, átlagos idő: %d", _nums[1][1], _nums[1][0], (_nums[1][0] / _nums[1][1])));
        System.out.println(String.format("putBackElem() futott: %d db, összes idő: %d, átlagos idő: %d", _nums[2][1], _nums[2][0], (_nums[2][0] / _nums[2][1])));
        System.out.println(String.format("getAvailableElems() futott: %d db, összes idő: %d, átlagos idő: %d", _nums[3][1], _nums[3][0], (_nums[3][0] / _nums[3][1])));*/

        //saveFullSolution(new StringBuffer(elapsed));

        timer.cancel();

        tpmq.Stop();
        twmq.Stop();
    }
}
