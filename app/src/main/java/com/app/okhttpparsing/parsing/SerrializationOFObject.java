package com.app.okhttpparsing.parsing;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * SerrializationOFObject.java : this activity is responsible to
 * serialize object

 */

public class SerrializationOFObject {

    /**
     * <p>
     * Save the object for future use
     * </p>
     *
     * @return true if write successfully else false
     */
    public boolean SaveCompleteObject(Object ahloaclOb, String ahfileName, Context ahContext) {
        try {
            FileOutputStream fos = ahContext.openFileOutput(ahfileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(ahloaclOb);
            os.close();
            return true;
        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }

    /**
     * <p>
     * Reading the object that is save previously
     * </p>
     *
     * @return object if found else return null
     */
    public Object ReadingObject(String ahfileName, Context ahContext) {
        try {
            FileInputStream fis = ahContext.openFileInput(ahfileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            Object ob = is.readObject();
            is.close();
            return ob;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }

    }

}
