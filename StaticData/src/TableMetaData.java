import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.service.ServiceRegistry;

public class TableMetaData {
	
	static Map<String, Map<String, Columns>> sdTableMetaData = null;
	
	public static Map<String, Columns> getTableMetaData(String tableName) {
		if(sdTableMetaData == null) {
			loadSdMetaData();
		}
		return sdTableMetaData.get(tableName);
	}
	
	public static void invalidateSdMetaData() {
		sdTableMetaData = null;
	}

	public static void loadSdMetaData() {
		if(sdTableMetaData != null) {
			return;
		}
		Configuration configuration = new Configuration().configure();
		StandardServiceRegistryBuilder standardServiceRegisrtyBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
		ServiceRegistry serviceRegistry = standardServiceRegisrtyBuilder.build();
		
		SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		Session session = sessionFactory.openSession();
		
		Connection conn = null;
		sdTableMetaData = new HashMap<String, Map<String, Columns>>();
		PreparedStatement psForAllSDTables = null;
		try {
			
			SessionImplementor sessionImplementor = (SessionImplementor) session;
			conn = sessionImplementor.getJdbcConnectionAccess().obtainConnection();
			psForAllSDTables = conn.prepareStatement("select * from SDTable");
			ResultSet allSDTables = psForAllSDTables.executeQuery();
			while (allSDTables.next()) {
				String sdTableName = allSDTables.getString("TABLE_NAME");
				PreparedStatement ps  = null;
				ResultSet rs = null;
				try {
					
					Map<String, Columns> columns = new HashMap<String, Columns>();
					ps = conn.prepareStatement("select * from " + sdTableName + " where 1=2");
					rs = ps.executeQuery();
					ResultSetMetaData rsmd = rs.getMetaData();
					for (int j = 1; j <= rsmd.getColumnCount(); j++) {
						Columns column = new Columns();
						column.setColumnName(rsmd.getColumnName(j));
						if (rsmd.isNullable(j) == 1) {
							column.setColumnNullable(true);
						}
						column.setColumnSize(rsmd.getColumnDisplaySize(j));
						column.setColumnType(rsmd.getColumnTypeName(j));
						column.setScale(rsmd.getScale(j) > 0 ? rsmd.getScale(j) : 0);
						PreparedStatement psForDefaultValue = null;
						ResultSet rsForDefaultValue = null;
						try {
							/*psForDefaultValue = conn.prepareStatement("select DATA_DEFAULT from ALL_TAB_COLS where TABLE_NAME = ? and COLUMN_NAME = ?");
							psForDefaultValue.setString(1, sdTableName);
							psForDefaultValue.setString(2, column.getColumnName());
							rsForDefaultValue = psForDefaultValue.executeQuery();*/
							//while (rsForDefaultValue.next()) {
								column.setDefaultValue(null);
							//}
							columns.put(column.getColumnName(), column);
						} finally {
							if(rsForDefaultValue != null) {
								rsForDefaultValue.close();
							}
							if(psForDefaultValue != null) {
								psForDefaultValue.close();
							}
						}
					}
					sdTableMetaData.put(sdTableName, columns);
				
				} catch (SQLException se) {
					System.out.println("Table Not found " + sdTableName+"--"+ se);
				} finally {
					if(rs != null) {
						rs.close();
					}
					if(ps != null) {
						ps.close();
					}
				}
			}
		} catch (SQLException se) {
			System.out.println(se);
		} finally {
			try {
				if(psForAllSDTables != null) {
					psForAllSDTables.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				System.out.println(("SQLException in loading SdMetaData: "+ se));
			}
		}
		System.out.println("Finished loading metadata for SD Tables");
	}

}
