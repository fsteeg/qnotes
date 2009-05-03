package com.quui.notes.editor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Fabian Steeg (fsteeg)
 * 
 */
public final class Util {
    private Util() {}

    public static String read(String location) {
        StringBuilder buf = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(location));
            String line = "";
            while ((line = reader.readLine()) != null) {
                buf.append(line).append("\n");
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }
}
