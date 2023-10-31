package data;


import java.sql.Connection;
import java.sql.DriverManager;
import factory.ConnectionFactory;
import factory.enums.LogTypeEnum;
import log.Logger;

public class ConnectionDb implements ConnectionFactory{
    private static Connection connection = null;
    private static Logger logger;

    @Override
    public Connection getConnection(){
        if(connection == null){
            log("Criando conexão com o bando de dados", LogTypeEnum.INFO, ConnectionFactory.class);
            String driver = selectTypedbString();
            String url = Env.getEnv("HOST");
            String user = Env.getEnv("USER");
            String senha = Env.getEnv("PASSWORD");

            try {
                Class.forName(driver);
                connection = DriverManager.getConnection(url, user, senha);
                log("Coneção estabelecida Retornando conexão", LogTypeEnum.INFO, ConnectionFactory.class);
                return connection;
            } catch (Exception e) {
                log(e.getMessage(), LogTypeEnum.ERROR, ConnectionFactory.class);
            }

        }
         log("Adiquirindo conexão com o bando de dados", LogTypeEnum.INFO, ConnectionFactory.class);
        return connection;
    }

    public static void enableLog() {
        logger = LogWriter.getLogWriter();
    }

    public static void disableLog() {
        logger = null;
    }

    private String selectTypedbString(){
        String driver = Env.getEnv("DBTYPE");
        if(driver != null){
            if(driver.equalsIgnoreCase("mysql")){
                return "com.mysql.cj.jdbc.Driver";
            }
        }

        return "com.mysql.cj.jdbc.Driver";
    }

    private void log(String msg, LogTypeEnum type, Class<?> classe) {
        if (classe == null) {
            classe = getClass();
        }
        if (logger != null) {
            if (type == LogTypeEnum.INFO) {
                logger.infoLog(msg, classe);
            } else {
                logger.ErrorLog(msg, classe);
            }
        }
    }

}
