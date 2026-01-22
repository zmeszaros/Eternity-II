package eternity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ProcessStatus
{
    private int ROW;
    private int COL;
    private Integer[][] table = null;
    private boolean[][] fixes = null;
    private Set<Integer>[][] potentials = null;
    private Set<Integer>[][] checked = null;
    private MemoryQueue wmq = null;

    public ProcessStatus() { }

    public ProcessStatus(MemoryQueue memoryqueue)
    {
        wmq = memoryqueue;
    }

    public int getRow()
    {
        return ROW;
    }

    public int getCol()
    {
        return COL;
    }

    public Integer[][] getTable()
    {
        return table;
    }

    public boolean[][] getFixes()
    {
        return fixes;
    }

    public Set<Integer>[][] getPotentials()
    {
        return potentials;
    }

    public Set<Integer>[][] getChecked()
    {
        return checked;
    }
    
    private void writeToFile(StringBuilder text)
    {
        try
        {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date newDate = new Date();
            String filename = "state_" + dateformat.format(newDate) + ".eii";
            wmq.send(new FileToWrite(filename, false, text));
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }

    public void saveStatus(int row, int col, Table table)
    {
        //StringBuilder writeRow = null;
        
        StringBuilder sb = new StringBuilder();

        TableElem[][] te = table.getTableElem();

        /* aktuális row és col mentése */
        sb.append("[").append(row).append(",").append(col).append("]\n");
        /* a tábla dimenziójának mentése */
        sb.append("[").append(table.getROW()).append(",").append(table.getCOL()).append("]\n");

        int lRow = table.getROW();
        int lCol = table.getCOL();
        for (int i = 0; i < lRow; i++)
        {
            for (int j = 0; j < lCol; j++)
            {
                Integer id;
                boolean fix = false;
                Set<Integer> pot = null;
                Set<Integer> chd = null;
                //StringBuilder writeRow = new StringBuilder();

                try
                {
                    id = te[i][j].getPuzzle().getID();
                }
                catch (Exception e)
                {
                    id = 0;
                }

                pot = te[i][j].getPotentials();
                chd = te[i][j].getChecked();
                fix = te[i][j].getFixed();
                /* a táblára lerakott elem azonosítója [sor (i), oszlop (j), puzzle (id)] */
                sb.append("[").append(i).append(",").append(j).append(",").append(id).append("]");
                /* a puzzlehoz tartozó lehetséges elemek halmaza */
                sb.append("#").append(pot);
                /* az puzzlehoz tartozó, már vizsgált elemek halmaza */
                sb.append("#").append(chd);
                /* a puzzle fix elem-e vagy nem */
                sb.append("#[").append(fix ? "1" : "0").append("]");
                sb.append("\n");
            }
        }

        writeToFile(sb);
    }

   private String removeChar(String s, char c)
   {
       String r = "";
       int lS = s.length();
       for (int i = 0; i < lS; i ++)
       {
           if (s.charAt(i) != c) r += s.charAt(i);
       }
       return r;
   }

    public boolean readStateFromFile(String filename)
    {
        boolean result = true;
        File file = new File(filename);

        String delims = "[#]";
        FileReader fr = null;
        BufferedReader reader = null;

        try
        {
            int ind = 0;
            int tableX = 0;
            int tableY = 0;
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            
            String line = reader.readLine();
            while (line != null)
            {
                ind++;
                String[] tokens = line.split(delims);
                String subDelims = "[,]";

                for (int i = 0; i < tokens.length; i++)
                {
                    tokens[i] = removeChar(tokens[i], '[');
                    tokens[i] = removeChar(tokens[i], ']');
                    String[] subTokens = tokens[i].split(subDelims);
                    Integer[] elems = new Integer[subTokens.length];
                    try
                    {
                        for (int j = 0; j < subTokens.length; j++)
                        {
                            subTokens[j] = subTokens[j].trim();
                            if (subTokens[j].length() > 0)
                                elems[j] = Integer.parseInt(subTokens[j]);
                            else
                                elems[j] = null;
                        }
                    }
                    catch (Exception e)
                    {
                        result = false;
                        break;
                    }

                    if (ind == 1)           // első sor
                    {
                        ROW = elems[0];
                        COL = elems[1];
                    }
                    else if (ind == 2)      //második sor
                    {
                        table = new Integer[elems[0]][elems[1]];
                        fixes = new boolean[elems[0]][elems[1]];
                        potentials = new Set[elems[0]][elems[1]];
                        checked = new Set[elems[0]][elems[1]];
                    }
                    else                    //többi sor
                    {
                        if ( i == 0 )       //első oszlop
                        {
                            tableX = elems[0];
                            tableY = elems[1];
                            table[tableX][tableY] = elems[2];
                        }
                        else if ( i == 1 )  //második oszlop
                            potentials[tableX][tableY] = new HashSet(Arrays.asList(elems));
                        else if ( i == 2 )  //harmadik oszlop
                            checked[tableX][tableY] = new HashSet(Arrays.asList(elems));
                        else if ( i == 3 )  //negyedik oszlop
                            fixes[tableX][tableY] = (elems[0].equals(Integer.valueOf(1))) ? true : false;
                    }
                }

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
}