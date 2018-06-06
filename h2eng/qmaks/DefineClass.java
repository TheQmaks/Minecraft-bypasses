package h2eng.qmaks;

import java.util.Map;
import java.util.HashMap;
import java.nio.file.Files;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.lang.reflect.Constructor;
import java.security.ProtectionDomain;
/**
 *
 * @author Анатолий
 */
public class DefineClass {

    public static ClassLoader launchClassLoader;

    public static void init() {
        try {
            for (Thread thread : getAllStackTraces().keySet()) {
                Field contextClassLoader = Thread.class.getDeclaredField("contextClassLoader");
                contextClassLoader.setAccessible(true);
                ClassLoader classLoader = (ClassLoader) contextClassLoader.get(thread);
                if (classLoader.getClass().getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
                    launchClassLoader = classLoader;
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void defineClass(File file) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, int.class, int.class, ProtectionDomain.class});
            defineClass.setAccessible(true);
            byte[] b = Files.readAllBytes(file.toPath());
            CodeSource cs = new CodeSource(file.toURI().toURL(), new CodeSigner[0]);
            Class c = (Class) defineClass.invoke(launchClassLoader, new Object[] {null, b, 0, b.length, new ProtectionDomain(cs, null)});
            Constructor constructor = c.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            constructor.newInstance(new Object[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        try {
            Method getThreads = Thread.class.getDeclaredMethod("getThreads");
            getThreads.setAccessible(true);
            Thread[] threads = (Thread[]) getThreads.invoke(Thread.currentThread(), new Object[0]);
            Method dumpThreads = Thread.class.getDeclaredMethod("dumpThreads", new Class[]{Thread[].class});
            dumpThreads.setAccessible(true);
            StackTraceElement[][] traces = (StackTraceElement[][]) dumpThreads.invoke(Thread.currentThread(), new Object[]{threads});
            Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
            for (int i = 0; i < threads.length; i++) {
                StackTraceElement[] stackTrace = traces[i];
                if (stackTrace != null) {
                    m.put(threads[i], stackTrace);
                }
            }
            return m;
        } catch (Exception ex) {
            return null;
        }
    }
}