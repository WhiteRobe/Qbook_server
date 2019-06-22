package web.database.dao;

import java.sql.*;

public final class OriDB {
	private Connection con = null;

	void setConnection(Connection connection){
		this.con=connection;
	}

	public Connection getConnection(){
		return this.con;
	}

	public Statement getNewStatement(){
		try {
			return con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 数据库事务操作方法
	 */
	public void beginTransaction(){
		try{
			this.con.setAutoCommit(false);
		}catch (Exception e){
			e.printStackTrace();
		}

	}
	public void commit(){
		try{
			this.con.commit();
			this.con.setAutoCommit(true);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void rollback(Savepoint sp){
		try{
			this.con.rollback(sp);
			this.con.setAutoCommit(true);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void rollback(){
		try{
			this.con.rollback();
			this.con.setAutoCommit(true);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public boolean getAutoCommit(){
		try{
			return this.con.getAutoCommit();
		}catch (Exception e){
			return false;
		}
	}

	/**
	 * 安全的快捷Sql语句执行
	 * @param sqlStr Sql语句
	 * @param param 参数
	 * @return 结果集
	 */
	public ResultSet preStaQuery(String sqlStr,String[] param){
		ResultSet result=null;
		try {
			PreparedStatement ps = con.prepareStatement(sqlStr);
			for(int i=0;i<param.length;i++){
				ps.setString(i+1,param[i]);
			}
			result = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public boolean preStaExec(String sqlStr,String[] param){
		try{
			PreparedStatement ps = con.prepareStatement(sqlStr);
			for(int i=0;i<param.length;i++){
				ps.setString(i+1,param[i]);
			}
			ps.executeUpdate();
		}catch (SQLException e){
			e.printStackTrace();
			return  false;
		}
		return true;
	}
	/**
	 * 较为安全的快捷Sql语句执行
	 * @param sqlStr Sql语句
	 * @return 结果集
	 */
	public ResultSet preStaQuery(String sqlStr){
		ResultSet result=null;
		try {
			result=con.prepareStatement(sqlStr).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public boolean preStaExec(String sqlStr){
		try{
			PreparedStatement ps = con.prepareStatement(sqlStr);
			ps.executeUpdate();
		}catch (SQLException e){
			e.printStackTrace();
			return  false;
		}
		return true;
	}

	/**
	 * 不安全的快捷Sql语句执行
	 * @param sqlStr Sql语句
	 * @return 结果集
	 */
	public ResultSet query(String sqlStr){
		ResultSet result=null;
		try {
			result=getNewStatement().executeQuery(sqlStr);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public boolean exec(String sqlStr){
		try {
			getNewStatement().execute(sqlStr);
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * 断开数据库链接
	 * @return 返回操作是否成功
	 */
	public boolean exit(){
		try {
			getConnection().close();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
}
