package dte.masteriot.mdp.travelinfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;



public class LoadURLContent implements Runnable {

    Handler creator; // handler to the main activity, who creates this task
    private String string_URL;

    public LoadURLContent(Handler handler, String strURL) {
        // The constructor accepts 2 arguments:
        // The handler to the creator of this object
        // The URL to load.
        creator = handler;
        string_URL = strURL;
    }

    @Override
    public void run() {
        // initial preparation of the message to communicate with the UI Thread:
        Message msg = creator.obtainMessage();
        Bundle msg_data = msg.getData();

    }
}
