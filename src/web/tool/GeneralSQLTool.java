package web.tool;

import org.json.JSONObject;
import web.database.dao.OriDB;
import web.gloable.GloableValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class GeneralSQLTool {
    public static int getAUTH(OriDB db,int UID) throws SQLException {
        int AUTH = -1;
        PreparedStatement ps = db.getConnection().prepareStatement("select AUTH from UserInfo where UID=?");
        ps.setInt(1,UID);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            AUTH = rs.getInt(1);
        } else {
            throw new SQLException("无查询结果");
        }
        return AUTH;
    }

    public static boolean goodsOwned(OriDB db, int UID, int IUID) throws SQLException {
        PreparedStatement ps = db.getConnection().prepareStatement(
                "select * from OrderInfo left join OrderDetailInfo on OrderInfo.ORDERID=OrderDetailInfo.ORDERID " +
                        "where OrderInfo.UID=? and OrderDetailInfo.IUID=?");
        ps.setInt(1,UID);ps.setInt(2,IUID);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public static boolean checkLogin(OriDB db, String ID, String PW) throws SQLException{
        ResultSet rs = db.preStaQuery("select * from UserInfo where ID=? and PW=?",
                new String[]{ID, MD5.getMD5(PW, GloableValue.MD5Salt)});
        return rs.next();
    }

    public static JSONObject findUserInfo(OriDB db, String ID, String PW) throws SQLException{
        ResultSet rs = db.preStaQuery("select * from UserInfo where ID=? and PW=?",
                new String[]{ID, MD5.getMD5(PW, GloableValue.MD5Salt)});
        if(rs.next())return new BeanSealer().UserInfoBean(rs);
        else throw new SQLException("未找到用户信息");
    }

    public static JSONObject findGoodsInfo(OriDB db, int IUID) throws SQLException{
        PreparedStatement ps = db.getConnection().prepareStatement("select * from GoodsInfo where IUID=?");
        ps.setInt(1,IUID);
        ResultSet rs = ps.executeQuery();
        if(rs.next())return new BeanSealer().GoodsInfoBean(rs);
        else throw new SQLException("未找到商品信息");
    }
}
