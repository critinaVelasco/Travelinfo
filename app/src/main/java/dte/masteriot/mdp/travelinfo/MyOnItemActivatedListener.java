package dte.masteriot.mdp.travelinfo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.OnItemActivatedListener;

public class MyOnItemActivatedListener implements OnItemActivatedListener<Long> {

    // This class serves to "Register an OnItemActivatedListener to be notified when an item
    // is activated (tapped or double clicked)."
    // [https://developer.android.com/reference/androidx/recyclerview/selection/OnItemActivatedListener]

    private static final String TAG = "TAGListOfItems, MyOnItemActivatedListener";

    private final Context context;
    private Dataset dataset; // reference to the dataset, so that the activated item's data can be accessed if necessary

    public MyOnItemActivatedListener(Context context, Dataset ds) {
        this.context = context;
        this.dataset = ds;
    }

    // ------ Implementation of methods ------ //

    @SuppressLint("LongLogTag")
    @Override
    public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails itemdetails,
                                   @NonNull MotionEvent e) {
        // From [https://developer.android.com/reference/androidx/recyclerview/selection/OnItemActivatedListener]:
        // "Called when an item is "activated". An item is activated, for example,
        // when no selection exists and the user taps an item with her finger,
        // or double clicks an item with a pointing device like a Mouse."

        Log.d(TAG, "Clicked item with position = " + itemdetails.getPosition()
                + " and key = " + itemdetails.getSelectionKey());

        Intent i = new Intent(context, InfoMonument.class);
        context.startActivity(i);
        return true;
    }
}