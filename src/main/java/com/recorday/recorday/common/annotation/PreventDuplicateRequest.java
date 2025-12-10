package com.recorday.recorday.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventDuplicateRequest {

	String key() default "";

	long time() default 3000;

	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
