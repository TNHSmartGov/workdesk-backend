package com.tnh.baseware.core.annotations;

import com.tnh.baseware.core.enums.ApiResponseType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiOkResponse {

    Class<?> value();

    ApiResponseType type() default ApiResponseType.LIST;
}
