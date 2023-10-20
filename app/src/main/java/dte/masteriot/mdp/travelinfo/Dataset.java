package dte.masteriot.mdp.travelinfo;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Dataset {

    // This dataset is a list of Items

    private static final String TAG = "DATASET";
    private List<Item> listofitems;

    Dataset(ArrayList<String> namesList) {
        Log.d(TAG, "Dataset() called, namesList = " + namesList.toString());
        listofitems = new ArrayList<>();
        for(int i = 0; i < namesList.size(); i++) {
            String monumentName = namesList.get(i);
            Log.d(TAG, "Dataset() called, monumentName = " + monumentName + " i = " + Integer.toString(i));
            listofitems.add(new Item(monumentName , (long) i));
        }
    }

    int getSize() {
        return listofitems.size();
    }

    Item getItemAtPosition(int pos) {
        return listofitems.get(pos);
    }

    Long getKeyAtPosition(int pos) {
        return (listofitems.get(pos).getKey());
    }

    public int getPositionOfKey(Long searchedkey) {
        // Look for the position of the Item with key = searchedkey.
        // The following works because in Item, the method "equals" is overriden to compare only keys:
        int position = listofitems.indexOf(new Item("placeholder", searchedkey));
        //Log.d(TAG, "getPositionOfKey() called for key " + searchedkey + ", returns " + position);
        return position;
    }

    void removeItemAtPosition(int i) {
        listofitems.remove(i);
    }

    void removeItemWithKey(Long key) {
        removeItemAtPosition(getPositionOfKey(key));
    }

}
