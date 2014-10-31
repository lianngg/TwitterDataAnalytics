package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Property {
    public static String MySQL_DriverName;
    public static String MySQL_SourceName;
    public static String MySQL_Account;
    public static String MySQL_Password;

    public static int num;

    static {
        Properties defaultProps = new Properties();
        InputStream in = null;
        try {

            in = Property.class.getResourceAsStream("connection.properties");
            defaultProps.load(in);
            in.close();
            MySQL_DriverName = defaultProps.getProperty("MySQL_DriverName");
            MySQL_SourceName = defaultProps.getProperty("MySQL_SourceName");
            MySQL_Account = defaultProps.getProperty("MySQL_Account");
            MySQL_Password = defaultProps.getProperty("MySQL_Password");
            num = Integer.parseInt(defaultProps.getProperty("Num"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
