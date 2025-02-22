package de.jeff_media.discordstepsisterverifier.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FileUtils {

    public static String inOneLine(File file) {
        try(
                InputStream inputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {

            ArrayList<String> lines = new ArrayList<>(bufferedReader.lines().toList());

            return String.join("",lines);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
