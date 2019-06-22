package web.database.dao;



import java.sql.DriverManager;

public final class OriDBFactory {

    /**
     * 产生一个新的OriDB
     * @param driver 数据库驱动
     * @param url 数据库链接
     * @param dbName 登录名
     * @param dbPw 登录密码
     * @return 一个新的OriDB
     */
    public OriDB newInstance(String driver, String url, String dbName, String dbPw){
        OriDB newDb = new OriDB();
        try{
            Class.forName(driver);
            newDb.setConnection(DriverManager.getConnection(url,dbName,dbPw));
        } catch (Exception e){
            e.printStackTrace();
        }
        return newDb;
    }

    /**
     * 按照DBValue的值生产一个OriDB
     * @param dbValue DBValue
     * @return 一个新的OriDB
     */
    public OriDB newInstance(DBValue dbValue){
        return newInstance(dbValue.DRIVER,dbValue.URL,dbValue.DBADMIN,dbValue.DBPW);
    }

}
