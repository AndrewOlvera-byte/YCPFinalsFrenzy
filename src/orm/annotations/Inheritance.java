package orm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Inheritance {
    InheritanceType strategy() default InheritanceType.SINGLE_TABLE;
}
