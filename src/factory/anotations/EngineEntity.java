package factory.anotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import factory.enums.EngineType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EngineEntity {
    EngineType engine();
}
