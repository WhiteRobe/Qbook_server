package web.gloable;


import org.json.JSONArray;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;

@WebListener()
public class SessionCreatedListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    // Public constructor is required by servlet spec
    public SessionCreatedListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */
      try{
          File f = new File("./ceshi.txt");
          f.createNewFile();
          System.out.println("当前相对地址测试文件输出地址："+f.getAbsoluteFile());
      }catch (Exception e){
          e.printStackTrace();
      }

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
        se.getSession().setMaxInactiveInterval(60*30);// 30min无动作在线时间
        se.getSession().setAttribute("UID",-1); // 统一身份标志 // UID统一在服务器用String记录
        se.getSession().setAttribute("Valicode","test"); // 测试用的验证码
        se.getSession().setAttribute("Cart",new JSONArray()); // 购物车信息
        /*try{
            OriDB dbUser = OriDBManager.produceOriDB();
            se.getSession().setAttribute("DB",dbUser);
            System.out.println("一个新的客户数据库链接建立");
        }catch (Exception e){
            e.printStackTrace();
        }*/
        System.out.println("一个新的客户接入了");
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
        /*try{
            OriDB dbUser =  (OriDB) se.getSession().getAttribute("DB");
            dbUser.getConnection().close();
            System.out.println("一个客户数据库销毁");
        }catch (Exception e){
            e.printStackTrace();
        }*/
        System.out.println("一个客户下线");
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
