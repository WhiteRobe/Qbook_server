package web.control;

import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;
import web.tool.GeneralSQLTool;
import web.tool.MD5;

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
        name = "LoginServlet",
        urlPatterns={"/LoginServlet"}
)
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try {
            String jsondata = request.getParameter("jsondata");
            OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            String ID = jsonObject.getString("ID");
            String PW = jsonObject.getString("PW");
            String VC = jsonObject.getString("VC");
            boolean KLS = jsonObject.getBoolean("KLS"); // 记住登陆状态
            String VC_S = (String) session.getAttribute("Valicode");
            if(!VC.equals(VC_S)){
                // 验证码不正确
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","验证码错误!");
                out.println(returnMsg);
                return;
            }
            int UID_S = (int)session.getAttribute("UID");
            if(UID_S!=-1){
                // 已有用户登陆但未合法退出
                session.setAttribute("UID",-1); // 注销之前未合法退出的用户
            }
//            ResultSet rs = db.preStaQuery("select * from UserInfo where ID=? and PW=?",
//                    new String[]{ID, MD5.getMD5(PW, GloableValue.MD5Salt)});

            if(GeneralSQLTool.checkLogin(db, ID, PW)){// 查到该用户
                JSONObject user = GeneralSQLTool.findUserInfo(db, ID, PW);
                int UID = user.getInt("UID");
                int AUTH = user.getInt("AUTH");

                session.setAttribute("UID",UID);
                returnMsg.put("UID",UID);
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","欢迎回来！"+this.getUserTitle(AUTH)+":"+ID+"，即将为您进行跳转!");
            } else {
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","账户密码不匹配!");
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

    public String getUserTitle(int AUTH){
        if(AUTH>=10)return "超级管理员";
        else return "用户";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}
