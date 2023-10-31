import data.EntityManager;
import data.LogWriter;
import data.Repository;
import factory.EntityManagerFactory;
import factory.RepositoryFactory;
import model.Pessoa;

public class App {

    public static void main(String[] args) {
        LogWriter.EnableAplicationLog();
        EntityManagerFactory em = new EntityManager(EntityManager.getAutoContext("model"));
        RepositoryFactory<Pessoa> repo = new Repository<>(Pessoa.class, em);
    }
}
