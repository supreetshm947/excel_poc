import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import model.Country;
import model.Employee;
import model.SDTable;

public class Main {
	public static void main(String s[]){
		Configuration configuration = new Configuration().configure();
		StandardServiceRegistryBuilder standardServiceRegisrtyBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
		ServiceRegistry serviceRegistry = standardServiceRegisrtyBuilder.build();
		
		SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		Session session = sessionFactory.openSession();
		
		//SessionImplementor sessionImplementor = (SessionImplementor) session;
		try{
			//Connection conn = sessionImplementor.getJdbcConnectionAccess().obtainConnection();
			/*Transaction t = session.beginTransaction();
			SDTable sd1 = new SDTable();
			sd1.setTablePrimaryKeyColumn("ID");
			sd1.setTableModelPackage("model.Country");
			sd1.setTableName("Country");
			sd1.setTableId(1L);
			SDTable sd2 = new SDTable();
			sd2.setTablePrimaryKeyColumn("ID,COMP_ID");
			sd2.setTableModelPackage("model.Employee");
			sd2.setTableName("Employee");
			sd2.setTableId(2L);
			Employee e = new Employee();
			e.setId(22);
			e.setCompany("INFY");
			e.setName("RAM");
			Country country = new Country();
			country.setId(new Double(34343));
			session.save(country);
			session.save(sd1);
			session.save(sd2);
			//session.save(e);
		
			//session.save(country);
			t.commit();
		*/
			SDTable sd = (SDTable)session.get(SDTable.class, 1L);
			new Validate().validateExcelAndPrepareInsertQueries(sd);
			session.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("done");
	}
}
