package compilerutil.stream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @Description 输出缓存
 * @Author 李如豪
 * @Date 2019/1/31 15:03
 * @VERSION 1.0
 **/
public class CacheOutputStream extends OutputStream {

    private volatile ThreadLocal<ByteArrayOutputStream> localBytesCache = ThreadLocal.withInitial(ByteArrayOutputStream::new);

    @Override
    public void write(int b) {
        ByteArrayOutputStream byteBufferStream = localBytesCache.get();
        byteBufferStream.write(b);
    }

    public byte[] removeBytes(long threadId) {
        ByteArrayOutputStream byteBufferStream = localBytesCache.get();

        if (byteBufferStream == null) {
            return new byte[0];
        }
        byte[] result = byteBufferStream.toByteArray();
        // 因为这个可能以后还可以重用（因为线程时有反复重用的，所以这里只需要将里面的内容清空就可以了）
        byteBufferStream.reset();
        return result;
    }
}
