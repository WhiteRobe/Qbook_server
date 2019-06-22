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
        name = "CartManagerServlet",
        urlPatterns = "/CartManagerServlet"
)
public class CartManagerServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 用于修改购物车
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            JSONArray CART = (JSONArray) session.getAttribute("Cart");
            String jsondata = request.getParameter("jsondata");
            //OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            int UID = jsonObject.getInt("UID");
            int UID_S = (int)session.getAttribute("UID");
            if(UID!=UID_S || UID_S==-1){ // 检查用户登陆状态，确保是有效用户
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                }
                out.println(returnMsg);
                return ;
            }
            int IUID = jsonObject.getInt("IUID");
            int AMOUNT = jsonObject.getInt("AMOUNT");
            String METHOD  = jsonObject.getString("METHOD"); // ENUM (ADD REMOVE)
            switch (METHOD){
                case "MODIFY":
                    returnMsg = modifyCart(CART,IUID,AMOUNT);
                    break;
                case "DELETE":
                    returnMsg = deleteFromCart(CART,IUID);
                    break;
                default:
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","未知操作:"+METHOD);
                    break;
            }
        } catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 用于获取购物车信息
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JSONObject returnMsg = new JSONObject();
        try{
            JSONArray CART = (JSONArray) session.getAttribute("Cart");
            String jsondata = request.getParameter("jsondata");
            OriDB db = OriDBManager.getSingletonOriDB();
            JSONObject jsonObject = new JSONObject(jsondata);
            int UID = jsonObject.getInt("UID");
            int UID_S = (int)session.getAttribute("UID");
            if(UID!=UID_S || UID_S==-1){ // 检查用户登陆状态，确保是有效用户
                if(UID_S==-1){
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","尚未登陆!");
                } else {
                    returnMsg.put("FLAG",false);
                    returnMsg.put("MSG","登陆状态错误!请刷新页面后重试！");
                }
                out.println(returnMsg);
                return ;
            }

            PreparedStatement ps = db.getConnection().prepareStatement("select * from GoodsInfo where IUID=?");
            boolean isAllSuccess = true;
            int firstFaliedUID = -1;
            JSONArray ITEMS = new JSONArray();
            for(int i=0;i<CART.length();i++){
                JSONObject item = CART.getJSONObject(i);
                int IUID = item.getInt("IUID");
                ps.setInt(1,IUID);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    ITEMS.put(new BeanSealer().GoodsInfoBean(rs));
                } else {
                    isAllSuccess = false;
                    firstFaliedUID = IUID;
                    break;
                }
            }

            if(isAllSuccess){
                returnMsg.put("ITEMS",ITEMS);
                returnMsg.put("FLAG",true);
                returnMsg.put("MSG","购物车内容查询成功!");
            } else {
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","购物车商品号为["+firstFaliedUID+"]的商品查询失败!");
            }
        }catch (Exception e){
            returnMsg = new JSONObject();
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","服务器错误!");
            returnMsg.put("ErrorDetail", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        out.println(returnMsg);
    }

    private JSONObject modifyCart(JSONArray CART, int IUID, int AMOUNT) throws Exception {
        // 修改购物车中的商品
        JSONObject returnMsg = new JSONObject();
        boolean notContained = true;
        for(int i=0;i<CART.length();i++){
            JSONObject item = CART.getJSONObject(i);
            if(IUID==item.getInt("IUID")){
                notContained = false;
                // 已存在则进行修改
                if(item.getInt("AMOUNT")+AMOUNT<=0){
                    return deleteFromCart(CART, IUID); // 移除商品
                } else item.put("AMOUNT",item.getInt("AMOUNT")+AMOUNT);
                break;
            }
        }
        if(notContained){
            if(AMOUNT>0){
                // 不存在则添加新商品
                JSONObject newItem = new JSONObject();
                newItem.put("IUID",IUID);
                newItem.put("AMOUNT",AMOUNT);
                CART.put(newItem);
                // 设置标记位
                returnMsg.put("CONTAINED",false);
            } else{
                returnMsg.put("FLAG",false);
                returnMsg.put("MSG","不能添加商品数量为负数或为零的商品！");
                return returnMsg;
            }
        } else {
            // 已经存在
            returnMsg.put("CONTAINED",true);
        }
        returnMsg.put("FLAG",true);
        returnMsg.put("MSG","购物车信息已修改!数量变化: "+AMOUNT);
        return returnMsg;
    }

    private JSONObject deleteFromCart( JSONArray CART, int IUID) throws Exception {
        // 从购物车中删除一个项目
        JSONObject returnMsg = new JSONObject();
        boolean notContained = true;
        int removedIndex = -1;
        // 检查购物车中是否已有该商品
        for(int i=0;i<CART.length();i++){
            JSONObject item = CART.getJSONObject(i);
            if(IUID==item.getInt("IUID")){
                // 找到并商品在CART的序号
                notContained = false;
                removedIndex = i;
                break;
            }
        }
        if(notContained){
            // 不存在
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","商品不存在!");
            returnMsg.put("CONTAINED",false);
        } else {
            // 已经存在则进行移除
            CART.remove(removedIndex);
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","移除商品成功!");
            returnMsg.put("CONTAINED",true);
        }
        return returnMsg;
    }

}
