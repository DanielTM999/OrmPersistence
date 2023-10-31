package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env {
    private static final String PROPATIES_PATH = System.getProperty("user.dir");
    private static Map<String, String> userConfigurations = new HashMap<>();
    private static String args = "";
    private static int length =0;
    private static int cont = 0;


    public static String getEnv(String args){
        readEnv();
        return userConfigurations.get(args);
    }

    private static void readEnv(){
        File env = getEnvFile();
        String FullString = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(env))) {
            String line;
            while ((line = reader.readLine()) != null) {
                FullString += line;
            }
            args = FullString;
            length = args.length();
            KeysToValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getEnvFile() {
        File dir = new File(PROPATIES_PATH);
        for (File file : dir.listFiles()) {
            if (file.getName().equals(".prop")) {
                return file;
            }
        }
        File newFile = new File(PROPATIES_PATH + "/.prop");
        try {
            newFile.createNewFile();
            return getEnvFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void KeysToValue(){
        List<String> elements = new ArrayList<>();

        while (length > cont) {
            char letter = args.charAt(cont);

            if(Character.isWhitespace(letter)){
                cont++;
            }else{
                String element = getElementString();
                if(!element.equals("=")){
                    elements.add(element);
                }
                cont++;
            }
        }

        for (int i = 0; i < elements.size() - 1; i++) {
            if(i == 0){
                userConfigurations.put(elements.get(i), elements.get(i+1));
            }else if(i % 2 == 0){
                userConfigurations.put(elements.get(i), elements.get(i+1));
            }

        }
    }

    private static String getElementString() {
        StringBuilder builder = new StringBuilder();
        char letter = args.charAt(cont);
        while (cont < length && (Character.isLetterOrDigit(letter) || letter == '=' || letter == ':' || letter == '/'
                || letter == '?')) {

            if (letter == ',') {
                break;
            }

            if (!Character.isWhitespace(letter)) {
                builder.append(letter);
            }
            cont++;
            if (cont < length) {
                letter = args.charAt(cont);
            }
        }

        return builder.toString();
    }

}
