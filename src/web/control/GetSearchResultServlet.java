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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

@WebServlet(
        name = "GetSearchResultServlet",
        urlPatterns = "/GetSearchResultServlet"
)
public class GetSearchResultServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        // HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            String jsondata = request.getParameter("jsondata");
            OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            String KEY = jsonObject.getString("KEY");
            String FILTER = jsonObject.getString("FILTER");
            String TYPE = jsonObject.getString("TYPE");
            String ORDER = jsonObject.getString("ORDER");
            int PAGE = jsonObject.getInt("PAGE");

            String sql = "select * from GoodsInfo where "+ getTypeFactor(TYPE)+" like ? and ONSALE=true " +
                            " order by "+getFilterFactor(FILTER)+" "+(ORDER.equals("desc")?"desc":"asc");

            PreparedStatement ps = db.getConnection().prepareStatement(sql);
            ps.setString(1,"%"+KEY+"%");
            ResultSet rs = ps.executeQuery();
            //System.out.println(sql);
            JSONArray ITEMS = new JSONArray();
            for(int i=0;i<6*(PAGE+1) && rs.next();i++){
                // 每页最多六个
                if(i<6*PAGE)continue;
                ITEMS.put(new BeanSealer().GoodsInfoBean(rs));
            }
            rs.last(); // 获取总命中数
            returnMsg.put("TOTAL",rs.getRow());
            returnMsg.put("ITEMS",ITEMS);
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","已搜索到数据!");
        } catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }

    /*过滤成适合的因子  防止SQL注入*/
    private String getTypeFactor(String type){
        String f="";
        switch (type){
            case "authorName":
                f = "AUTHOR";
                //sql = "select * from GoodsInfo where AUTHOR like ? and ONSALE=true order by "+getFilterFactor(filter)+" "+(order.equals("desc")?"desc":"asc");
                break;
            case "publiserName":
                f = "PUBLISHER";
                //sql = "select * from GoodsInfo where PUBLISHER like ? and ONSALE=true order by "+getFilterFactor(filter)+" "+(order.equals("desc")?"desc":"asc");
                break;
            case "bookName":
            default:
                f = "INAME";
                //sql = "select * from GoodsInfo where INAME like ? and ONSALE=true order by "+getFilterFactor(filter)+" "+(order.equals("desc")?"desc":"asc");
                break;
        }
        return f;
    }
    /*过滤成适合的因子  防止SQL注入*/
    private String getFilterFactor(String filter){
        String fstr="";
        switch (filter){
            case "time":
                fstr = "ODATE";
                break;
            case "discount":
                fstr = "DISCOUNT";
                break;
            case "hot":
            default:
                fstr = "SALED";
                break;
        }
        return fstr;
    }

}
