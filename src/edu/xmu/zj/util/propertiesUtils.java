package edu.xmu.zj.util;
 
import java.util.MissingResourceException;
import java.util.ResourceBundle;
 
/**
 * 获取配置资源文件 [公共参数] 信息
 * @author Henry_zp
 */
public class propertiesUtils {
    private String propertyFileName;
    private ResourceBundle resourceBundle;
    public  propertiesUtils() {
        propertyFileName = "config/config";
        resourceBundle = ResourceBundle.getBundle(propertyFileName);
    }
    public String getString(String key) {
        if (key == null || key.equals("") || key.equals("null")) {
            return "";
        }
        String result = "";
        try {
            result = resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static void main(String[] arg) {
    	propertiesUtils utils = new propertiesUtils();
    	System.out.println(utils.getString("imgFile"));
	}
}