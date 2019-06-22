package web.tool;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class BeanSealer {
    public JSONObject UserInfoBean(ResultSet rs) throws SQLException {
        JSONObject item = new JSONObject();
        item.put("UID",rs.getInt(1));
        item.put("ID",rs.getString(2));
        item.put("PW",rs.getString(3));
        item.put("EMAIL",rs.getString(4));
        item.put("AUTH",rs.getInt(5));
        item.put("AVATAR",rs.getString(6));
        return item;
    }

    public JSONObject OrderInfoBean(ResultSet rs) throws SQLException {
        JSONObject item = new JSONObject();
        item.put("ORDERID",rs.getInt(1));
        item.put("UID",rs.getInt(2));
        item.put("OSTATUS",rs.getString(3));
        item.put("TOTAL_PRICE",rs.getDouble(4));
        item.put("ODATE",rs.getString(5));
        return item;
    }

    public JSONObject OrderDetailInfoBean(ResultSet rs) throws SQLException {
        JSONObject item = new JSONObject();
        item.put("ORDERID",rs.getInt(1));
        item.put("IUID",rs.getInt(2));
        item.put("PRICE",rs.getDouble(3));
        item.put("DISCOUNT",rs.getInt(4));
        item.put("AMOUNT",rs.getInt(5));
        return item;
    }

    public JSONObject GoodsInfoBean(ResultSet rs) throws SQLException {
        JSONObject item = new JSONObject();
        item.put("IUID",rs.getInt(1));
        item.put("INAME",rs.getString(2));
        item.put("AUTHOR",rs.getString(3));
        item.put("PUBLISHER",rs.getString(4));
        item.put("DESCS",rs.getString(5));
        item.put("PRICE",rs.getDouble(6));
        item.put("DISCOUNT",rs.getInt(7));
        item.put("COVER_PIC",rs.getString(8));
        item.put("ONSALE",rs.getBoolean(9));
        item.put("SALED",rs.getInt(10));
        item.put("ODATE",rs.getString(11));
        item.put("ISBN",rs.getString(12));
        return item;
    }
}
