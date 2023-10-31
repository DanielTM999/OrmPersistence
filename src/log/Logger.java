package log;

public interface Logger {
    void ErrorLog(String message, Class<?> classe);
    public void infoLog(String message, Class<?> classe);
}
