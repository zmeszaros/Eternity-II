package eternity;

import java.util.Arrays;
import java.util.Collections;

public class Puzzle implements Cloneable
{
    private Integer id;
    //private byte rotate = 0;
    private Integer puzzle[] = null;
    private Integer rotate[][] = null;

    public Puzzle(Integer key, Integer[] elem)
    {
        id = key;
        puzzle = elem;
        
        int lE = elem.length;
        rotate = new Integer[lE][lE];
        for (int i = lE; --i >= 0; )
        {
            Collections.rotate(Arrays.asList(elem), -1);
            System.arraycopy(elem, 0, rotate[i], 0, lE);
        }
    }

    @Override
    public Object clone()
    {
        try
        {
            return (Puzzle) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(e.toString());
        }
    }

    public Integer getID()
    {
        return id;
    }

    public Integer[] getElem()
    {
        return puzzle;
    }

    public Integer getElemSide(int nr)
    {
        return puzzle[nr];
    }

    /*public void rotate()
    {
        //rotate = (rotate < puzzle.length - 1) ? ++rotate : 0;
        Collections.rotate(Arrays.asList(puzzle), -1);
    }*/

    public Integer[] getRotateElem(int i)
    {
        return rotate[i];
    }

    public void setRotatePuzzle(Integer[] rotate)
    {
        puzzle = rotate;
    }
}
