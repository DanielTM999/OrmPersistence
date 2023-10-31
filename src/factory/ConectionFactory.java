package factory;

import java.sql.Connection;

public interface ConectionFactory {
    void createConection();
    Connection adquirConnection();
}
