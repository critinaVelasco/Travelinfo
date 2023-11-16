package dte.masteriot.mdp.travelinfo;

import android.util.Log;

public class Item {
    // This class contains the actual data of each item of the dataset
    private static final String TAG = "DATASET";
    private String title;
    private Long key; // In this app we use keys of type Long

    Item(String title, Long key) {
        Log.d(TAG, "Item to be created. Title = " + title + " key = " + Long.toString(key));
        this.title = title;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public Long getKey() {
        return key;
    }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return this.key == ((Item) other).getKey();
    }

}