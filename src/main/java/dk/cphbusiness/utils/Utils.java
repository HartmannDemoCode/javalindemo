package dk.cphbusiness.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {
    public static void main(String[] args) {
        System.out.println(getPomProp("db.name"));
    }
    public static String getPomProp(String propName)  {
        // NOTE: This will only work after building the project with maven.
        InputStream is = Utils.class.getClassLoader().getResourceAsStream("properties-from-pom.properties");
        Properties pomProperties = new Properties();
        try {
            pomProperties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pomProperties.getProperty(propName);
    }
}
