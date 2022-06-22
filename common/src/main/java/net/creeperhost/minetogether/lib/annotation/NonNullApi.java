package net.creeperhost.minetogether.lib.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by covers1624 on 20/6/22.
 */
@Nonnull
@Retention (RetentionPolicy.RUNTIME)
@TypeQualifierDefault ({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface NonNullApi {
}
