package science.aist.neo4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Transaction {
    enum Mode {READ, WRITE}

    Mode mode() default Mode.WRITE;

    String transactionManager() default "transactionManager";
}
