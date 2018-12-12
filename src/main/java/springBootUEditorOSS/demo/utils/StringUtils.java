package springBootUEditorOSS.demo.utils;

/**
 * Created by Administrator on 2016-03-23.
 */
public class StringUtils {
    public static boolean isNull(String param) {
        if (null == param) {
            return true;
        }
        if (null == param.trim() || "".equals(param.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNotNull(String param) {
        return !isNull(param);
    }

    public static boolean isNotBlank(String param){
        if((param != null) && (null != param.trim() && !"".equals(param.trim()))){
            return true;
        }
        return false;
    }

}
