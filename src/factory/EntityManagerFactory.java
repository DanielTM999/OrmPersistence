package factory;

import java.util.List;

public interface EntityManagerFactory {
    void loadEntitys();
    List<Class<?>> getAplicationContexList();
    void showContextAplication();
}
