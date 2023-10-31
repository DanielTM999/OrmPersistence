package data;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import factory.ContextDatabase;
import factory.EntityManagerFactory;
import factory.anotations.Atribute;
import factory.anotations.Entity;
import factory.anotations.Identity;
import factory.anotations.Unique;
import factory.enums.LogTypeEnum;
import log.Logger;


public class EntityManager implements EntityManagerFactory{
    private static List<Class<?>> classList;
    private static ContextDatabase context;
    private static Logger logger;


    public EntityManager(ContextDatabase context){
        log("obtendo Contexto da Aplicação", LogTypeEnum.INFO, ContextDatabase.class);
        EntityManager.context = context;
        classList = context.getContext();
        log("Criando Entity Manager", LogTypeEnum.INFO, getClass());
        loadEntitys();
    }

    public static void enableLog(){
        logger =  LogWriter.getLogWriter();
    }

    public static void disableLog(){
        logger =  null;
    }

    public static ContextDatabase getContextDatabase(){
        return context;
    }

    public static ContextDatabase getAutoContext(String MainPackge){
        List<Class<?>> classes = new ArrayList<>();
        String packageName = MainPackge;
        String path = System.getProperty("user.dir") + "/bin/" + packageName.replace(".", "/");
        File packageDir = new File(path);

        if (packageDir.exists() && packageDir.isDirectory()) {
            for (File file : packageDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                       System.out.println(e.getMessage());
                    }
                }
            }
        }

        return new ContextDatabase() {

            @Override
            public List<Class<?>> getContext() {
                return classes;
            }

        };
    }

    @Override
    public void loadEntitys() {
        int count = 1;
        log("iniciando o carregador de entidades", LogTypeEnum.INFO, getClass());
        log("iniciando A chamada de metodo De ordem de criação", LogTypeEnum.INFO, getClass());

        Queue<Class<?>> fila = OrderByCreate();

        while (!fila.isEmpty()) {
            Class<?> classe = fila.poll();
            log("pegando "+ count +"° elemento da fila", LogTypeEnum.INFO, getClass());
            if(classe.isAnnotationPresent(Entity.class)){
                log("Virificando Anotaçoes da classe "+classe.getName(), LogTypeEnum.INFO, getClass());
                log("Criando sql da classe "+classe.getName(), LogTypeEnum.INFO, getClass());
                String sql = "CREATE TABLE IF NOT EXISTS " + classe.getSimpleName().toLowerCase() +"( ";
                Field[] fields = classe.getDeclaredFields();
                for (Field field : fields) {
                    if(field.isAnnotationPresent(Identity.class)){
                        sql += field.getName() + " INT AUTO_INCREMENT PRIMARY KEY,";
                    }else{
                       sql += selectType(field);
                    }


                }
                sql = sql.substring(0, sql.length()-1);
                sql += " );";

                log("Executando Criação da tabela "+classe.getName(), LogTypeEnum.INFO, getClass());
                execute(sql);
                count++;
            }
        }

    }

    @Override
    public List<Class<?>> getAplicationContexList() {
       return classList;
    }

    @Override
    public void showContextAplication(){
        for (Class<?> class1 : classList) {
            System.out.println("[Class] => " + class1.getSimpleName());
        }
    }

    private void execute(String sql){
        Connection con = new ConnectionDb().getConnection();
        Statement statement = null;

        try {
            statement = con.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String selectType(Field field){
        String type = field.getType().getSimpleName();
        String query = "";
        String unique = "";

        if(isUnique(field)){
            unique = "UNIQUE";
        }

        if(type.equalsIgnoreCase("String")){

            if (field.isAnnotationPresent(Atribute.class)) {
                Atribute annotation = field.getAnnotation(Atribute.class);
                int tam = annotation.tam();
                boolean nullable = annotation.nullable();
                query = field.getName() + " VARCHAR(" + tam + ")";
                if (!nullable) {
                    query += " NOT NULL " + unique;
                }
                query += ",";
            }else{
                query = field.getName() + " VARCHAR(255) NOT NULL "+ unique+",";
            }

        }else if(type.equalsIgnoreCase("int")){

            if(field.isAnnotationPresent(Atribute.class)){
                Atribute annotation = field.getAnnotation(Atribute.class);
                boolean nullable = annotation.nullable();
                query = field.getName() + " INT";
                if (!nullable) {
                    query += " NOT NULL " + unique;
                }
                query += ",";
            }else{
                query = field.getName() + " INT NOT NULL "+ unique+",";
            }
        }else if(classList.contains(field.getType())){
            String className = field.getType().getSimpleName().toLowerCase();
            String relation = field.getType().getSimpleName().toLowerCase()+"_id";
            String idName = getIdentityVarname(field.getType());
            query = relation + " INT, FOREIGN KEY ("+relation+") REFERENCES " + className+"("+idName+"),";
        }
        return query;
    }

    private int conteisRelation(Field field){
        if(classList.contains(field.getType())){
            return 1;
        }
        return 0;
    }

    private String getIdentityVarname(Class<?> classe){

        Field vars[] = classe.getDeclaredFields();
        String name = "";
        for (Field field : vars) {
            if(field.isAnnotationPresent(Identity.class)){
                name = field.getName();
            }
        }

        return name;
    }

    private boolean isUnique(Field field){
        if(field.isAnnotationPresent(Unique.class)){
            return true;
        }

        return false;
    }

    private Queue<Class<?>> OrderByCreate() {
        log("iniciando o carregador de orden de criação", LogTypeEnum.INFO, getClass());
        Queue<Class<?>> queue = new ArrayDeque<>();
        ConcurrentMap<Integer, Class<?>> concurrentMap = new ConcurrentHashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        log("Preparando PoolThread", LogTypeEnum.INFO, ExecutorService.class);

        for (Class<?> class1 : classList) {
            log("Analizando a classe " + class1.getName(), LogTypeEnum.INFO, ExecutorService.class);
            executorService.submit(() -> {
                Field[] variables = class1.getDeclaredFields();
                int count = 0;
                for (Field field : variables) {
                    count += conteisRelation(field);
                }
                concurrentMap.put(count, class1);
            });
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log(e.getMessage() , LogTypeEnum.ERROR, ExecutorService.class);
        }

        log("Inserindo elementos na Fila" , LogTypeEnum.INFO, ExecutorService.class);
        concurrentMap.entrySet().stream()
                .sorted(ConcurrentMap.Entry.comparingByKey())
                .forEach(entry -> queue.add(entry.getValue()));

        return queue;
    }

    private void log(String msg, LogTypeEnum type, Class<?> classe){
        if(classe == null){
            classe = getClass();
        }
        if(logger != null){
            if(type == LogTypeEnum.INFO){
                logger.infoLog(msg, classe);
            }else{
                logger.ErrorLog(msg, classe);
            }
        }
    }


}
