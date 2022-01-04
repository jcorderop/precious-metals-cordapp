import static com.sun.xml.bind.Util.getSystemProperty;

public class test {
    public static void main(String[] args) {
        String javaHome  = System.getProperty("java.home");
        System.out.println(javaHome);
        String javaHome2 = getSystemProperty( "java.home" ) ;

        System.out.println(javaHome2);
    }
}
