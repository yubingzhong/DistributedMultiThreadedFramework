//$Id: JDBCCracker.java 5927 2013-01-11 03:55:12Z ChristopherSmith $
package distributedMultiThreadedFramework.PasswordCracking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * given JDBC info, try to break into database, possibly executing a query
 * 
 * @author smitc
 * @author olivb
 */
public class JDBCCracker extends CrackExecutor{

	String connectionURL = null;
	String user = null;
	String driverClass = null;
	String statement = null;

	
	/**
	 * 
	 * @param connectionURL: full JDBC url
	 * @param user: user to run password cracks against
	 * @param driverClass: class name of driver
	 * @param statement: a possible statement to execute once connected
	 */
	public JDBCCracker(String connectionURL, String user, String driverClass, String statement) {
		this.connectionURL = connectionURL;
		this.user = user;
		this.driverClass = driverClass;
		this.statement = statement;
	}

	
	/**
	 * runs the crack jobs on the URL
	 * 
	 * @param password: current password guess
	 * @return: true if password was found, false if password not found, 
	 * 	       OR if statement was set, true if statement executes, false if it fails
	 */
	@Override
	public boolean crack(String password) {

		Connection connection = null;
		try {

			// Load the Driver class.
			Class.forName(this.driverClass);
			
			//try to create a connection
			connection = DriverManager.getConnection(this.connectionURL, this.user, password);

			if (connection.isClosed())
				return false;

			//if statement is blank/null, but the connection isn't closed (above), made sucessfull connection
			if (this.statement == null || this.statement.trim().length() == 0)
				return true;

			// Create a Statement class to execute the SQL statement
			Statement sqlStatement = connection.createStatement();

			// Execute the SQL statement and get the results in a Resultset
			ResultSet resultSet = sqlStatement.executeQuery(this.statement);

			// Iterate through the ResultSet, displaying two values for each row using the getString method
			int numberOfColumns = resultSet.getMetaData().getColumnCount();

			for (int i = 1; i <= numberOfColumns; i++) {

				if (i > 1)
					System.out.print(",  ");
				String columnName = resultSet.getMetaData().getColumnName(i);

				System.out.print(columnName);
			}
			
			System.out.println("");

			while (resultSet.next()) {
				for (int i = 1; i <= numberOfColumns; i++) {
					if (i > 1)
						System.out.print(",  ");

					String columnValue = resultSet.getString(i);
					System.out.print(columnValue);
				}
				System.out.println("");
			}

		} catch (SQLException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NullPointerException e){
				//this is hit when the password fails. it is an intentional failure
			} catch (Exception e){
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 * nothing necessary to reset
	 */
	@Override
	public void reset() {
		
	}

}
