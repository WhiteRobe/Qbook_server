package web.gloable;

import web.database.dao.DBValue;

public class GloableValue {
    public static DBValue dbValue = new DBValue();

    public static String RootPath = "./";
    public static String UploadImgPath = "./upload/img";
    public static String MD5Salt = "WhoIsYourDaddy";
}
