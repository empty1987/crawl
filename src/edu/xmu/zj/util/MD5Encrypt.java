package edu.xmu.zj.util;


public class MD5Encrypt {

	public static String getMD5(byte[] source) {
		String s = null;
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd','e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest();
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2];
			// 所以表示成 16 进制需要 32 个字符
			int k = 0;
			for (int i = 0; i < 16; i++) {
				// 转换成 16 进制字符的转换
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static String getCurrentPwd(String basePwd)
	{
		return getMD5(basePwd.getBytes());
	}
}
