package ch.cyberduck.runner;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by yla on 13.11.2014.
 */
public class AllTestsRunner {

    @Test
    public static void main(final String[] args) {
        Reflections reflections = new Reflections(ClasspathHelper.forPackage("ch.cyberduck"),
                new MethodAnnotationsScanner());
        Set<Method> methods = reflections.getMethodsAnnotatedWith(Test.class);
        List<Class> classes = new ArrayList<>();
        for (Method method : methods) {
            classes.add(method.getDeclaringClass());
            System.out.println(method.getDeclaringClass().getName());
        }
        System.out.println("jetzt rennen " + classes.size());
        JUnitCore.runClasses(ch.cyberduck.core.DefaultIOExceptionMappingServiceTest.class);
//        JUnitCore.runClasses(classes.toArray(new Class[classes.size()]));
    }
}
