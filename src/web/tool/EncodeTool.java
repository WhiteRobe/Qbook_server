package web.tool;

import java.nio.charset.StandardCharsets;

public final class EncodeTool {
    public static String isoToUtf8(String origin){
        try{
            origin = new String(origin.getBytes("ISO-8859-1"), StandardCharsets.UTF_8);
        } catch (Exception e){
            e.printStackTrace();
        }
        return origin;
    }
}
