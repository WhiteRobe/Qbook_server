package web.tool;

import java.security.MessageDigest;

/**
 * 求算MD5的工具类
 */
public final class MD5 {
    public static String getMD5(String msg){
        return MD5.getMD5(msg,"");
    }
    public static String getMD5(String msg, String salt){
        String retult="";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buff = md.digest((msg+salt).getBytes());
            retult  = toHex(buff);
        }catch (Exception e){
            e.printStackTrace();
        }
        return retult;
    }

    //将128位的二进制序列转为32位的16进制编码
    private static String toHex(byte[] bytes) {
        StringBuilder md5str = new StringBuilder();
        for (byte aByte : bytes) {
            int temp = aByte;
            if (temp < 0) temp += 256; // 0x8* 在经过toHexString转化时，会被转为ffffff8*，需要+256保证其正值
            if (temp < 16) md5str.append("0"); // 0x05 转化会变成 5，缺少一位0
            md5str.append(Integer.toHexString(temp));
        }
        return md5str.toString();
    }
}
