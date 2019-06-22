package web.database.dao;


public final class OriDBManager {
    /**
     * 初始化静态参数
     */
    private static DBValue dbValue;

    public OriDBManager(){}

    public OriDBManager(DBValue dbValue){
        setDbValue(dbValue);
    }

    /**
     * @return DBValue是否已被初始化
     */
    public static boolean isInitSuccess(){return OriDBManager.dbValue != null;}

    /**
     * 配置 DBValue
     * @return DBValue
     */
    public static DBValue getDbValue() {
        return OriDBManager.dbValue;
    }
    public static void setDbValue(DBValue dbValue) {
        OriDBManager.dbValue = dbValue;
    }

    /**
     * 从默认配置获取一个OriDB
     * @return 一个新的OriDB 或 null
     */
    public static OriDB produceOriDB(){
        if(isInitSuccess())return new OriDBFactory().newInstance(dbValue);
        else return null;
    }

    private static OriDB SingletonDB = null;
    /**
     * 获取单实例
     * @return 单实例OriDB 或 null
     */
    public static OriDB getSingletonOriDB(){
        //OriDB db = (OriDB)OriKeeper.getObj(OriDB.class);
        if(SingletonDB==null){
            SingletonDB = OriDBManager.produceOriDB();
        }
        return SingletonDB;
    }
}
