package compilerutil;


import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

/**
 * @Description 沙箱安全管理器
 * @Author 李如豪
 * @Date 2019/1/24 11:53
 * @VERSION 1.0
 **/
public class SandboxSecurityManager extends SecurityManager {

    /**
     * 防止有人非法退出虚拟机
     */
    @Override
    public void checkExit(int status) {
        if (status != 1238888) {
            throw new RuntimeException("非法退出，不允许退出虚拟机");
        }
        super.checkExit(status);
    }

    @Override
    public void checkPermission(Permission perm) {
        conformPermissionToSandbox(perm);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        conformPermissionToSandbox(perm);
    }

    /**
     * 只给与必要的权限（比如读取，获取某些信息等），避免提交者进行非法操作。
     *
     * @param perm 权限
     */
    private void conformPermissionToSandbox(Permission perm) {
        if (perm instanceof SecurityPermission) {
            if (perm.getName().startsWith("getProperty")) {
                return;
            }
        } else if (perm instanceof PropertyPermission) {
            if (perm.getActions().equals("read")) {
                return;
            }
        } else if (perm instanceof FilePermission) {
            if (perm.getActions().equals("read")) {
                return;
            }
        } else if (perm instanceof RuntimePermission
                || perm instanceof ReflectPermission
                || perm instanceof LoggingPermission) {
            return;
        }

        throw new SecurityException(perm.toString() + "无法使用该权限");
    }

}
