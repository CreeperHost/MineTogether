package net.creeperhost.minetogether.lib.chat.irc.pircbotx.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.types.GenericEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A simple Subscribable event listener system atop PircBotX
 * You are able to subscribe to _any_ subclass of {@link GenericEvent}
 * and any subclass of that will call any registered handlers up the class hierarchy.
 * <p>
 * Created by covers1624 on 24/6/22.
 */
public class EventSubscriberListener implements Listener {

    private static final Logger logger = LogManager.getLogger();

    private static final Class<?>[] EMPTY = {};

    private final Map<Class<?>, Class<?>[]> eventHierarchyCache = new HashMap<>();
    private final Map<Class<?>, List<Handler>> eventHandlers = new HashMap<>();

    @Override
    public void onEvent(Event event) {
        fireEvent(event.getClass(), event);
    }

    private void fireEvent(Class<?> clazz, Event event) {
        fireFor(clazz, event);

        for (Class<?> parent : getHierarchy(clazz)) {
            fireEvent(parent, event);
        }
    }

    private void fireFor(Class<?> clazz, Event event) {
        List<Handler> handlers = eventHandlers.get(clazz);
        if (handlers == null) return;

        for (Handler handler : handlers) {
            try {
                handler.handle.invoke(handler.instance, event);
            } catch (Throwable throwable) {
                logger.error("Failed to invoke event handler.", throwable);
            }
        }
    }

    public void addListener(Object instance) {
        Class<?> listenerClass = instance.getClass();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            for (Method method : listenerClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(SubscribeEvent.class)) continue;

                Class<?>[] args = method.getParameterTypes();
                if (args.length != 1) {
                    logger.error("Method '{}.{}' does not have a singular parameter, Ignoring..", listenerClass.getName(), method.getName());
                    continue;
                }
                Class<?> argument = args[0];
                if (!GenericEvent.class.isAssignableFrom(argument)) {
                    logger.error("Method '{}.{}' first argument is not assignable from GenericEvent, Got '{}', ignoring..", listenerClass.getName(), method.getName(), argument.getName());
                    continue;
                }
                method.setAccessible(true);
                MethodHandle handle = lookup.unreflect(method);

                eventHandlers.computeIfAbsent(argument, e -> new LinkedList<>())
                        .add(new Handler(instance, handle));
            }
        } catch (Throwable e) {
            logger.error("Failed to register listener.", e);
        }
    }

    private Class<?>[] getHierarchy(Class<?> clazz) {
        if (clazz == GenericEvent.class) return EMPTY;

        Class<?>[] hierarchy = eventHierarchyCache.get(clazz);
        if (hierarchy == null) {

            Class<?> superClass = clazz.getSuperclass();
            Class<?>[] interfaces = clazz.getInterfaces();

            if (superClass == null) {
                hierarchy = interfaces;
            } else {
                hierarchy = Arrays.copyOf(interfaces, interfaces.length + 1);
                hierarchy[0] = superClass;
                System.arraycopy(interfaces, 0, hierarchy, 1, interfaces.length);
            }

            eventHierarchyCache.put(clazz, hierarchy);
        }
        return hierarchy;
    }

    private record Handler(Object instance, MethodHandle handle) {
    }
}
