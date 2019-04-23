package compilerutil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class DynaCompTest
{
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        String[] strs = line.split(",");
        System.out.println(Arrays.stream(strs).mapToInt(Integer::parseInt).sum());
//        String fullName = "DynaClass";
//        StringBuilder src = new StringBuilder();
//        src.append("public class DynaClass {\n");
//        src.append("    public String toString() {\n");
//        src.append("        return \"Hello, I am \" + ");
//        src.append("this.getClass().getSimpleName();\n");
//        src.append("    }\n");
//        src.append("}\n");
//
//        String test =
//				 "public class Main{\n"
//        		+ "public int main(int x,int y){\n"
//        		+ "return x+y;\n"
//        		+ "}\n"
//        		+ "}\n";
//        System.out.println(test);
//        DynamicEngine de = DynamicEngine.getInstance();
//        Map<String,Object> results =  (Map<String,Object>) de.javaCodeToObject("Main",test.toString());
//        Method method = (Method) results.get("method");
//        Object instance = results.get("instance");
//        Object result = method.invoke(instance,new Object[] {1,2});//方式一
//        System.out.println(result);
    }
}
