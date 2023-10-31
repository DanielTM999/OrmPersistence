package data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import factory.EntityManagerFactory;
import factory.RepositoryFactory;
import factory.anotations.EngineEntity;
import factory.anotations.Identity;
import factory.anotations.OneToMany;
import factory.enums.EngineType;
import factory.enums.LogTypeEnum;
import log.Logger;

public class Repository<S> extends SqlActions<S> implements RepositoryFactory<S> {
    private Class<S> classe;
    private Field[] variables;
    private List<Class<?>> LoadClass;
    private static Logger logger;

    public Repository(Class<S> classe, EntityManagerFactory manager) {
        this.classe = classe;
        LoadClass = manager.getAplicationContexList();
        variables = classe.getDeclaredFields();
        log("Obtendo Contexto da Aplicação e criando Repositorio da Classe "+classe.getName(), LogTypeEnum.INFO, RepositoryFactory.class);
    }

    public static void enableLog() {
        logger = LogWriter.getLogWriter();
    }

    public static void disableLog() {
        logger = null;
    }

    @Override
    public List<S> findAll() {
        List<S> elementList = new ArrayList<>();
        String sql = findAllSQl(classe, LoadClass);
        ResultSet response = ExecuteSelect(sql);

        try {
            while (response.next()) {
                S instance = classe.cast(TransformeToClass(classe, response));
                if(instance != null){
                    elementList.add(instance);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return elementList;
    }

    @Override
    public List<S> findBy(String field, Object value) {
        Field varf = VarField(field);
        isInstanceOf(varf.getType(), value);
        String sql = findBySQL(classe, LoadClass, value, field);
        ResultSet response = ExecuteSelect(sql);
        List<S> elementList = new ArrayList<>();


        try {
            while (response.next()) {
                S instance = classe.cast(TransformeToClass(classe, response));
                if(instance != null){
                    elementList.add(instance);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return elementList;
    }

    @Override
    public void save(S entity) {
        EngineType engine = ConteinsEngine(entity);
        Object value = conteinsIdValue(entity);


        if(value instanceof Long){
            if((long)value == 0){
                if(engine == EngineType.CASCATE){
                    Queue<Class<?>> queue = PrepareListToOrder(entity);

                    executeInsert(entity, queue);

                }
            }else{
                String sql = UpdateSQL(entity.getClass(), entity, value);
                Execute(sql);
            }
        }

    }

    @Override
    public S findById(long id) {
        S instance = null;
        String sql = findByIdSQL(classe, LoadClass, id);
        ResultSet response = ExecuteSelect(sql);

        try {
            while (response.next()) {
                instance = classe.cast(TransformeToClass(classe, response));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return instance;
    }

    @Override
    public void delete(S entity) {
        Object value = conteinsIdValue(entity);
        Field id = getIdEntity(entity);
        String sql = DeleteSQL(entity.getClass(), value, id);
        Execute(sql);
    }

    private Object TransformeToClass(Class<?> classe, ResultSet res) throws Exception{
        Constructor<?> constructor = classe.getConstructor();
        Object instance = constructor.newInstance();
        Field[] variables = classe.getDeclaredFields();

        for (Field field : variables) {
            field.setAccessible(true);
            if(LoadClass.contains(field.getType()) && !field.isAnnotationPresent(OneToMany.class)){
                Object instanceOfSubclass = TransformeToClass(field.getType(), res);
                field.set(instance, instanceOfSubclass);
            }else if(!field.isAnnotationPresent(OneToMany.class)){
                Object value = getValueOfVarible(field, res);
                field.set(instance, value);
            }else if(field.isAnnotationPresent(OneToMany.class)){
                System.out.println(field.getName());
            }
        }

        return instance;

    }

    private void isInstanceOf(Class<?> fieldType, Object value) {
        if (!fieldType.isInstance(value)) {
            throw new RuntimeException("Erro: O valor passado não é do tipo da variável.");
        }

    }

    private Field VarField(String fieldName) {
        for (Field field : variables) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }

        throw new RuntimeException("Erro Variavel " + fieldName + " Nâo encontrada");
    }

    private Queue<Class<?>> PrepareListToOrder(S entity){
        Class<?> classe = entity.getClass();
        Field[] variables = classe.getDeclaredFields();
        List<Class<?>> classList = new ArrayList<>();

        for (Field field : variables) {
            Class<?> subElement = null;
            if(LoadClass.contains(field.getType()) || (subElement != null && LoadClass.contains(subElement))){
                classList.add(field.getType());
            }
        }

        return PrepareQueue(classList, entity);
    }

    private Queue<Class<?>> PrepareQueue(List<Class<?>> classList, S entity) {
        Queue<Class<?>> queue = new ArrayDeque<>();
        ConcurrentMap<Integer, Class<?>> concurrentMap = new ConcurrentHashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (Class<?> class1 : classList) {
            executorService.submit(() -> {
                Field[] variables = class1.getDeclaredFields();
                int count = 0;
                for (Field field : variables) {
                    count += conteisRelation(field, classList);
                }
                concurrentMap.put(count, class1);
            });
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        concurrentMap.entrySet().stream()
                .sorted(ConcurrentMap.Entry.comparingByKey())
                .forEach(entry -> queue.add(entry.getValue()));



        queue.add(entity.getClass());
        return queue;
    }

    private int conteisRelation(Field field, List<Class<?>> classList) {
        if (classList.contains(field.getType())) {
            return 1;
        }
        return 0;
    }

    private EngineType ConteinsEngine(S entity){

        Class<?> classe = entity.getClass();

        if(classe.isAnnotationPresent(EngineEntity.class)){
            EngineEntity anotation = classe.getAnnotation(EngineEntity.class);
            EngineType engine = anotation.engine();
            return engine;
        }

        throw new RuntimeException("Esperava uma anotação do tipo " + EngineEntity.class);
    }

    private Field getIdEntity(S entity){
        Class<?> classe = entity.getClass();

        Field vars[] = classe.getDeclaredFields();
        for (Field field : vars) {
            if (field.isAnnotationPresent(Identity.class)) {
                return field;
            }
        }

        return null;
    }

    private Object conteinsIdValue(S entity){
        Object id = null;
        Field var = getIdEntity(entity);

        if(var ==null){
            throw new RuntimeException("Id is null");
        }

        var.setAccessible(true);
        try {
            id = var.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return id;
    }

    private Object getValueOfVarible(Field field, ResultSet res){
        Object value = null;

        try {
            value = res.getObject(field.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return value;
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

