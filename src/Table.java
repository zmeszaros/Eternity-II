package eternity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Table implements Cloneable
{
    private int ROW;
    private int COL;
    private TableElem[][] te;
    private Relations rel = new Relations();

    private final int TOP = 0;
    private final int RIGHT = 1;
    private final int BOTTOM = 2;
    private final int LEFT = 3;

    private final Puzzle zeroElem = new Puzzle(0, new Integer[]{0,0,0,0});

    public Table(int r, int c)
    {
        ROW = r;
        COL = c;
        te = new TableElem[ROW][COL];

        for (int i = 0; i < ROW; i++)
        {
            for (int j = 0; j < COL; j++)
            {
                te[i][j] = new TableElem();
            }
        }
    }

    @Override
    public Object clone()
    {
        Table copyTable = null;

        try
        {
            copyTable = (Table) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(e.toString());
        }

        copyTable.rel = null;
        copyTable.te = (TableElem[][])te.clone();

        return copyTable;
    }

    public void initial(int row, int col, Set start)
    {
        te[row][col].addPotentials(start);
    }

    public void setRelations(HashMap hm)
    {
        rel.createRelations(hm);
    }

    public boolean fitElem(int row, int col, Puzzle elem)
    {
        if (elem == null) return false;

        Puzzle top = null;
        Puzzle left = null;
        Puzzle right = null;
        Puzzle bottom = null;
        
        if (row > 0)
        {
            top = te[row-1][col].getPuzzle();
        }
        else
        {
            top = zeroElem;
        }

        if (col > 0)
        {
            left = te[row][col-1].getPuzzle();
        }
        else
        {
            left = zeroElem;
        }

        Integer rotate[] = null;
        int bottom_top = 0;
        int right_left = 0;
        boolean rightFix = false;
        boolean bottomFix = false;
        int top_bottom = top.getElemSide(BOTTOM);
        int left_right = left.getElemSide(RIGHT);

        if ((row < ROW-1) && isFixed(row+1, col))
        {
            bottomFix = true;
            bottom = te[row+1][col].getPuzzle();
            bottom_top = bottom.getElemSide(TOP);
        }

        if ((col < COL-1) && isFixed(row, col+1))
        {
            rightFix = true;
            right = te[row][col+1].getPuzzle();
            right_left = right.getElemSide(LEFT);
        }

        int lE = elem.getElem().length;
        for (int i = lE; --i >= 0; )
        {
            rotate = elem.getRotateElem(i);

            if (rotate[TOP] == top_bottom && rotate[LEFT] == left_right)
            {
                boolean oke = true;
                if (bottomFix)
                {
                    if (rotate[BOTTOM] != bottom_top)
                        oke = false;
                }
                if (rightFix)
                {
                    if (rotate[RIGHT] != right_left)
                        oke = false;
                }

                if (oke)
                {
                    elem.setRotatePuzzle(rotate);
                    return true;
                }
            }
        }

        return false;
    }

    public void putElem(int row, int col, Puzzle elem)
    {
        te[row][col].setPuzzle(elem);

        if ((row < ROW-1))
        {
            if (isFixed(row+1, col))
            {
                Integer id = te[row+1][col].getPuzzle().getID();
                te[row+1][col].addPotential(id);
            }
            else
            {
                te[row+1][col].addPotentials(rel.getPotential(elem.getID(), elem.getElemSide(BOTTOM)));
            }
        }
        if ((col < COL-1))
        {
            if (isFixed(row, col+1))
            {
                Integer id = te[row][col+1].getPuzzle().getID();
                te[row][col+1].addPotential(id);
            }
            else
            {
                te[row][col+1].addPotentials(rel.getPotential(elem.getID(), elem.getElemSide(RIGHT)));
            }
        }
    }

    public void setFixElem(int row, int col, Puzzle elem)
    {
        putElem(row, col, elem);
        te[row][col].setFixed(true);
        te[row][col].addPotential(elem.getID());

        if ( ((col == 0) && (row > 1)) || ((col > 0) && (row > 0)) )
        {
            te[row-1][col].addPotentials(rel.getPotential(elem.getID(), elem.getElemSide(TOP)));
        }
        if ( ((row == 0) && (col > 1)) || ((row > 0) && (col > 0)) )
        {
            te[row][col-1].addPotentials(rel.getPotential(elem.getID(), elem.getElemSide(LEFT)));
        }
    }

    public Puzzle getElem(int row, int col)
    {
        return te[row][col].getPuzzle();
    }

    public void removeElem(int row, int col)
    {
        te[row][col].removePuzzle();

        if (row < ROW-1)
        {
            te[row+1][col].clearPotential();
            if (isFixed(row+1, col))
            {
                Integer id = te[row+1][col].getPuzzle().getID();
                te[row+1][col].addPotential(id);
            }
        }
        if (col < COL-1)
        {
            te[row][col+1].clearPotential();
            if (isFixed(row, col+1))
            {
                Integer id = te[row][col+1].getPuzzle().getID();
                te[row][col+1].addPotential(id);
            }
        }

        Puzzle tmp = null;
        if ((row > 0) && (col < COL-1))
        {
            tmp = te[row-1][col+1].getPuzzle();
            te[row][col+1].addPotentials(rel.getPotential(tmp.getID(), tmp.getElemSide(BOTTOM)));
        }
        if ((row < ROW-1) && (col < COL-1))
        {
            tmp = te[row+1][col+1].getPuzzle();
            if (tmp != null)
                te[row][col+1].addPotentials(rel.getPotential(tmp.getID(), tmp.getElemSide(TOP)));
        }
    }

    public Set getPotentials(int row, int col)
    {
        try
        {
            return te[row][col].getPotentials();
        }
        catch (Exception e)
        {
            return new HashSet<Integer>();
        }
    }

    public Set getElemPotentialTOP(int row, int col)
    {
        Puzzle tmp = te[row][col].getPuzzle();
        return rel.getPotential(tmp.getID(), tmp.getElemSide(TOP));
    }

    public Set getElemPotentialRIGHT(int row, int col)
    {
        Puzzle tmp = te[row][col].getPuzzle();
        return rel.getPotential(tmp.getID(), tmp.getElemSide(RIGHT));
    }

    public Set getElemPotentialBOTTOM(int row, int col)
    {
        Puzzle tmp = te[row][col].getPuzzle();
        return rel.getPotential(tmp.getID(), tmp.getElemSide(BOTTOM));
    }

    public Set getElemPotentialLEFT(int row, int col)
    {
        Puzzle tmp = te[row][col].getPuzzle();
        return rel.getPotential(tmp.getID(), tmp.getElemSide(LEFT));
    }

    public void addCheckedElem(int row, int col, Integer elem)
    {
        te[row][col].addChecked(elem);
    }

    public void addCheckedElems(int row, int col, Set elems)
    {
        te[row][col].addCheckeds(elems);
    }

    public Set getChecked(int row, int col)
    {
        return te[row][col].getChecked();
    }

    public void clearCheckedElem(int row, int col)
    {
        //if (row >= 0 && col >= 0)
        te[row][col].clearChecked();
    }

    public boolean isFixed(int row, int col)
    {
        if (row >= 0 && col >= 0)
            return te[row][col].getFixed();
        else
            return false;
    }

    public HashMap<Integer, Set> getElemPotentials(Integer id)
    {
        return rel.getElemPotentials(id);
    }

    public void setElemPotentials(Integer id, HashMap<Integer, Set> hm)
    {
        rel.setElemPotentials(id, hm);
    }

    /*
    public boolean isPotentialEmpty(int row, int col)
    {
        try
        {
            return te[row][col].isPotEmpty();
        }
        catch (Exception e)
        {
            return false;
        }
    }
    */

    /* az állapot mentéséhez, visszaállításához szükségesek */
    public int getROW()
    {
        return ROW;
    }

    public int getCOL()
    {
        return COL;
    }

    public TableElem[][] getTableElem()
    {
        return te;
    }

    public boolean restoreTable(HashMap<Integer, Puzzle> e, Integer[][] t, boolean[][] f, Set<Integer>[][] p, Set<Integer>[][] c)
    {
        boolean result = true;

        for (int x = 0; x < t.length; x++)
        {
            for (int y = 0; y < t[x].length; y++)
            {
                try
                {
                    Puzzle pz = e.get(t[x][y]);
                    te[x][y].setPuzzle(pz);
                    te[x][y].setFixed(f[x][y]);
                    te[x][y].addPotentials(p[x][y]);
                    te[x][y].addCheckeds(c[x][y]);
                }
                catch (Exception ex)
                {
                    return false;
                }
            }
        }

        return result;
    }
}