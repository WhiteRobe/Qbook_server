package web.control;

import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;
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
import java.util.regex.Pattern;

@WebServlet(
        name = "RegisterServlet",
        urlPatterns={"/RegisterServlet"}
)
public class RegisterServlet extends HttpServlet {
    private static final String REGEX_USERNAME = "^[a-zA-Z0-9]{6,12}$";
    private static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,32}$";
    private static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            String ID = jsonObject.getString("ID");
            String PW = jsonObject.getString("PW");
            String EMAIL = jsonObject.getString("EMAIL");
            String VC = jsonObject.getString("VC");
            String VC_S = (String) session.getAttribute("Valicode");
            if(!Pattern.matches(REGEX_USERNAME, ID)){
                // 用户名格式不正确
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","用户名格式错误!");
                out.println(returnMsg);
                return;
            }
            if(!Pattern.matches(REGEX_PASSWORD, PW)){
                // 密码格式不正确
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","密码格式错误!");
                out.println(returnMsg);
                return;
            }
            if(!Pattern.matches(REGEX_EMAIL, EMAIL)){
                // 邮箱格式不正确
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","邮箱格式错误!");
                out.println(returnMsg);
                return;
            }
            if(!VC.equals(VC_S)){
                // 验证码不正确
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","验证码错误!");
                out.println(returnMsg);
                return;
            }
            ResultSet rs = db.preStaQuery("select * from UserInfo where ID=?",new String[]{ID});
            if(rs.next()){
                // 已有同名用户
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","用户名已被占用");
                out.println(returnMsg);
                return;
            }
            boolean isSuccess = db.preStaExec("insert into UserInfo(ID,PW,EMAIL) values(?,?,?)",
                    new String[]{ID,MD5.getMD5(PW,GloableValue.MD5Salt),EMAIL}); // 执行注册
            if(isSuccess){
                rs = db.preStaQuery("select * from UserInfo where ID=? and PW=?",
                        new String[]{ID, MD5.getMD5(PW, GloableValue.MD5Salt)}); // 查询刚注册的用户的UID
                rs.next();
                int UID = rs.getInt(1); // 此处可用String
                session.setAttribute("UID",UID);
                returnMsg.put("UID",UID);
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","注册成功!");
            } else{
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","注册失败，请重试!");
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}
