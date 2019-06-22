package web.gloable;

import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.database.dao.OriTableIniter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.sql.ResultSet;

@WebListener()
public class APPInitListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    // Public constructor is required by servlet spec
    public APPInitListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */
        ServletContext ctx = sce.getServletContext();
        GloableValue.dbValue.DRIVER = ctx.getInitParameter("DRIVER_MYSQL");
        GloableValue.dbValue.DBADMIN = ctx.getInitParameter("AdminName");
        GloableValue.dbValue.DBPW = ctx.getInitParameter("AdminPW");
        GloableValue.dbValue.URL = ctx.getInitParameter("URL");
        GloableValue.RootPath = sce.getServletContext().getRealPath("/");
        GloableValue.UploadImgPath = sce.getServletContext().getRealPath("./") + File.separator+"res"+ File.separator+"img";
        //初始化数据库
        if(!OriDBManager.isInitSuccess()){
            OriDBManager.setDbValue(GloableValue.dbValue);
        }
        //重载数据库
        boolean needReloadDatabase = Boolean.valueOf(ctx.getInitParameter("ReloadDatabase"));
        boolean needAlwaysReloadDatabase = Boolean.valueOf(ctx.getInitParameter("AlwaysReloadDatabase"));
        if(needReloadDatabase){
            File file = new File(GloableValue.RootPath+"/WEB-INF/sqlFile");
            OriDB db = OriDBManager.getSingletonOriDB();
            ResultSet rs = db.preStaQuery("SELECT table_name FROM information_schema.TABLES WHERE table_name ='student'");
            try{
                if(rs.next()){
                    // 测试阶段 每次都重新建立
                    if(needAlwaysReloadDatabase){
                        new OriTableIniter().initTable(OriDBManager.produceOriDB(),file);
                        System.out.println("数据库已存在:数据库已经重新建立.");
                    } else{
                        System.out.println("数据库已存在:数据库已经建立，不再重新建立.");
                    }
                } else {
                    boolean isSuccess = new OriTableIniter().initTable(OriDBManager.produceOriDB(),file);
                    System.out.println("数据库不存在:数据库尝试进行初始化建立："+isSuccess);
                }
            } catch (Exception e){
                boolean isSuccess =new OriTableIniter().initTable(OriDBManager.produceOriDB(),file);
                System.out.println("数据库状况不明:数据库尝试重新建立："+isSuccess);
            }
        }
        System.out.println("服务器初始化完成!");
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
    }
}
