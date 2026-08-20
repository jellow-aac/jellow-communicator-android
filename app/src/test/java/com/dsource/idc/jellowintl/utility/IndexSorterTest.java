package com.dsource.idc.jellowintl.utility;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class IndexSorterTest {

    @Test
    public void testIndexSorterWithArray() {
        Integer[] data = {50, 20, 40, 10, 30};
        IndexSorter<Integer> sorter = new IndexSorter<>(data);
        sorter.sort();
        
        // Sorts descending by default (d2.compareTo(d1))
        // Values: 50, 40, 30, 20, 10
        // Indexes: 0,  2,  4,  1,  3
        Integer[] expectedIndexes = {0, 2, 4, 1, 3};
        
        assertArrayEquals(expectedIndexes, sorter.getIndexes());
    }

    @Test
    public void testIndexSorterWithList() {
        List<String> data = Arrays.asList("apple", "orange", "banana", "kiwi");
        IndexSorter<String> sorter = new IndexSorter<>(data);
        sorter.sort();
        
        // Sorts descending
        // Values descending: orange, kiwi, banana, apple
        // Original Indexes:   1,      3,    2,      0
        Integer[] expectedIndexes = {1, 3, 2, 0};
        
        assertArrayEquals(expectedIndexes, sorter.getIndexes());
    }
}