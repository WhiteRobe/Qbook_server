package web.control;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@WebServlet(
        name = "CheckLoginStatusServlet",
        urlPatterns={"/CheckLoginStatusServlet"}
)
/**
 * 该servlet用于保证客户端与服务端的状态一致以及客户端掉线重连
 */
public class CheckLoginStatusServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 用于用户登录状态同步
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            JSONObject jsonObject = new JSONObject(jsondata);
            int UID = jsonObject.getInt("UID");
            int UID_S = (int) session.getAttribute("UID");

            if(UID==UID_S){
                if(UID==-1){ // 用户未登陆
                    returnMsg.put("FLAG",false); // 任何返回false的校验结果 都会重置客户端状态
                    returnMsg.put("MSG","用户未登录!");
                } else{ // 服务器与客户端同步成功:用户登陆状态同步成功
                    returnMsg.put("UID",UID_S);
                    returnMsg.put("FLAG",true);
                    returnMsg.put("MSG","用户["+UID_S+"]在线!");
                }
            } else{
                // 客户端与服务器登陆不同步
                if(UID==-1){ // 客户端单方面掉线:重新登陆
                    returnMsg.put("UID",UID_S);
                    returnMsg.put("FLAG",true);
                    returnMsg.put("MSG","客户端掉线，用户["+UID_S+"]重新登陆!");
                } else{ // 服务端掉线或客户端状态错误:重置服务器侧和客户端侧
                    returnMsg.put("FLAG",false); // 任何返回false的校验结果 都会重置客户端状态
                    returnMsg.put("MSG","客户端["+UID+"]与服务器["+UID_S+"]不一致!");
                    session.setAttribute("UID",-1); // 重置服务器侧
                }
            }
        }catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail",Arrays.toString(e.getStackTrace()));
            session.setAttribute("UID",-1);
            e.printStackTrace();
        }
        out.println(returnMsg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 用于用户退出登陆
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            JSONObject jsonObject = new JSONObject(jsondata);
            int UID = jsonObject.getInt("UID");
            int UID_S = (int) session.getAttribute("UID");
            if(UID==UID_S){
                session.setAttribute("UID",-1); // 退出
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","您已成功退出！");
            } else{
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","与服务器同步错误！请刷页面新后重新尝试退出！");
            }
        } catch (Exception e){
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!\n"+ Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }
}
