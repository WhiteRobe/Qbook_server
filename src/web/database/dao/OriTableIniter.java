package web.database.dao;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class OriTableIniter {
    private Statement sta = null;
    private Connection con = null;
    private boolean allRight = true;

    /**
     * 从sql文件处完成数据库OriDB的数据初始化
     * @param db OriDB
     * @param sqlFileURL 要执行的SQL语句地址
     * @return 是否完全成功
     */
    public boolean initTable(OriDB db, String sqlFileURL){
        init(db);
        try{
            FileInputStream fin = new FileInputStream(new File(sqlFileURL));
            BufferedReader in = new BufferedReader(new InputStreamReader(fin,"UTF-8"));
            while (in.ready()){
                String sql = in.readLine();
                if(!sql.equals("\n") && sql.length()>1)SqlExec(sql);
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
            allRight=false;
        }finally {
            ExitDB();
        }
        return allRight;
    }

    /**
     * 从sql文件处完成数据库OriDB的数据载入
     * @param db OriDB
     * @param file sql文件
     * @return 是否完全成功
     */
    public boolean initTable(OriDB db, File file){
        init(db);
        try{
            FileInputStream fin = new FileInputStream(file);
            BufferedReader in =new BufferedReader(new InputStreamReader(fin,"UTF-8"));
            while (in.ready()){
                String sql = in.readLine();
                if(!sql.equals("\n") && sql.length()>1)SqlExec(sql);
            }
            in.close();
        }catch (Exception e){
            allRight=false;
            e.printStackTrace();
        }finally {
            ExitDB();
        }
        return allRight;
    }

    /**
     * 初始化
     * @param db OriDB
     */
    private void init(OriDB db){
        allRight = true;
        sta = db.getNewStatement();
        con = db.getConnection();
    }

    /**
     * 简单执行语句
     * @param str sql语句
     * @return 是否正确完成数据库操作
     */
    private boolean SqlExec(String str){
        try {
            sta.execute(str);
        } catch (SQLException e) {
            System.out.println("F:03 -Prepare Language:[ "+str+" ]Failed!");
            allRight=false;
            e.printStackTrace();
            return false;
        }
        //System.out.println("A:03 -Prepare Language:[ "+str+" ]Acessed!");
        return true;
    }

    /**
     * 关闭该数据库链接
     */
    public void ExitDB() {
        try{
            con.close();
            //System.out.println("F:05 -Database Close Access!");
        }
        catch(Exception e){
            System.out.println("F:05 -Database Close Failed!");
            allRight=false;
        }
    }
}
