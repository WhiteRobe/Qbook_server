package web.control;

import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Arrays;

@WebServlet(
        name = "UserNameAJAXServlet",
        urlPatterns={"/UserNameAJAXServlet"}
)
public class UserNameAJAXServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            String ID = jsonObject.getString("ID");
            ResultSet rs = db.preStaQuery("select * from UserInfo where ID=?;",new String[]{ID});
            if(rs.next()){
                // 已有同名用户
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","用户名已被占用");
            } else{
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","用户名可以使用");
            }
        }catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail",Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }
}
