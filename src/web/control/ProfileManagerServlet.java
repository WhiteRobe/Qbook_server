package web.control;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;
import web.tool.EncodeTool;
import web.tool.FileReciver;
import web.tool.MD5;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet(
        name = "ProfileManagerServlet",
        urlPatterns = "/ProfileManagerServlet"
)
public class ProfileManagerServlet extends HttpServlet {
    private static final String REGEX_USERNAME = "^[a-zA-Z0-9]{6,12}$";
    private static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,32}$";
    private static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 修改用户资料
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        OriDB db = OriDBManager.produceOriDB();
        Savepoint sp = null;
        try{
            assert db != null;
            db.beginTransaction();
            sp = db.getConnection().setSavepoint();
        }catch (Exception e){
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误:01!");
            returnMsg.put("ErrorDetail",e.getStackTrace());
            out.println(returnMsg);
            db.exit();
            return;
        }

        try{
            List<FileItem> formItems = new FileReciver().accept(request);
            JSONObject jsonObject = new JSONObject(EncodeTool.isoToUtf8(formItems.get(0).getString())); // 需要重新编码 iso -> utf8

            int UID_S = (int)session.getAttribute("UID");
            int UID = jsonObject.getInt("UID");

            if(UID!=UID_S || UID_S==-1){ // 校验登陆状态
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                }
                out.println(returnMsg);
                db.rollback(sp);db.exit();
                return;
            }

            //String ID = jsonObject.getString("ID");
            String PW = jsonObject.getString("PW");
            String NEW_PW = jsonObject.getString("NEWPW");// 新密码
            String EMAIL = jsonObject.getString("EMAIL");
            String AVATAR = jsonObject.getString("AVATAR");
            String AVATARURL = GloableValue.UploadImgPath + File.separator + MD5.getMD5(""+UID_S)+".jpg";

            // 格式校验
            if(!NEW_PW.equals("") && !Pattern.matches(REGEX_PASSWORD, NEW_PW)){
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","新密码的格式不符合规范！");
                out.println(returnMsg);
                db.rollback(sp);db.exit();
                return;
            }
            if(!Pattern.matches(REGEX_EMAIL, EMAIL)){
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","邮箱的格式不符合规范！");
                out.println(returnMsg);
                db.rollback(sp);db.exit();
                return;
            }
//            if(!Pattern.matches(REGEX_USERNAME, ID)){
//                returnMsg.put("FLAG",false);
//                returnMsg.put("MSG","用户名的格式不符合规范！");
//                out.println(returnMsg);
//                db.rollback(sp);db.exit();
//                return;
//            }

//            ResultSet rs = db.preStaQuery("select * from UserInfo where ID=?",new String[]{ID});
//            if(rs.next()){
//                returnMsg.put("FLAG",false);
//                returnMsg.put("MSG","用户名已被使用！");
//                out.println(returnMsg);
//                db.rollback(sp);db.exit();
//                return;
//            }

            PreparedStatement ps = db.getConnection().prepareStatement("select * from UserInfo where UID=? and PW=?");
            ps.setInt(1,UID_S);ps.setString(2, MD5.getMD5(PW, GloableValue.MD5Salt));
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                // 账号和密码不匹配
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","账户名和密码不匹配!");
                db.rollback(sp);
            } else {
                // 是合法修改资料的操作
                ps = db.getConnection().prepareStatement("update UserInfo set PW=?,EMAIL=? where UID=?");
                String pwToChange = NEW_PW.equals("")?MD5.getMD5(PW,GloableValue.MD5Salt):MD5.getMD5(NEW_PW,GloableValue.MD5Salt);
                ps.setString(1,pwToChange);
                ps.setString(2,EMAIL);
                ps.setInt(3,UID_S);
                if(ps.executeUpdate()>0){
                    if(!AVATAR.equals("NULL")){// 存储图片
                        FileItem pic = formItems.get(1); // 头像图片
                        pic.write(new File(AVATARURL));// 保存文件到硬盘
                        PreparedStatement ps2 = db.getConnection().prepareStatement("update UserInfo set AVATAR=? where UID=?");
                        ps2.setString(1,MD5.getMD5(""+UID_S)+".jpg");
                        ps2.setInt(2,UID_S);
                        if(ps2.executeUpdate()<0){
                            returnMsg.put("FLAG",false);
                            returnMsg.put("MSG","资料修改失败!(头像修改失败!)请刷新页面后重试!");
                            boolean deleted = new File(AVATARURL).delete();
                            out.println(returnMsg);
                            db.rollback(sp);db.exit();
                            return;
                        }
                    }
                    returnMsg.put("FLAG",true);
                    returnMsg.put("MSG","资料修改成功!");
                    db.commit();
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","资料修改失败!请刷新页面后重试!");
                    db.rollback(sp);
                }
            }
        } catch (Exception e){
            db.rollback(sp);
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail",Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        } finally {
            db.exit();
        }
        out.println(returnMsg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取用户资料
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            OriDB db = OriDBManager.getSingletonOriDB();
            String jsondata = request.getParameter("jsondata");
            JSONObject jsonObject = new JSONObject(jsondata);
            int UID = jsonObject.getInt("UID");
            int UID_S = (int)session.getAttribute("UID");
            if(UID!=UID_S || UID_S==-1){ // 校验登陆状态
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                }
                out.println(returnMsg);
                return;
            }
            PreparedStatement ps = db.getConnection().prepareStatement("select * from UserInfo where UID=?");
            ps.setInt(1,UID_S);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                returnMsg.put("UID",UID_S);
                returnMsg.put("ID",rs.getString(2));
                // returnMsg.put("PW",rs.getString(3));
                returnMsg.put("EMAIL",rs.getString(4));
                returnMsg.put("AUTH",rs.getInt(5));
                returnMsg.put("AVATAR",rs.getString(6));
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","查询成功！");
            } else {
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","未查询到相关资料！请刷新页面后重试！");
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
