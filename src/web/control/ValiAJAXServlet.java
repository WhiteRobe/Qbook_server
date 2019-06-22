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
import java.util.Map;

@WebServlet(
        name = "ValiAJAXServlet",
        urlPatterns={"/ValiAJAXServlet"}
)
public class ValiAJAXServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            JSONObject jsonObject = new JSONObject(jsondata);
            String VC = jsonObject.getString("VC");
            String VC_S = (String) session.getAttribute("Valicode");
            if(VC.equals(VC_S)){
                // 验证码正确
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","验证码正确!");
            } else{
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","验证码错误!");
            }
        }catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail",Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg.toString());
    }
}
