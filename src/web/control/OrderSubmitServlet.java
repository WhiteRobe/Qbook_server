package web.control;

import org.json.JSONArray;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@WebServlet(name =
        "OrderSubmitServlet ",
        urlPatterns={"/OrderSubmitServlet"}
)
public class OrderSubmitServlet extends HttpServlet {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        OriDB db = OriDBManager.produceOriDB(); // 获取一个新的db
        Savepoint sp = null; // 设置回滚点
        try {
            assert db != null;
            db.beginTransaction(); // 开始一个新事务
            sp = db.getConnection().setSavepoint();
        } catch (SQLException e) {
            // returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误:01!");
            returnMsg.put("ErrorDetail",Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            out.println(returnMsg);
            return;
        }
        try{
            String jsondata = request.getParameter("jsondata");

            JSONObject jsonObject = new JSONObject(jsondata);
            int UID = jsonObject.getInt("UID"); // UID统一在服务器用String记录
            int UID_S = (int) session.getAttribute("UID");
            if(UID!=UID_S || UID_S==-1){ // 检查用户登陆状态，确保是有效用户
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                }
                out.println(returnMsg);
                db.exit(); // 释放资源 提前结束
                return ;
            }

            String datenow = df.format(new Date()).toString();
            int ORDERID = -1;
            // 创建一个单号
            PreparedStatement ps = db.getConnection().prepareStatement("insert into OrderInfo(UID,TOTAL_PRICE,ODATE,OSTATUS) values(?,?,?,?)");
            ps.setInt(1,UID_S);
            ps.setDouble(2,0.0); // 暂时设为0
            ps.setString(3,datenow);
            ps.setString(4,"完成"); // 支付逻辑未完成 此处默认未订单-"完成"
            ps.executeUpdate();
            // 查询刚刚创建好的订单的单号
            ps = db.getConnection().prepareStatement("select * from OrderInfo where UID=? and ODATE=?");
            ps.setInt(1,UID_S);ps.setString(2,datenow);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) ORDERID = rs.getInt(1);
            else { // 若查不到则是订单创建失败
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","订单创建失败!");
                out.println(returnMsg);
                db.rollback(sp); // 回滚
                db.exit(); // 释放资源 提前结束
                return ;
            }

            JSONArray items = jsonObject.getJSONArray("ITEMS");
            JSONArray CART = (JSONArray) session.getAttribute("Cart");
            boolean isAllSuccess = true; // 是否每条购买记录都被正确执行
            int lastIUID = -1;
            double totalPriceCount = 0.0; // 该订单的总价
            for(int i=0;i<items.length();i++){
                JSONObject item = items.getJSONObject(i);
                int IUID = item.getInt("IUID");
                JSONObject serverCartItem = serverFindItem(CART,IUID);
                if(serverCartItem==null){
                    lastIUID = IUID;
                    isAllSuccess = false;
                    break;
                }
                // double PRICE = item.getDouble("PRICE");
                // int DISCOUNT = item.getInt("DISCOUNT");
                double PRICE=-1;
                int DISCOUNT=-1;
                int AMOUNT = serverCartItem.getInt("AMOUNT");
                ps = db.getConnection().prepareStatement("select PRICE,DISCOUNT from GoodsInfo where IUID=?");ps.setDouble(1,IUID);
                ResultSet rsi = ps.executeQuery();
                if(rsi.next()){
                    PRICE=rsi.getDouble(1);
                    DISCOUNT=rsi.getInt(2);
                } else {
                    isAllSuccess = false;
                    break;
                }
                // 增加购买记录 : 允许重复购买
                ps = db.getConnection().prepareStatement("insert into OrderDetailInfo(ORDERID,IUID,PRICE,DISCOUNT,AMOUNT) values(?,?,?,?,?)");
                ps.setInt(1,ORDERID);ps.setInt(2,IUID);
                ps.setDouble(3,PRICE);ps.setInt(4,DISCOUNT);
                ps.setInt(5,AMOUNT);
                lastIUID = IUID;
                if(ps.executeUpdate()<=0 || PRICE < 0 || DISCOUNT < 0){
                    isAllSuccess = false;
                    break;
                } else {
                    totalPriceCount += PRICE*(1-DISCOUNT/100.0);
                }
                // 增加书的销量
                ps = db.getConnection().prepareStatement("update GoodsInfo set SALED=SALED+1 where IUID=?");
                ps.setInt(1,IUID);
                if(ps.executeUpdate()<=0){
                    isAllSuccess = false;
                    break;
                }
            }
            if(isAllSuccess){
                // 更新订单总价
                ps = db.getConnection().prepareStatement("update OrderInfo set TOTAL_PRICE=? where ORDERID=?");
                ps.setDouble(1,totalPriceCount);ps.setInt(2,ORDERID);
                if(ps.executeUpdate()>0){
                    session.setAttribute("Cart",new JSONArray()); // 重置购物车内容
                    returnMsg.put("FLAG",true);
                    returnMsg.put("MSG","订单提交成功!");
                    db.commit(); // 提交事务
                } else{
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","更新订单总价失败，请重试！");
                    returnMsg.put("ErrorDetail","更新订单总价["+totalPriceCount+"]失败。");
                    db.rollback(sp);
                }
            } else {
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","部分订单内容提交失败，请重试！");
                returnMsg.put("ErrorDetail","错误的商品代码为[ "+lastIUID+" ]。");
                db.rollback(sp); // 撤销事务
            }
        }catch (Exception e){
            db.rollback(sp);// 回滚事务
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误:02!");
            returnMsg.put("ErrorDetail",Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }finally {
            db.exit();// 释放服务器资源
        }
        out.println(returnMsg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private JSONObject serverFindItem(JSONArray CART,int IUID){
        for(int i=0;i<CART.length();i++){
            JSONObject item = CART.getJSONObject(i);
            int IUID_S = item.getInt("IUID");
            if(IUID == IUID_S) return item; // { IUID:.., AMOUNT:... } @ CartManagerServlet.java Line:150
        }
        return null;
    }
}
