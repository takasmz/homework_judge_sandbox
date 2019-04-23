package compilerutil;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class JavaClassObject extends SimpleJavaFileObject {
 
    protected final ByteArrayOutputStream bos =
        new ByteArrayOutputStream();
 
    //获得编译类
    public JavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/')
            + kind.extension), kind);
    }
    // 获取编译成功的字节码byte[]
    public byte[] getBytes() {
        return bos.toByteArray();
    }
 
    @Override
    public OutputStream openOutputStream() throws IOException {
        return bos;
    }
}