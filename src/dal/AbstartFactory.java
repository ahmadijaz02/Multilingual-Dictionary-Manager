package dal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstartFactory implements IFactory {

    private static IFactory instance = null;

    public static final IFactory getInstance() {
        if (instance == null) {
            String factoryClassName = null;

            try (InputStream input = AbstartFactory.class.getClassLoader().getResourceAsStream("configure.properties")) {
                if (input == null) {
                    return null;
                }

                Properties prop = new Properties();
                prop.load(input);

                factoryClassName = prop.getProperty("dal.factory");

                Class<?> clazz = Class.forName(factoryClassName);
                instance = (IFactory) clazz.getDeclaredConstructor().newInstance();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
}