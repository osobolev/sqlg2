package sqlg2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods marking that given method is a business method
 * and should be present at data access interface. Business method won't be called
 * at preprocess phase so all SQL queries (if they are present in this method) won't be checked for
 * correctness. This annotation is usually used for higher-level business methods that are using
 * business methods of other (lower-level) data access interfaces.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface BusinessNoSql {
}
