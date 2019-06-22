package web.control;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;
import web.tool.FileReciver;
import web.tool.MD5;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Savepoint;
import java.util.List;

@WebServlet(
        name = "FileUploadServlet",
        urlPatterns = "/FileUploadServlet"
)
public class FileUploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        JSONObject returnMsg = new JSONObject();
//        try{
//            OriDB db = OriDBManager.produceOriDB();
//            db.beginTransaction();
//            Savepoint sp = db.getConnection().setSavepoint();
//        }catch (Exception e){
//            returnMsg.put("FLAG",false);
//            returnMsg.put("MSG","服务器错误:01!");
//            returnMsg.put("ErrorDetail",e.getStackTrace());
//            out.println(returnMsg);
//            return;
//        }

        try{
            List<FileItem> formItems = new FileReciver().accept(request);
            JSONObject jsonObject = new JSONObject(formItems.get(0).getString());
            FileItem file = formItems.get(1);
            String ISBN = jsonObject.getString("FILENAME");
            String filePath = GloableValue.UploadImgPath + File.separator + MD5.getMD5(ISBN)+".jpg";
            File storeFile = new File(filePath);
            // 在控制台输出文件的上传路径
            System.out.println("文件保存到:" + filePath);
            // 保存文件到硬盘
            file.write(storeFile);//直接写出文件
        }catch (Exception e){
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误:02!");
            returnMsg.put("ErrorDetail",e.getStackTrace());
        }
        out.println(returnMsg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
