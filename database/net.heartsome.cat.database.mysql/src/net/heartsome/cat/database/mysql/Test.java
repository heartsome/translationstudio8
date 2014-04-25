package net.heartsome.cat.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Test {

	public static void main(String arg[]) {

		try {

			Connection conn2 = getConnection();
			System.out.println(conn2);
			String selectSql = "select * from test";
			PreparedStatement pstm2 = conn2.prepareStatement(selectSql);
			ResultSet rs = pstm2.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
			pstm2.close();
			rs.close();

			Connection conn1 = getConnection();
			System.out.println(conn1);
			String insertSql = "insert into test(test) values(?)";
			PreparedStatement pstm = conn1.prepareStatement(insertSql);
			pstm.setString(1, "test12313");
			pstm.executeUpdate();
			conn1.commit();
			pstm.close();
			conn1.close();

			String sql2 = "select * from test where test=?";
			PreparedStatement pstm3 = conn2.prepareStatement(sql2);
			pstm3.setString(1, "test12313");
			ResultSet rs1 = pstm3.executeQuery();
			while (rs1.next()) {
				System.out.println("\n\n  ==" + rs1.getString(1));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String url = "jdbc:mysql://localhost:3306/sample_db?user=root&password=root";
		try {
			Connection conn = DriverManager.getConnection(url);
			conn.setAutoCommit(false);
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
