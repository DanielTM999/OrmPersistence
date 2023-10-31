package data;


import log.Log;
import log.Logger;

public class LogWriter {
    private static Logger log;

    public static Logger getLogWriter(){
        if(log == null){
            log = new Log(isConsole());
            return log;
        }

        return log;
    }


    public static void EnableAplicationLog(){
        EntityManager.enableLog();
        ConnectionDb.enableLog();
        Repository.enableLog();
    }

    public static void DisableAplicationLog(){
        EntityManager.disableLog();
        ConnectionDb.disableLog();
        Repository.disableLog();
    }

    private static boolean isConsole(){
        try {
            if(Env.getEnv("LOG").equalsIgnoreCase("console")){
                return true;
            }
        } catch (Exception e) {

        }


        return false;
    }


}
