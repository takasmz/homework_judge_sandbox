package compilerutil;


import compilerutil.dto.ExamResultDto;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


public class DynamicEngine {
    //单例
    private static DynamicEngine ourInstance = new DynamicEngine();
 
    public static DynamicEngine getInstance() {
        return ourInstance;
    }
    private URLClassLoader parentClassLoader;
    private String classpath;
    private DynamicEngine() {
        //获取类加载器
        this.parentClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        
        //创建classpath
        this.buildClassPath();
    }
   
    /**
     * @MethodName	: 创建classpath
     * @Description	: TODO
     */
    private void buildClassPath() {
        this.classpath = null;
        StringBuilder sb = new StringBuilder();
        for (URL url : this.parentClassLoader.getURLs()) {
            String p = url.getFile();
            sb.append(p).append(File.pathSeparator);//分隔连续多个路径字符串的分隔符
        }
        this.classpath = sb.toString();
       
    }
    
    /**
     * @MethodName	: 编译java代码到Object
     * @Description	: TODO
     * @param fullClassName   类名
     * @param javaCode  类代码
     * @return Object
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     */
    @SuppressWarnings({ "resource" })
	public Object javaCodeToObject(String fullClassName, String javaCode ) throws IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        long start = System.currentTimeMillis(); //记录开始编译时间
        ExamResultDto examResultDto = new ExamResultDto();
        Object instance;
        //获取系统编译器
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler(); 
        // 建立DiagnosticCollector java文件诊断信息,javaFileObject Java源文件对象，负责源文件对象加载至内存
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
         // 建立用于保存被编译文件名的对象
         // 每个文件被保存在一个从JavaFileObject继承的类中
        ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));
        List<JavaFileObject> jfiles = new ArrayList<>();
        jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));
        //使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-sourcepath");
        options.add(classpath);
        //生成编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
        // 执行编译任务
        boolean success = task.call();
        Method method = null;
        if (success) {
            //如果编译成功，用类加载器加载该类
            JavaClassObject jco = fileManager.getJavaClassObject();
            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(this.parentClassLoader);
            Class<?> clazz = dynamicClassLoader.loadClass(fullClassName,jco);
            instance = clazz.newInstance();
            //clazz.getMethod("Main", parameterTypes)
            method = clazz.getMethod("main",String[].class);
            examResultDto.setInstance(instance);
        } else {
            //如果想得到具体的编译错误，可以对Diagnostics进行扫描
            String error = "";
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                error = error + compilePrint(diagnostic);
            }
            examResultDto.setError(error);
        }
        long end = System.currentTimeMillis();
        examResultDto.setMethod(method);
        examResultDto.setUseTime(end-start);
        //System.out.println("javaCodeToObject use:"+(end-start)+"ms");
		return examResultDto;
        
    }
 
    /**
     * @MethodName	: compilePrint
     * @Description	: 输出编译错误信息
     * @param diagnostic
     * @return
     */
    private String compilePrint(Diagnostic<?> diagnostic) {
//        System.out.println("Code:" + diagnostic.getCode());
//        System.out.println("Kind:" + diagnostic.getKind());
//        System.out.println("Position:" + diagnostic.getPosition());
//        System.out.println("Start Position:" + diagnostic.getStartPosition());
//        System.out.println("End Position:" + diagnostic.getEndPosition());
//        System.out.println("Source:" + diagnostic.getSource());
//        System.out.println("Message:" + diagnostic.getMessage(null));
//        System.out.println("LineNumber:" + diagnostic.getLineNumber());
//        System.out.println("ColumnNumber:" + diagnostic.getColumnNumber());
        StringBuffer res = new StringBuffer();
//        res.append("Code:[" + diagnostic.getCode() + "]\n");
//        res.append("Kind:[" + diagnostic.getKind() + "]\n");
//        res.append("Position:[" + diagnostic.getPosition() + "]\n");
//        res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
//        res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
//        res.append("Source:[" + diagnostic.getSource() + "]\n");
        res.append("LineNumber:" + diagnostic.getLineNumber() + ": "+diagnostic.getMessage(null));
        //res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
        return res.toString();
    }
}