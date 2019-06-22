package web.control;

import org.json.JSONArray;
import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.tool.BeanSealer;

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
import java.util.Arrays;

@WebServlet(
        name = "GetOrderListDetailServlet",
        urlPatterns = "/GetOrderListDetailServlet"
)
public class GetOrderListDetailServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            JSONObject jsonObject = new JSONObject(jsondata);
            OriDB db = OriDBManager.getSingletonOriDB();
            int UID = jsonObject.getInt("UID"); // 获取用户UID
            int UID_S = (int) session.getAttribute("UID");
            if(UID!=UID_S || UID_S==-1){ // 检查用户登陆状态，确保是有效用户
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                    //System.out.println(UID+" "+UID_S);
                }
                out.println(returnMsg);
                return ;
            }
            int ORDERID = (int)jsonObject.getInt("ORDERID");
            PreparedStatement ps = db.getConnection().prepareStatement("select * from OrderInfo where ORDERID=? and UID=?");
            ps.setInt(1,ORDERID);ps.setInt(2,UID_S);
            ResultSet rs = ps.executeQuery();
            JSONObject ORDER = new JSONObject();
            if(rs.next()){
//                ORDER.put("ORDERID",rs.getInt(1));
//                ORDER.put("UID",rs.getInt(2));
//                ORDER.put("OSTATUS",rs.getString(3));
//                ORDER.put("TOTAL_PRICE",rs.getDouble(4));
//                ORDER.put("ODATE",rs.getString(5));
                ORDER = new BeanSealer().OrderInfoBean(rs);
            } else {
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","订单["+ORDERID+"]查询错误!");
                out.println(returnMsg);
                return;
            }
            ps = db.getConnection().prepareStatement("select * from OrderDetailInfo where ORDERID=?");
            PreparedStatement ps2 = db.getConnection().prepareStatement("select * from GoodsInfo where IUID=?");
            ps.setInt(1,ORDERID);
            rs = ps.executeQuery();
            JSONArray ITEMS = new JSONArray();
            while(rs.next()){
                JSONObject orderDetailBean = new BeanSealer().OrderDetailInfoBean(rs);
                int IUID = orderDetailBean.getInt("IUID");
                double PRICE = orderDetailBean.getDouble("PRICE");
                int DISCOUNT = orderDetailBean.getInt("DISCOUNT");
                int AMOUNT = orderDetailBean.getInt("AMOUNT");

                ps2.setInt(1,IUID);
                ResultSet rs2 = ps2.executeQuery();
                JSONObject item = new JSONObject();
                if(rs2.next()){
                    item = new BeanSealer().GoodsInfoBean(rs2);
                    item.remove("PRICE");item.put("PRICE",PRICE);
                    item.remove("DISCOUNT");item.put("DISCOUNT",DISCOUNT);
                    /*item.remove("AMOUNT");*/item.put("AMOUNT",AMOUNT);
                    item.remove("DESCS");// 节约流量
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","订单["+ORDERID+"]的商品["+IUID+"]查询错误!");
                    out.println(returnMsg);
                    return;
                }
                ITEMS.put(item);
            }
            returnMsg.put("ITEMS",ITEMS);
            returnMsg.put("ORDER",ORDER);
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","订单["+ORDERID+"]查询成功!");
        } catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail", Arrays.toString(e.getStackTrace()));
        }
        out.println(returnMsg);
    }
}
