package eternity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Relations
{
    private HashMap<Integer, Puzzle> puzzles = null;
    private HashMap<Integer, HashMap> relations = null;

    public Relations()
    {
        relations = new HashMap<Integer, HashMap>();
        puzzles  = new HashMap<Integer, Puzzle>();
    }

    private boolean containsAny(Set a, Set b)
    {
        for (Iterator iter = b.iterator(); iter.hasNext(); )
        {
            if (a.contains(iter.next()))
            {
                return true;
            }
        }
        return false;
    }

    private void searchRelations(Integer k, Puzzle p)
    {
        HashMap<Integer, Set> pages = new HashMap<Integer, Set>();
        Set<Integer> head = new HashSet<Integer>(Arrays.asList(p.getElem()));

        /* végig megy a puzzle (p) mintáin */
        for (Iterator itr = head.iterator(); itr.hasNext();)
        {
            Integer obj = (Integer)itr.next();
            Set<Integer> side = new HashSet<Integer>();

            /* végig megy a puzzle elemein */
            Iterator it = puzzles.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                Integer key = (Integer)entry.getKey();
                Puzzle value = (Puzzle)entry.getValue();

                if (key.equals(k)) continue;

                Set<Integer> elem = new HashSet(Arrays.asList(value.getElem()));

                if ( elem.contains(obj))
                {
                    side.add(key);
                }
            }

            pages.put(obj, side);
        }
        //System.out.println(k + " -> " + pages);

        relations.put(k, pages);

        head = null;
        pages = null;
    }

    public void createRelations(HashMap hm)
    {
        puzzles = hm;

        Iterator it = hm.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry)it.next();
            Integer key = (Integer)entry.getKey();
            Puzzle value = (Puzzle)entry.getValue();

            searchRelations(key, value);
        }

        puzzles = null;
    }

    /**
    * @return visszaadja egy puzzle (id) oldalához (side)
    * tartozó lehetséges elemeket <Set>
    */
    public Set getPotential(Integer id, Integer side)
    {
        HashMap<Integer, Set> pages = relations.get(id);
        return pages.get(side);
    }

    public HashMap<Integer, Set> getElemPotentials(Integer id)
    {
        return relations.get(id);
    }

    public void setElemPotentials(Integer id, HashMap<Integer, Set> hm)
    {
        relations.remove(id);
        relations.put(id, hm);
        //System.out.println(id + " -> " + hm);
    }
}