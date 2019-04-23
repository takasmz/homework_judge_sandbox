package compilerutil;


/**
 * @Description 字符串转换类
 * @Author 李如豪
 * @Date 2019/1/31 14:23
 * @VERSION 1.0
 **/
public class StringFormat {

    public static int StringToInteger(String str){
        return Integer.parseInt(str);
    }

    public static int[] StringToOneDimenstionInteger(String str){
        if(str == null || str.equals("")){
            return new int[]{};
        }
        String[] strs = str.split(",");
        int[] result = new int[strs.length];
        for(int i=0; i<strs.length; i++){
            result[i] = Integer.parseInt(strs[i]);
        }
        return result;
    }




}
