## como iniciar

    para iniciar a usar a aplicação é nesscessario Chamar o EntityManagerFactory e criar o arquivo .prop
    porem se tentar rodar a aplicação sem o .prop ele ira criar automaticamente logo apos o erro de prop not found.
    Apos acriar um EntityManagerFactory é nesscesario parrar um contexto de aplicação que são as classes que será as entidades do banco de dados ou pode usar o EntityManager.getAutoContext("model")
    passando o pacote das classes como com.sistema.model


```java
    import data.EntityManager;
    import factory.EntityManagerFactory;

    public class App {
        public static void main(String[] args) {
            EntityManagerFactory em = new EntityManager(EntityManager.getAutoContext("model"));
        }
    }

```
# com Log
```java
    import data.EntityManager;
    import factory.EntityManagerFactory;
    import data.LogWriter;

    public class App {
        public static void main(String[] args) {
            LogWriter.EnableAplicationLog();
            EntityManagerFactory em = new EntityManager(EntityManager.getAutoContext("model"));
        }
    }

```
