package project.trendpick_pro.global.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.*;

@Target({METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ExeTimer { }
