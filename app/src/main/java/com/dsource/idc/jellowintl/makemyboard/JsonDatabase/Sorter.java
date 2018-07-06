package com.dsource.idc.jellowintl.makemyboard.JsonDatabase;

import com.dsource.idc.jellowintl.utility.JellowIcon;

import java.util.ArrayList;

public class Sorter {
    private ArrayList<JellowIcon> mailIconList;

    public Sorter(ArrayList<JellowIcon> mailIconList) {
        this.mailIconList = mailIconList;
    }

    /**
     * Logic behind this function is
     * 1. Get all the level One Icons ( with or without parents)
     * 2. Get all the level two parents with no level one parents
     * 3. Get all the level three icons with no parents
     */
    public ArrayList<JellowIcon> getLevelOneIcons()
    {
        ArrayList<JellowIcon> l1Icons=new ArrayList<>();
        ArrayList<Integer> level0Parents=new ArrayList<>();
        ArrayList<Integer> level1Parents=new ArrayList<>();
        for(int i=0;i<mailIconList.size();i++)
        {
            JellowIcon icon=mailIconList.get(i);
            if(icon.parent1==-1)//Level 0
            {
                level0Parents.add(icon.parent0);
                l1Icons.add(icon);
            }
            else if(level0Parents.contains(icon.parent0))//Any child of existing leveloneParent will not be added into the list
            {
                continue;
            }
            else if(icon.parent2!=-1)//get All levelTwoIcons with no level one parent
            {
                level1Parents.add(icon.parent1);
                l1Icons.add(icon);
            }
            else if(level1Parents.contains(icon.parent1))//Skip those level 3 elements which have  level 2 parent
            {
                continue;
            }
            else if(icon.parent2!=-1)// Add level 3 icons to the list
            {
                l1Icons.add(icon);
            }



        }

        return l1Icons;
    }

}