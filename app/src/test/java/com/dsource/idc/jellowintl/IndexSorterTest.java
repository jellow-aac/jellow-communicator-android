package com.dsource.idc.jellowintl;

import androidx.test.runner.AndroidJUnit4;

import com.dsource.idc.jellowintl.utility.IndexSorter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

public class IndexSorterTest {

    @Test
    public void isPreferencesSortedCorrectly(){
        Integer[] prefArray = {4, 6, 2, 5, 8, 0, 1, 11, 45, 2, 77};
        IndexSorter<Integer> is = new IndexSorter<Integer>(prefArray);
        is.sort();
        Integer[] sortArray = is.getIndexes();
        Integer[] expectedSortArray = {10, 8, 7, 4, 1, 3, 0, 2, 9, 6, 5};
        for (int i = 0; i < sortArray.length; i++) {
            assertEquals(expectedSortArray[i], sortArray[i]);
        }
    }

    @Test
    public void legacyTests(){
        ArrayList<Integer> integerArrayList = new ArrayList<>();
        integerArrayList.add(5);
        integerArrayList.add(3);
        integerArrayList.add(4);
        try{
            IndexSorter indexSorter = new IndexSorter(integerArrayList);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}