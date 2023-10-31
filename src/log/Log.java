package log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log implements Logger{
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public boolean isConsole;

    public Log(boolean isConsole){
        this.isConsole = isConsole;
    }

    @Override
    public void ErrorLog(String message, Class<?> classe){
        String path = System.getProperty("user.dir") + "/app.log";
        File file = new File(path);
        Date dataHoraAtual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String data = formato.format(dataHoraAtual);
        String msgformated = data + ": [ERROR] --> "+ message + " : " + classe.getSimpleName();


        if(isConsole){
            System.out.println(msgformated);
        }else{
            if(file.exists()){
                try {
                    BufferedWriter whiter = new BufferedWriter(new FileWriter(file, true));
                    whiter.write("\n"+msgformated);
                    whiter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public void infoLog(String message, Class<?> classe){
        String path = System.getProperty("user.dir") + "/app.log";
        File file = new File(path);
        Date dataHoraAtual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String data = formato.format(dataHoraAtual);
        String msgformated = data + ": [INFO] --> "+ message + " : " + classe.getSimpleName();


        if(isConsole){
            System.out.println(msgformated);
        }else{
            if(file.exists()){
                try {
                    BufferedWriter whiter = new BufferedWriter(new FileWriter(file, true));
                    whiter.write("\n"+msgformated);
                    whiter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
