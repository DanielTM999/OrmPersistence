package factory;

import java.util.List;

public interface RepositoryFactory<S> {
    List<S> findAll();
    List<S> findBy(String field, Object value);
    void save(S entity);
    void delete(S entity);
    S findById(long id);
}
