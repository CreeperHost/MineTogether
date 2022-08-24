package net.creeperhost.minetogether.lib.chat.annotation;

import net.creeperhost.minetogether.lib.chat.util.HashLength;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by covers1624 on 22/8/22.
 */
@Retention (RetentionPolicy.SOURCE)
@Target ({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
public @interface HashLen {

    HashLength value();
}
