package compilerutil;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class CharSequenceJavaFileObject extends SimpleJavaFileObject {
 
    private CharSequence content;
 
    // 该构造器用来输入源代码
    public CharSequenceJavaFileObject(String className,
                                      CharSequence content) {
    	// 1、先初始化父类，由于该URI是通过类名来完成的，必须以.java结尾。
        // 2、如果是一个真实的路径，比如是file:///test/demo/Hello.java则不需要特别加.java
        // 3、这里加的String:///并不是一个真正的URL的schema, 只是为了区分来源
        super(URI.create("string:///" + className.replace('.', '/')
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }
 
    @Override
    public CharSequence getCharContent(
            boolean ignoreEncodingErrors) {
        return content;
    }
}