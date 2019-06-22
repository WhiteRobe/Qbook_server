package web.control;

import org.json.JSONArray;
import org.json.JSONObject;
import web.database.dao.OriDB;
import web.database.dao.OriDBManager;
import web.gloable.GloableValue;
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

@WebServlet(name = "GetOrderListServlet",
        urlPatterns={"/GetOrderListServlet"}
)
public class GetOrderListServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            JSONObject jsonObject = new JSONObject(jsondata);
            OriDB db = OriDBManager.getSingletonOriDB();
            int PAGE = jsonObject.getInt("PAGE"); // 获取当前页码 从0页起
            int ITEMNUM = jsonObject.getInt("ITEMNUM"); // 获取每页数
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
            PreparedStatement ps = db.getConnection().prepareStatement("select * from OrderInfo where UID=? order by ODATE desc");
            ps.setInt(1,UID_S);
            ResultSet rs = ps.executeQuery();
            int nowItemIndex = 0;
            JSONArray items = new JSONArray();
            while(nowItemIndex < (PAGE+1)* ITEMNUM){
                if(!rs.next()) break; // 查询完毕
                if(nowItemIndex++ < PAGE*ITEMNUM) continue;
                JSONObject item = new BeanSealer().OrderInfoBean(rs);
//                int ORDERID = rs.getInt(1);
//                //int UID = rs.getInt(2);
//                String OSTATUS = rs.getString(3);
//                Double TOTAL_PRICE = rs.getDouble(4);
//                String ODATE = rs.getString(5);
//                item.put("ORDERID",ORDERID);item.put("UID",UID);
//                item.put("OSTATUS",OSTATUS);item.put("TOTAL_PRICE",TOTAL_PRICE);
//                item.put("ODATE",ODATE);
                items.put(item);
            }
            rs.last();
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","用户订单列表数据获取成功，为第["+PAGE+"]页。");
            returnMsg.put("ITEMS",items);
            returnMsg.put("TOTAL",rs.getRow());
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
