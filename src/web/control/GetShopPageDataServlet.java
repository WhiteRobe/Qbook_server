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
        name = "GetShopPageDataServlet",
        urlPatterns = "/GetShopPageDataServlet"
)
public class GetShopPageDataServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        // HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            OriDB db = OriDBManager.getSingletonOriDB();
            BeanSealer sealer = new BeanSealer();
            // 热门物品
            ResultSet rs = db.preStaQuery("select * from GoodsInfo where ONSALE=true order by SALED desc");
            JSONArray hotItems = new JSONArray();
            for(int i=0;i<6&&rs.next();i++){
                JSONObject item = sealer.GoodsInfoBean(rs);
                hotItems.put(item);
            }
            returnMsg.put("HOTITEMS",hotItems);
            // 最新物品
            rs = db.preStaQuery("select * from GoodsInfo where ONSALE=true order by ODATE desc");
            JSONArray newItems = new JSONArray();
            for(int i=0;i<6&&rs.next();i++){
                JSONObject item = sealer.GoodsInfoBean(rs);
                newItems.put(item);
            }
            returnMsg.put("NEWITEMS",newItems);
            // 折扣物品
            rs = db.preStaQuery("select * from GoodsInfo where ONSALE=true and DISCOUNT>0 order by DISCOUNT desc");
            JSONArray saleItems = new JSONArray();
            for(int i=0;i<6&&rs.next();i++){
                JSONObject item = sealer.GoodsInfoBean(rs);
                saleItems.put(item);
            }
            returnMsg.put("SALEITEMS",saleItems);

            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","数据已返回!");
        } catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }

}
