package web.control;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;
import web.tool.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@WebServlet(
        name = "GoodsManagerServlet",
        urlPatterns = "/GoodsManagerServlet"
)
public class GoodsManagerServlet extends HttpServlet {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    private static final String REGEX_ISBN ="^\\d{3}-\\d-\\d{3}-\\d{5}-\\d$";
    // 用于修改商品
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            int UID = jsonObject.getInt("UID");
            int UID_S = (int)session.getAttribute("UID");
            // 检查用户登陆状态，确保是有效用户
            if(UID!=UID_S || UID_S==-1){
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                }
                out.println(returnMsg);
                db.rollback(sp);db.exit();// 释放资源 提前结束
                return ;
            }
            // 获取用户权限
            int AUTH = GeneralSQLTool.getAUTH(db,UID_S);
            if(AUTH<10){
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","用户不具备权限或数据库正在更新!请重新登陆后再试！");
                out.println(returnMsg);
                db.rollback(sp);db.exit();
                return ;
            }

            String METHOD = jsonObject.getString("METHOD");
            JSONObject ITEM = jsonObject.getJSONObject("ITEM");
            switch (METHOD){
                case "INSERT":
                    returnMsg = addGoods(db, sp, ITEM, formItems.get(1));
                    break;
                case "MODIFY":
                    boolean WITHPIC = jsonObject.getBoolean("WITHPIC");
                    returnMsg = modifyGoods(db, sp, ITEM, WITHPIC?formItems.get(1):null);
                    break;
                case "REMOVE":
                    returnMsg = removeGoods(db, sp, ITEM);
                    break;
                default:
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","未知商品操作!");
                    db.rollback(sp);
                    break;
            }
        }catch (Exception e){
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误:02!");
            returnMsg.put("ErrorDetail",e.getStackTrace());
            e.printStackTrace();
            db.rollback(sp);
        } finally {
            db.exit();
        }

        out.println(returnMsg);
    }

    // 用于获取商品信息
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            int IUID = jsonObject.getInt("IUID");
            int UID = jsonObject.getInt("UID");
            int UID_S = (int)session.getAttribute("UID");

            PreparedStatement ps = db.getConnection().prepareStatement("select * from GoodsInfo where IUID=?");
            ps.setInt(1,IUID);
            ResultSet rs = ps.executeQuery();
            JSONObject item = new JSONObject(); // 存放商品的JSON数据
            if(rs.next()){
                //int IUID = rs.getInt(1);item.put("IUID",IUID);
//                String INAME = rs.getString(2);item.put("INAME",INAME);
//                String AUTHOR = rs.getString(3);item.put("AUTHOR",AUTHOR);
//                String PUBLISHER = rs.getString(4);item.put("PUBLISHER",PUBLISHER);
//                String DESCS = rs.getString(5);item.put("DESCS",DESCS);
//                double PRICE = rs.getDouble(6);item.put("PRICE",PRICE);
//                int DISCOUNT = rs.getInt(7);item.put("DISCOUNT",DISCOUNT);
//                String COVER_PIC = rs.getString(8);item.put("COVER_PIC",COVER_PIC);
//                boolean ONSALE = rs.getBoolean(9);item.put("ONSALE",ONSALE);
//                int SALED = rs.getInt(10);item.put("SALED",SALED);
//                String ODATE = rs.getString(11);item.put("ODATE",ODATE);
//                String ISBN = rs.getString(12);item.put("ISBN",ISBN);
                item = new BeanSealer().GoodsInfoBean(rs);
            } else {
                returnMsg = new JSONObject();
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","该商品编码["+IUID+"]，查无此物!");
                out.println(returnMsg);
                return;
            }
//            ps = db.getConnection().prepareStatement(
//                    "select * from OrderDetailInfo where OrderDetailInfo.IUID=? and OrderDetailInfo.ORDERID=" +
//                    "(select OrderInfo.ORDERID from OrderInfo where UID=?)");
//            ps.setInt(1,IUID);ps.setInt(1,UID);
              // 查询是已经买过了
//            ps = db.getConnection().prepareStatement(
//                    "select * from OrderInfo left join OrderDetailInfo on OrderInfo.ORDERID=OrderDetailInfo.ORDERID " +
//                            "where OrderInfo.UID=? and OrderDetailInfo.IUID=?");
//            ps.setInt(1,UID);ps.setInt(2,IUID);
//            rs = ps.executeQuery();

            returnMsg.put("OWNED",GeneralSQLTool.goodsOwned(db, UID_S, IUID));// 是否已经买过了
            returnMsg.put("ITEM",item);
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","查询商品["+IUID+"]的信息成功!");

        }catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }

    private JSONObject addGoods(OriDB db, Savepoint sp, JSONObject data, FileItem pic) throws Exception{
        JSONObject returnMsg = new JSONObject();
        String INAME = data.getString("INAME");
        String AUTHOR = data.getString("AUTHOR");
        String PUBLISHER = data.getString("PUBLISHER");
        String DESCS = data.getString("DESCS");
        double PRICE = data.getDouble("PRICE");
        int DISCOUNT = data.getInt("DISCOUNT");
        String ISBN = data.getString("ISBN");
        String COVER_PIC = MD5.getMD5(ISBN)+".jpg";
        // System.out.println("文件保存到:" + filePath);// 在控制台输出文件的上传路径

        PreparedStatement ps = db.getConnection().prepareStatement(
                "insert into GoodsInfo(INAME,AUTHOR,PUBLISHER,DESCS,PRICE,DISCOUNT,COVER_PIC,ODATE,ISBN) values(?,?,?,?,?,?,?,?,?)");
        ps.setString(1,INAME);ps.setString(2,AUTHOR);
        ps.setString(3,PUBLISHER);ps.setString(4,DESCS);
        ps.setDouble(5,PRICE);ps.setInt(6,DISCOUNT);
        ps.setString(7,COVER_PIC);ps.setString(8,df.format(new Date()).toString());
        ps.setString(9,ISBN);

        String filePath = GloableValue.UploadImgPath + File.separator + COVER_PIC;
        pic.write(new File(filePath));// 保存文件到硬盘
        if(ps.executeUpdate()>0){
            db.commit();
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","添加商品成功!");
        } else {
            db.rollback(sp);
            boolean deleted = new File(filePath).delete();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","添加商品失败!");
        }
        return returnMsg;
    }

    private JSONObject modifyGoods(OriDB db, Savepoint sp, JSONObject data, FileItem pic) throws Exception{
        JSONObject returnMsg = new JSONObject();
        int IUID = data.getInt("IUID");
        String INAME = data.getString("INAME");
        String AUTHOR = data.getString("AUTHOR");
        String PUBLISHER = data.getString("PUBLISHER");
        String DESCS = data.getString("DESCS");
        double PRICE = data.getDouble("PRICE");
        int DISCOUNT = data.getInt("DISCOUNT");
        boolean ONSALE = data.getBoolean("ONSALE");
        String ISBN = data.getString("ISBN");
        String COVER_PIC = MD5.getMD5(ISBN)+".jpg";

        PreparedStatement ps = db.getConnection().prepareStatement(
                "update GoodsInfo set INAME=?,AUTHOR=?,PUBLISHER=?,DESCS=?,PRICE=?,DISCOUNT=?,ONSALE=?,ISBN=? where IUID=?");
        ps.setString(1,INAME);ps.setString(2,AUTHOR);ps.setString(3,PUBLISHER);
        ps.setString(4,DESCS);ps.setDouble(5,PRICE);ps.setInt(6,DISCOUNT);
        ps.setBoolean(7,ONSALE);//ps.setString(8,df.format(new Date()));
        ps.setString(8,ISBN);ps.setInt(9,IUID);
        String filePath = GloableValue.UploadImgPath + File.separator + COVER_PIC;

        if(ps.executeUpdate()>0){
            if(pic!=null) pic.write(new File(filePath));// 保存文件到硬盘
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","商品["+IUID+"]的信息更新成功!");
            db.commit();
        } else{
            db.rollback(sp);
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","商品["+IUID+"]的信息更新失败!");
        }
        return returnMsg;
    }

    private JSONObject removeGoods(OriDB db, Savepoint sp, JSONObject data) throws Exception{
        JSONObject returnMsg = new JSONObject();
        int IUID = data.getInt("IUID");
        JSONObject GoodsBean = GeneralSQLTool.findGoodsInfo(db, IUID);
        String ISBN = GoodsBean.getString("ISBN");
        PreparedStatement ps = db.getConnection().prepareStatement("delete from GoodsInfo where IUID=?");
        ps.setInt(1,IUID);
        if(ps.executeUpdate()>0){
            db.commit();
            String filePath = GloableValue.UploadImgPath + File.separator + MD5.getMD5(ISBN)+".jpg";
            boolean deleted = new File(filePath).delete();
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","商品["+IUID+"]的信息移除成功!");
        } else {
            db.rollback(sp);
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","商品["+IUID+"]的信息移除失败!");
        }
        return returnMsg;
    }

}
