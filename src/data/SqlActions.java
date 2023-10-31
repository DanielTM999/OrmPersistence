package data;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import factory.anotations.Identity;
import factory.anotations.Join;
import factory.anotations.OneToMany;


public class SqlActions<S> extends ConnectionDb{
    private List<Class<?>> classList = EntityManager.getContextDatabase().getContext();
    private List<String> classListString;
    protected Map<Class<?>, Integer> insertedIds = new HashMap<>();

    protected SqlActions(){
        classListString = getContexStrings();
    }

    protected String DeleteSQL(Class<?> classe, Object id, Field element){
        String deletesql = "DELETE FROM " +classe.getSimpleName().toLowerCase()
        + " WHERE " + element.getName() + " = '" + id+"';";

        return deletesql;
    }

    protected String UpdateSQL(Class<?> classe, S entity, Object id){
        String tableName = classe.getSimpleName().toLowerCase();
        String sql = "UPDATE "+tableName+" SET ";
        Field vars[] = classe.getDeclaredFields();

        for (Field field : vars) {
            field.setAccessible(true);
            if(!isIdentity(field)){

                if(classListString.contains(field.getType().getSimpleName())){
                    sql += field.getName()+" = ";
                    try {
                        Object value = field.get(entity);
                        Field idSubclass = getIdEntity(field.getType());
                        idSubclass.setAccessible(true);
                        Object valueId = idSubclass.get(value);
                        sql += "'"+valueId+"', ";
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }else{
                    sql += field.getName()+" = ";
                    try {
                        Object value = field.get(entity);
                        sql += "'"+value+"', ";
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        getIdentityVarname(entity.getClass());
        sql = sql.substring(0, sql.length()-2) +" WHERE " +getIdentityVarname(entity.getClass()) +" = '"+id+"';";
        return sql;
    }

    protected String findAllSQl(Class<?> classe, List<Class<?>> classList){
        String sql = "";
        boolean isSubstr = false;
        String sub_sql = "SELECT * FROM " + classe.getSimpleName().toLowerCase();
        Field vars[] = classe.getDeclaredFields();
        for (Field field : vars) {
            if(!field.isAnnotationPresent(OneToMany.class)){
                if(classList.contains(field.getType())){
                    String Fkey = field.getType().getSimpleName().toLowerCase()+"_id";
                    String Idname = getIdentityVarname(field.getType());
                    sql = "SELECT * FROM " + classe.getSimpleName().toLowerCase()
                    + " JOIN " + field.getType().getSimpleName().toLowerCase()
                    + " ON " + classe.getSimpleName().toLowerCase()
                    + "."+Fkey+" = " +field.getType().getSimpleName().toLowerCase()
                    + "."+Idname + ";";
                }else{
                    sql = "SELECT * FROM " + classe.getSimpleName().toLowerCase() + ";";
                }
            }else{
                isSubstr = true;
                String classMapName = GetOneTomanyValue(field);
                String Fkey = classe.getSimpleName().toLowerCase() + "_id";

                sub_sql += " JOIN " + classMapName.toLowerCase()
                        + " ON " + classe.getSimpleName().toLowerCase()
                        + ".id = " + classMapName.toLowerCase()
                        + "." + Fkey;


            }
        }


        if(sql.isEmpty() || isSubstr){
            return sub_sql;
        }

        return sql;
    }

    protected String findBySQL(Class<?> classe, List<Class<?>> classList, Object id, String clausu){
        Field[] vars = classe.getDeclaredFields();
        boolean isSubstr = false;
        String sub_sql = "SELECT * FROM " + classe.getSimpleName().toLowerCase();
        String whereClause = " WHERE " + clausu + " = '" + id+"'";

        for (Field field : vars) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                isSubstr = true;
                String classMapName = GetOneTomanyValue(field);
                String Fkey = classe.getSimpleName().toLowerCase() + "_id";

                sub_sql += " JOIN " + classMapName.toLowerCase()
                        + " ON " + classe.getSimpleName().toLowerCase()
                        + ".id = " + classMapName.toLowerCase()
                        + "." + Fkey;
            } else if (classList.contains(field.getType())) {
                String Fkey = field.getType().getSimpleName().toLowerCase() + "_id";
                String Idname = getIdentityVarname(field.getType());

                sub_sql += " JOIN " + field.getType().getSimpleName().toLowerCase()
                        + " ON " + classe.getSimpleName().toLowerCase()
                        + "." + Fkey + " = " + field.getType().getSimpleName().toLowerCase()
                        + "." + Idname;
            }
        }

        if (isSubstr) {
            return sub_sql + whereClause;
        } else {
            return sub_sql + whereClause;
        }
    }

    protected void Execute(String sql){
        Connection connection = getConnection();
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected String findByIdSQL(Class<?> classe, List<Class<?>> classList, long id) {
        Field[] vars = classe.getDeclaredFields();
        boolean isSubstr = false;
        String sub_sql = "SELECT * FROM " + classe.getSimpleName().toLowerCase();
        String whereClause = " WHERE " + classe.getSimpleName().toLowerCase() + ".id = '" + id+"'";

        for (Field field : vars) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                isSubstr = true;
                String classMapName = GetOneTomanyValue(field);
                String Fkey = classe.getSimpleName().toLowerCase() + "_id";

                sub_sql += " JOIN " + classMapName.toLowerCase()
                        + " ON " + classe.getSimpleName().toLowerCase()
                        + ".id = " + classMapName.toLowerCase()
                        + "." + Fkey;
            } else if (classList.contains(field.getType())) {
                String Fkey = field.getType().getSimpleName().toLowerCase() + "_id";
                String Idname = getIdentityVarname(field.getType());

                sub_sql += " JOIN " + field.getType().getSimpleName().toLowerCase()
                        + " ON " + classe.getSimpleName().toLowerCase()
                        + "." + Fkey + " = " + field.getType().getSimpleName().toLowerCase()
                        + "." + Idname;
                whereClause += " AND " + field.getType().getSimpleName().toLowerCase() + ".id = '" + id +"'";
            }
        }

        if (isSubstr) {
            return sub_sql + whereClause;
        } else {
            return sub_sql + whereClause;
        }
    }

    protected void executeInsert(S entity, Queue<Class<?>> queue) {
        if (queue.isEmpty()) {
            return;
        }
        List<Object> values = new ArrayList<>();
        Class<?> entityClass = queue.poll();
        String tableName = entityClass.getSimpleName().toLowerCase();
        Field[] fields = entityClass.getDeclaredFields();
        String insertSQL = "INSERT INTO " + tableName + " (";

        for (Field field : fields) {
            field.setAccessible(true);
            if(!isIdentity(field)){

                if(field.isAnnotationPresent(Join.class)){
                    insertSQL += field.getName()+"_id, ";
                }else{
                     insertSQL += field.getName()+", ";
                }



                if(field.isAnnotationPresent(OneToMany.class)){
                    System.out.println(field.getName());
                }else if(contextInEntity(entity, entityClass)){
                    Object subEntity = GetcontextInEntity(entity, entityClass);
                    values = getListElemObjects(subEntity);
                }else{
                    if(!classListString.contains(field.getType().getSimpleName())){
                        try {
                            Object value = field.get(entity);
                            values.add(value);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }else{
                        if (insertedIds.containsKey(field.getType())) {
                            int referencedId = insertedIds.get(field.getType());
                            values.add(referencedId);
                            executeInsert(entity, queue);
                        }
                    }


                }

            }

        }
        insertSQL = insertSQL.substring(0, insertSQL.length() - 2) + ") VALUES (";
        for (int i = 0; i < values.size(); i++) {
            insertSQL += "?, ";
        }
        insertSQL = insertSQL.substring(0, insertSQL.length() - 2) + ");";

        int idLast = executeQueryInsert(insertSQL, values);
        insertedIds.put(entityClass, idLast);

        executeInsert(entity, queue);
    }

    protected ResultSet ExecuteSelect(String sql){
        Connection con = getConnection();
        Statement statement = null;
        ResultSet resultado = null;

        try {
            statement = con.createStatement();
            resultado = statement.executeQuery(sql);
            return resultado;
        } catch (SQLException e) {
            System.out.println(e.getSQLState());
        }

        return resultado;
    }

    protected String GetOneTomanyValue(Field field){
        OneToMany annotation = field.getAnnotation(OneToMany.class);
        String ClassMapped = annotation.MapByCLass();
        if(ClassMapped == null){
            throw new RuntimeException("valor da anotação não encontrdo");
        }

        return ClassMapped;
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

    private boolean isIdentity(Field field){

        if(field.isAnnotationPresent(Identity.class)){
            return true;
        }

        return false;
    }

    private boolean contextInEntity(S entity, Class<?> entitySubClass){
        Class<?> classe = entity.getClass();

        if(classe == entitySubClass){
            return false;
        }

        return true;
    }

    private Object GetcontextInEntity(S entity, Class<?> entitySubClass){
        Class<?> classe = entity.getClass();
        Field[] variables = classe.getDeclaredFields();
        Object value = null;

        for (Field field : variables) {
            field.setAccessible(true);
            if(field.getType() == entitySubClass){
                try {
                    value = field.get(entity);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }


        if(value == null){
            throw new RuntimeException("objeto " +entitySubClass.getSimpleName() + "Is null");
        }

        return value;
    }

    private List<Object> getListElemObjects(Object subEntity){
        List<Object> values = new ArrayList<>();
        Class<?> classe = subEntity.getClass();
        Field[] variables = classe.getDeclaredFields();

        for (Field field : variables) {
            field.setAccessible(true);
            try {
                if(!isIdentity(field)) {
                    Object value = field.get(subEntity);
                    values.add(value);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        }

        return values;
    }

    private List<String> getContexStrings(){
        List<String> context = new ArrayList<>();
        for (Class<?> classListString : classList) {
            context.add(classListString.getSimpleName());
        }

        return context;
    }

    private int executeQueryInsert(String sql, List<Object> values){
        Connection connection = getConnection();
        int generatedId = -1;
        int cout = 1;
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (Object object : values) {
                stmt.setObject(cout, object);
                cout++;
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0){
                Statement stmtid = connection.createStatement();
                ResultSet rs = stmtid.executeQuery("SELECT LAST_INSERT_ID()");

                if (rs.next()) {
                    int lastId = rs.getInt(1); // Obtém o último ID inserido
                    generatedId = lastId;
                }

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        return generatedId;

    }

    private Field getIdEntity(Class<?> classe) {
        Field vars[] = classe.getDeclaredFields();
        for (Field field : vars) {
            if (field.isAnnotationPresent(Identity.class)) {
                return field;
            }
        }

        return null;
    }

}
