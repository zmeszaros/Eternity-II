package eternity;

import java.util.HashSet;
import java.util.Set;

public class TableElem implements Cloneable
{
    private boolean fix = false;
    private Puzzle puzzleElem = null;
    private Set<Integer> potentials;
    private Set<Integer> checked;

    public TableElem()
    {
        checked = new HashSet<Integer>();
        potentials = new HashSet<Integer>();
    }

    @Override
    public Object clone()
    {
        TableElem copyTableElem = null;

        try
        {
            copyTableElem = (TableElem) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(e.toString());
        }

        copyTableElem.puzzleElem = (Puzzle) puzzleElem.clone();

        return copyTableElem;
    }

    public void setPuzzle(Puzzle elem)
    {
        puzzleElem = elem;
    }

    public Puzzle getPuzzle()
    {
        return puzzleElem;
    }

    public void removePuzzle()
    {
        puzzleElem = null;
    }

    public void addPotential(Integer id)
    {
        if (id != null)
            potentials.add(id);
    }

    public void addPotentials(Set s)
    {
        if (s != null && !s.contains(null))
        {
            if (potentials.isEmpty())
                potentials.addAll(s);
            else
                potentials.retainAll(s);
        }
    }

    public void clearPotential()
    {
        potentials.clear();
    }

    public Set getPotentials()
    {
        return potentials;
    }

    public void addChecked(Integer id)
    {
        if (id != null)
            checked.add(id);
    }

    public void addCheckeds(Set s)
    {
        if (s != null && !s.contains(null))
            checked.addAll(s);
    }

    public Set getChecked()
    {
        return checked;
    }

    public void clearChecked()
    {
        checked.clear();
    }

    public void setFixed(boolean fixed)
    {
        fix = fixed;
    }

    public boolean getFixed()
    {
        return fix;
    }

    /*
    public boolean isPotEmpty()
    {
        System.out.println("checked: " + Arrays.asList(checked));
        System.out.println("potentials: " + Arrays.asList(potentials));
        if (potentials.isEmpty() || checked.isEmpty())
            return false;

        Set result = (Set)((HashSet)potentials).clone();
        result.retainAll(checked);
        System.out.println("result: " + Arrays.asList(result));
        System.out.println("potentials: " + Arrays.asList(potentials));

        return result.isEmpty();
    }
    */
}