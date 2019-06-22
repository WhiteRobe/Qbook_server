package web.control;

import org.json.*;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

@WebServlet(
        name = "TestServlet",
        urlPatterns={"/api/test"}
)
public class TestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        boolean isSuccess = false;
        try{
            String jsondata = request.getParameter("jsondata");
            JSONObject jobj = new JSONObject(jsondata);
            String message = jobj.getString("message");
            System.out.println(message);
            isSuccess=true;
        }catch (Exception e){
            e.printStackTrace();
        }
        JSONObject returnMsg = new JSONObject();
        if(isSuccess) {
            returnMsg.put("StatusCode", "200");
            returnMsg.put("StatusMsg", "操作成功");
        } else {
            returnMsg.put("StatusCode", "400");
            returnMsg.put("StatusMsg", "操作失败");
        }
        out.println(returnMsg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("error way");
    }

    private void write(String[] params){
        try {
            String fileurl = GloableValue.RootPath + "/WEB-INF/data.csv";
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileurl,true), "utf-8");
            PrintWriter fw = new PrintWriter (osw);
            for (String s : params) {
                fw.write(s+",");
            }
            fw.write("\n");
            fw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
