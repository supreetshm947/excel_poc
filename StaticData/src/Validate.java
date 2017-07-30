import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import model.SDTable;

public class Validate {

	private static final String KEY_FOR_COLUMN_IS_MISSING_OR_INCORRECT = "Key for column is missing or incorrect";

	private static final String SQL_ERROR = "SQL error";

	private static final String NOT_NULLABLE_COLUMN_ERROR = "Not nullable columns should have values";

	private static final String DATA_LENGTH_ISSUE = "Data length issue";

	private static final String FILE_VALIDATIONS_FAILED = "The file failed in validations.";

	private static final String NULL_FILE_ERROR = "The uploaded file is empty.";

	private static final String DATA_TYPE_INVALID = "Data type for the column is invalid";

	private static final String greenHex = "FF00B050", redHex = "FFFF0000", yellowHex = "FFFFFF00";

	private Map<Integer, FileError> errors = new HashMap<>();
	private List<String> columnNamesInFile = new ArrayList<String>();
	private int noOfColumns = 0;

	/*
	 * private void createErrorFile(SdUploadDraft draft) throws WFException {
	 * int rowNumber; LOGGER.error("Errors present in file : "); for (Integer
	 * row : errors.keySet()) { rowNumber = row + 1; LOGGER.error("Row number "
	 * + rowNumber + " \n" + errors.get(row).toString()); } String
	 * outputFileName =
	 * draft.getFileName().substring(draft.getFileName().lastIndexOf("/") + 1,
	 * draft.getFileName().length()); outputFileName =
	 * outputFileName.substring(0, outputFileName.lastIndexOf(".")) + "_Error" +
	 * outputFileName.substring(outputFileName.lastIndexOf("."),
	 * outputFileName.length()); draft.setErrorFileName(outputFileName);
	 * writeErrorFile(
	 * ApplicationCache.getApplicationPropertyMap().get(ApplicationConstants.
	 * SD_FILE_UPLOAD_PATH) + draft.getFileName(),
	 * ApplicationCache.getApplicationPropertyMap().get(ApplicationConstants.
	 * SD_FILE_UPLOAD_PATH), outputFileName, errors); }
	 */

	/*
	 * private boolean executeQueries(List<Query> queries) { int i = 0; for
	 * (Query query : queries) { try { query.executeUpdate(); } catch
	 * (ConstraintViolationException e1) { System.out.println(e1); FileError fe
	 * = new FileError(); fe.setRowErrors(0,
	 * KEY_FOR_COLUMN_IS_MISSING_OR_INCORRECT + " (" + e1.getConstraintName() +
	 * ")"); errors.put(i, fe); } catch (HibernateException e) {
	 * System.out.println(e); FileError fe = new FileError(); fe.setRowErrors(0,
	 * SQL_ERROR); errors.put(i, fe); } finally { i++; } } if (errors.isEmpty())
	 * { return true; } return false; }
	 */

	/**
	 * 
	 * @param iPersistanceWrapper
	 * @param fileName
	 * @param sdVersion
	 * @param sdTable
	 * @return
	 * @throws PersistanceException
	 * @throws WFException
	 */
	/* Need to Refactor this method once working proof is established */
	public boolean validateExcelAndPrepareInsertQueries(
			SDTable sdTable) throws Exception {
		List<Query> queries = new LinkedList<Query>();
		noOfColumns = 0;
		XSSFWorkbook workbook = null;
		try {
			Class<?> modelClass = Class.forName(sdTable.getTableModelPackage());
			Object modelObject = modelClass.newInstance();
			Field[] fields = (modelObject.getClass()).getDeclaredFields();
			List<Object> insertObjectList = new ArrayList<Object>();
			List<Object> modifyObjectList = new ArrayList<Object>();
			List<Query> modifyQueryList = new ArrayList<Query>();
			List<Object> deleteObjectList = new ArrayList<Object>();
			List<Query> deleteQueryList = new ArrayList<Query>();
			try {
				workbook = new XSSFWorkbook(new FileInputStream("in/in.xlsx"));
			} catch (FileNotFoundException e) {
				System.out.println(e);
				throw e;
			} catch (IOException e) {
				System.out.println(e);
				throw e;
			}
			
			//Conn open n Shit
			Configuration configuration = new Configuration().configure();
			StandardServiceRegistryBuilder standardServiceRegisrtyBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
			ServiceRegistry serviceRegistry = standardServiceRegisrtyBuilder.build();
			
			SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
			Session session = sessionFactory.openSession();
			Transaction tran = session.beginTransaction();
			//Conn open n Shit

			XSSFSheet worksheet = workbook.getSheetAt(0);

			XSSFRow firstRow = worksheet.getRow(0);

			if (firstRow == null) {
				FileError error = new FileError();
				error.setRowErrors(0, NULL_FILE_ERROR);
				errors.put(0, error);
				return false;
			}

			noOfColumns = firstRow.getPhysicalNumberOfCells();
			Map<String, Columns> columns = TableMetaData.getTableMetaData(sdTable.getTableName());
			XSSFRow row;
			if (!validateColumnHeader(firstRow, columns)) {
				return false;
			}

			/*String backupTableQuery = ApplicationConstants.SD_BACKUP_QUERY;
			backupTableQuery = backupTableQuery.replace(ApplicationConstants.SD_BACKUP_TABLE,
					sdTable.getBackupTableName());
			backupTableQuery = backupTableQuery.replace(ApplicationConstants.SD_TABLE, sdTable.getTableName());
			Query backupQuery = ((GenericHibernatePersistanceWrapper) iPersistanceWrapper)
					.createSQLQuery(backupTableQuery);
			backupQuery.setParameter(0, sdVersion);
			backupQuery.executeUpdate();*/

			int noOfColorColumns = 0;
			Map<Integer, ArrayList<Integer>> indexOfUpdatedColumns = new HashMap<Integer, ArrayList<Integer>>();
			//Map<Integer, Integer> primaryKeyUpdatedColumnValues = new HashMap<Integer, Integer>();
			Map<Integer, Map<Integer, Integer>> primaryKeyUpdatedColumnValues = new HashMap<Integer, Map<Integer, Integer>>();
			Map<Integer, Map<Integer, Integer>> primaryKeyDeletedColumnValues = new HashMap<Integer, Map<Integer, Integer>>();
			ArrayList<Integer> indexOfColorColumn = null;
			List<Integer> primaryKeyColumnNumbers = getPrimaryKeyColumnNumbers(firstRow, sdTable);

			for (int i = 1; i <= worksheet.getLastRowNum(); i++) {
				row = worksheet.getRow(i);

				/* If row is empty proceed to the next row */
				if (row == null) {
					System.out.println("Row is empty " + i);
					FileError fe = new FileError();
					fe.setRowErrors(0, "Empty Row");
					errors.put(i, fe);
					continue;
				}
				FileError error = new FileError();
				//the whole row will be highlighted so take first column
				//XSSFColor color = row.getCell(primaryKeyColumnNumber).getCellStyle().getFillForegroundColorColor();
				
				XSSFColor color = row.getCell(primaryKeyColumnNumbers.get(0)).getCellStyle().getFillForegroundColorColor();
				
				// Don't Traverse if the column is not highlighted
				if (color != null) {
					String colorHex = color.getARGBHex();
					// Prepare Inserts when Color is Green
					if (greenHex.equals(colorHex)) {
						System.out.println("Row number need to be inserted : " + i);
						Object fileObject = modelClass.newInstance();
						row : for (int j = 0; j < noOfColumns; j++) {
							Columns column = columns.get(columnNamesInFile.get(j));
							XSSFCell cell = row.getCell(j);
							field : for (Field field : fields) { 
								for (Annotation ann : field.getAnnotations()) {
									if(ann instanceof Column){
										Column c = (Column) ann;
										if (c.name().equalsIgnoreCase(columnNamesInFile.get(j))) {
											/*String methodName = "set" + field.toString().substring(0, 1).toUpperCase()
													+ field.toString().substring(1);*/
											String methodName = "set" + field.getName().substring(0, 1).toUpperCase()
													+ field.getName().substring(1);
											Method setter = fileObject.getClass().getMethod(methodName,
													(Class<?>) field.getType());
											try {
												if (cell != null && !isCellEmpty(cell)) {
													if (column.getColumnType().equals("NUMBER")||column.getColumnType().equals("INTEGER")|| column.getColumnType().equals("DOUBLE")){
														setter.invoke(fileObject,
																processNumericValue(error, j, column, cell));
													} else if (column.getColumnType().equals("DATE")) {
														setter.invoke(fileObject,
																processDateValue(error, j, column, cell));
													} else {
														setter.invoke(fileObject,
																processTextValue(error, j, column, cell));
													}
												} else {
													if (!column.isColumnNullable()) {
														error.setRowErrors(j + 1, NOT_NULLABLE_COLUMN_ERROR);
													} else {
														setter.invoke(fileObject, (Object) null);
													}
												}
											} catch (Exception e) {
												error.setRowErrors(j + 1, DATA_TYPE_INVALID);
												break row;
											}
											
											break field;
										}
									}else{
										continue;
									}
								}
							}
						}
						if(error.toString().equals(""))
							insertObjectList.add(fileObject);
					}
					// Prepare Updates when Color is Yellow
					else if (yellowHex.equals(colorHex)) {
						indexOfColorColumn = new ArrayList<Integer>();
						Map<Integer, Object> keyMap = new HashMap<>();
						for (int j = 0; j < noOfColumns; j++) {
							if (primaryKeyColumnNumbers.contains(j)) {
								XSSFCell cell = row.getCell(j);
								Columns column = columns.get(columnNamesInFile.get(j));
								if (cell != null && !isCellEmpty(cell)) {
									Double d = processNumericValue(error, j, column, cell);
									//removed int value was getting class cast exc int->double
									keyMap.put(j, d);
								}else{
									error.setRowErrors(j + 1, NOT_NULLABLE_COLUMN_ERROR);
								}
							} else {
								XSSFColor colorOfColumnsOfUpdatedRow = row.getCell(j).getCellStyle()
										.getFillForegroundColorColor();
								if (colorOfColumnsOfUpdatedRow != null
										&& yellowHex.equals(colorOfColumnsOfUpdatedRow.getARGBHex()))
									indexOfColorColumn.add(j);
							}
						}
						if(error.toString().equals("")){
							
							Object fileObject = retrieveObject(keyMap, columnNamesInFile, modelClass, session, fields);
							
							if(fileObject!=null){
								/*initFieldsforUpdation(fileObject, columnNamesInFile, row, error,
										indexOfColorColumn, columns, fields);
								if(error.toString().equals(""))
									modifyObjectList.add(fileObject);*/
								
								Query q = createUpdateQuery(columnNamesInFile, row, error, indexOfColorColumn, keyMap, columns, fields, sdTable.getTableName(), session);
								if(q!=null)
									modifyQueryList.add(q);
							}else
								System.out.println("obj doesn't exist");
						}
					}
					// Prepare Deletes when Color is Red
					else if (redHex.equals(colorHex)) {
						Map<Integer, Object> keyMap = new HashMap<>();
						for (int j = 0; j < noOfColumns; j++) {
							if (primaryKeyColumnNumbers.contains(j)) {
								XSSFCell cell = row.getCell(j);
								Columns column = columns.get(columnNamesInFile.get(j));
								Double d = processNumericValue(error, j, column, cell);
								keyMap.put(j, d);
							}
						}
						if(error.toString().equals("")){
							Object fileObject = retrieveObject(keyMap, columnNamesInFile, modelClass, session, fields);
							if(fileObject!=null){
								Query q = createdeleteQuery(columnNamesInFile, row, error, keyMap, columns, fields, sdTable.getTableName(), session);
								//deleteObjectList.add(fileObject);
								deleteQueryList.add(q);
							}
						}
					}
					if (!error.toString().equals("")) {
						errors.put(i, error);
					}
				}

			}
			/* Perform Inserts/Updates/Deletes here */
			
			
			
			//inserting records
			int batchSize = 5;	
			for(int i = 0; i< insertObjectList.size(); i++ ){
				session.save(insertObjectList.get(i));
				if(i%batchSize==0){
					session.flush();
					session.clear();
				}
			}
			//modifying records
			for(int i = 0; i< modifyObjectList.size(); i++){
				session.update(modifyObjectList.get(i));
				if(i%batchSize==0){
					session.flush();
					session.clear();
				}
			}
			
			for(int i = 0; i< modifyQueryList.size(); i++){
				modifyQueryList.get(i).executeUpdate();
			}
			
			//remove records
			for(int i = 0; i< deleteObjectList.size(); i++){
				session.delete(deleteObjectList.get(i));
				if(i%batchSize==0){
					session.flush();
					session.clear();
				}
			}
			for(int i = 0; i< deleteQueryList.size(); i++){
				deleteQueryList.get(i).executeUpdate();
			}
			
			
			tran.commit();
			session.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			closeWorkBook(workbook);
		}
		return false;
	}
	
	private Object retrieveObject( Map<Integer, Object> keyMap,
			List<String> columnsInFile, Class modelClass, Session session, Field[] fields){
		Criteria crit = session.createCriteria(modelClass);
		for(int index : keyMap.keySet()){
			field : for (Field field : fields) { 
				for (Annotation ann : field.getAnnotations()) {
					if(ann instanceof Column){
						Column c = (Column) ann;
						if (c.name().equalsIgnoreCase(columnNamesInFile.get(index))) {
							crit.add(Restrictions.eq(field.getName(), keyMap.get(index)));
							break field;
						}
					}
				}
			}
		}
		
		return crit.uniqueResult();	
	}
	
	private void initFieldsforUpdation(Object obj, List<String> columnsInFile, XSSFRow row, FileError error,
			ArrayList<Integer> indexOfColorColumn, Map<String, Columns> columns, Field[] fields) throws NoSuchMethodException{
		for(int index : indexOfColorColumn) {
			Columns column = columns.get(columnNamesInFile.get(index));
			XSSFCell cell = row.getCell(index);
			field : for (Field field : fields) { 
				for (Annotation ann : field.getAnnotations()) {
					if(ann instanceof Column){
						Column c = (Column) ann;
						if (c.name().equalsIgnoreCase(columnNamesInFile.get(index))) {
							String methodName = "set" + field.getName().substring(0, 1).toUpperCase()
									+ field.getName().substring(1);
							Method setter = obj.getClass().getMethod(methodName,
									(Class<?>) field.getType());
							try {
								if (cell != null && !isCellEmpty(cell)) {
									if (column.getColumnType().equals("NUMBER")||column.getColumnType().equals("INTEGER")) {
										setter.invoke(obj,
												processNumericValue(error, index, column, cell));
									} else if (column.getColumnType().equals("DATE")) {
										setter.invoke(obj,
												processDateValue(error, index, column, cell));
									} else {
										setter.invoke(obj,
												processTextValue(error, index, column, cell));
									}
								} else {
									if (!column.isColumnNullable()) {
										error.setRowErrors(index + 1, NOT_NULLABLE_COLUMN_ERROR);
									} else {
										setter.invoke(obj, (Object) null);
									}
								}
							} catch (Exception e) {
							}
							break field;
						}else{
							continue;
						}
					}
				}
			}	
		}
	}
	
	private Query createUpdateQuery(List<String> columnsInFile, XSSFRow row, FileError error,
			ArrayList<Integer> indexOfColorColumn, Map<Integer, Object> keyMap, Map<String, Columns> columns,
			Field[] fields, String tableName, Session session) throws NoSuchMethodException{
		StringBuilder hql = new StringBuilder("UPDATE "+tableName+" set ");
		Query query = null;
		Map<String, Object> colKeyValue = new HashMap<>();
		Map<String, Object> primKeyValue = new HashMap<>();
		for(int index : indexOfColorColumn) {
			Columns column = columns.get(columnNamesInFile.get(index));
			XSSFCell cell = row.getCell(index);
			field : for (Field field : fields) { 
				for (Annotation ann : field.getAnnotations()) {
					if(ann instanceof Column){
						Column c = (Column) ann;
						if (c.name().equalsIgnoreCase(columnNamesInFile.get(index))) {
							try {
								if (cell != null && !isCellEmpty(cell)) {
									if (column.getColumnType().equals("NUMBER")||column.getColumnType().equals("INTEGER")) {
										Object val = processNumericValue(error, index, column, cell);
										colKeyValue.put(field.getName(), val); 
									} else if (column.getColumnType().equals("DATE")) {
										Object val = processDateValue(error, index, column, cell);
										colKeyValue.put(field.getName(), val); 
									} else {
										Object val = processTextValue(error, index, column, cell);
										colKeyValue.put(field.getName(), val);
									}
								} else {
									if (!column.isColumnNullable()) {
										error.setRowErrors(index + 1, NOT_NULLABLE_COLUMN_ERROR);
									} else {
										colKeyValue.put(field.getName(), null);
									}
								}
							} catch (Exception e) {
							}
							break field;
						}else{
							continue;
						}
					}
				}
			}	
		}
		if(error.toString().equals("")){
			List<String> cols = new ArrayList<>(colKeyValue.keySet());
			for(int i = 0; i<cols.size(); i++){
				if(i!=cols.size()-1)
					hql.append(cols.get(i)+"=:"+cols.get(i)+", ");
				else
					hql.append(cols.get(i)+"=:"+cols.get(i));
			}
			
			hql.append(" where ");
			for(int index : keyMap.keySet()){
				field : for (Field field : fields) { 
					for (Annotation ann : field.getAnnotations()) {
						if(ann instanceof Column){
							Column c = (Column) ann;
							if (c.name().equalsIgnoreCase(columnNamesInFile.get(index))) {
								primKeyValue.put(field.getName(), keyMap.get(index));
								break field;
							}
						}
					}
				}
			}
			
			
			List<String> prims = new ArrayList<>(primKeyValue.keySet());
			for(int i = 0; i<prims.size(); i++){
				if(i!=prims.size()-1)
					hql.append(prims.get(i)+"=:"+prims.get(i)+" and ");
				else
					hql.append(prims.get(i)+"=:"+prims.get(i));
			}
			
			hql.append(" and 1=1");
			String q = new String(hql);
			query = session.createQuery(q);
			
			for(String key : colKeyValue.keySet()){
				query.setParameter(key, colKeyValue.get(key));
			}
			for(String key : primKeyValue.keySet()){
				query.setParameter(key, primKeyValue.get(key));
			}
		}
		
		return query;	
	}
	
	private Query createdeleteQuery(List<String> columnsInFile, XSSFRow row, FileError error,
			Map<Integer, Object> keyMap, Map<String, Columns> columns,
			Field[] fields, String tableName, Session session){
		StringBuilder hql = new StringBuilder("DELETE FROM "+tableName+" WHERE ");
		Query query = null;
		Map<String, Object> primKeyValue = new HashMap<>();
		for(int index : keyMap.keySet()){
			field : for (Field field : fields) { 
				for (Annotation ann : field.getAnnotations()) {
					if(ann instanceof Column){
						Column c = (Column) ann;
						if (c.name().equalsIgnoreCase(columnNamesInFile.get(index))) {
							primKeyValue.put(field.getName(), keyMap.get(index));
							break field;
						}
					}
				}
			}
		}
		
		List<String> prims = new ArrayList<>(primKeyValue.keySet());
		for(int i = 0; i<prims.size(); i++){
			if(i!=prims.size()-1)
				hql.append(prims.get(i)+"=:"+prims.get(i)+" and ");
			else
				hql.append(prims.get(i)+"=:"+prims.get(i));
		}
		
		String q = new String(hql);
		query = session.createQuery(q);
		
		for(String key : primKeyValue.keySet()){
			query.setParameter(key, primKeyValue.get(key));
		}
		
		return query;
	}
	
	private boolean isCellEmpty(XSSFCell cell) {
		return (cell.getCellTypeEnum() == CellType.BLANK
				|| (cell.getCellTypeEnum() == CellType.STRING && cell.getStringCellValue().isEmpty()));
	}

	/**
	 * 
	 * @param error
	 * @param j
	 * @param column
	 * @param cell
	 * @return
	 */
	private String processTextValue(FileError error, int j, Columns column, XSSFCell cell) {
		if (cell.getCellTypeEnum() == CellType.NUMERIC) {
			if (!isNumberSizeValid(cell.getNumericCellValue(), column.getColumnSize(), 0)) {
				error.setRowErrors(j + 1, DATA_LENGTH_ISSUE);
			} else {
				return String.format("%.0f", cell.getNumericCellValue());
			}
		} else if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
			if (!isSizeValid(Boolean.toString(cell.getBooleanCellValue()), column.getColumnSize())) {
				error.setRowErrors(j + 1, DATA_LENGTH_ISSUE);
			} else {
				return Boolean.toString(cell.getBooleanCellValue());
			}
		} else {
			if (column.getColumnType().equals("TIMESTAMP")) {
				return cell.getStringCellValue();
			} else {
				if (cell.getHyperlink() != null) {
					error.setRowErrors(j + 1, DATA_TYPE_INVALID);
				} else if (!isSizeValid(cell.getStringCellValue(), column.getColumnSize())) {
					error.setRowErrors(j + 1, DATA_LENGTH_ISSUE);
				} else {
					return cell.getStringCellValue();
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param error
	 * @param j
	 * @param column
	 * @param cell
	 * @return
	 */
	private Date processDateValue(FileError error, int j, Columns column, XSSFCell cell) {
		if (cell.getCellTypeEnum() != CellType.FORMULA) {
			error.setRowErrors(j + 1, DATA_TYPE_INVALID);
		} else if (!isSizeValid(cell.getDateCellValue() + "", column.getColumnSize())) {
			error.setRowErrors(j + 1, DATA_LENGTH_ISSUE);
		} else {
			return cell.getDateCellValue();
		}
		return null;
	}

	/**
	 * 
	 * @param error
	 * @param j
	 * @param column
	 * @param cell
	 * @return
	 */
	private double processNumericValue(FileError error, int j, Columns column, XSSFCell cell) {
		if (cell.getCellTypeEnum() != CellType.NUMERIC) {
			error.setRowErrors(j + 1, DATA_TYPE_INVALID);
		} else if (!isNumberSizeValid(cell.getNumericCellValue(), column.getColumnSize() - 1, column.getScale())) {
			error.setRowErrors(j + 1, DATA_LENGTH_ISSUE);
		} else {
			return cell.getNumericCellValue();
		}
		return 0;
	}

	/**
	 * Validate column header from the file
	 * 
	 * @param row
	 * @param columns
	 * @return
	 */
	private boolean validateColumnHeader(XSSFRow row, Map<String, Columns> columns) {
		FileError error = new FileError();

		for (int j = 0; j < noOfColumns; j++) {
			columnNamesInFile.add(row.getCell(j).getStringCellValue().toUpperCase());
		}

		verifyColumnNamesInFile(columns, error, columnNamesInFile);
		verifyIfMandatoryColumnsArePresent(columns, error, columnNamesInFile);

		if (errors.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Verify whether all mandatory columns are present in the excel or not
	 * 
	 * @param columns
	 * @param error
	 * @param columnNamesInFile
	 */
	private void verifyIfMandatoryColumnsArePresent(Map<String, Columns> columns, FileError error,
			List<String> columnNamesInFile) {
		int columnNumber = 1;
		for (String columnName : columns.keySet()) {
			if (!columnNamesInFile.contains(columnName)) {
				if (!((columns.get(columnName).isColumnNullable())
						|| (columns.get(columnName).getDefaultValue() != null))) {
					error.setRowErrors(columnNumber, columnName + " : Mandatory Column missing");
				}
			}
			columnNumber++;
			if (!error.toString().equals("")) {
				errors.put(0, error);
			}
		}
	}

	/**
	 * Verify whether correct column names are given in the file or not
	 * 
	 * @param columns
	 * @param error
	 * @param columnNamesInFile
	 */
	private void verifyColumnNamesInFile(Map<String, Columns> columns, FileError error,
			List<String> columnNamesInFile) {
		int columnNumber = 1;
		for (String columnInFile : columnNamesInFile) {
			if (!columns.keySet().contains(columnInFile)) {
				error.setRowErrors(columnNumber, columnInFile + " : Column name incorrect");
			}
			if (!error.toString().equals("")) {
				errors.put(0, error);
			}
			columnNumber++;
		}
	}

	/**
	 * Create the insert query template
	 * 
	 * @param tableName
	 * @param firstRow
	 * @return
	 */
	private String createInsertQueryTemplate(String tableName, XSSFRow firstRow) {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + tableName + " (");
		for (int j = 0; j < noOfColumns - 1; j++) {
			sb.append(firstRow.getCell(j).getStringCellValue().toUpperCase() + ",");
		}
		sb.append(firstRow.getCell(noOfColumns - 1).getStringCellValue().toUpperCase() + ")").append(" VALUES (");

		for (int j = 0; j < noOfColumns - 1; j++) {
			sb.append("?,");
		}
		sb.append("?)");
		return sb.toString();
	}

	/**
	 * Validate the size of the column
	 * 
	 * @param stringCellValue
	 * @param column
	 * @return
	 */
	private boolean isSizeValid(String stringCellValue, int columnSize) {
		return !(stringCellValue.getBytes().length > columnSize);
	}

	/**
	 * Checks whether the size of the number is valid or not
	 * 
	 * @param numericCellValue
	 * @param column
	 * @return
	 */
	private boolean isNumberSizeValid(double numericCellValue, int columnSize, int scale) {
		if (scale == 0) {
			return !(String.format("%.0f", numericCellValue).length() > columnSize);
		} else {
			return !(String.format("%." + scale + "f", numericCellValue).length() > columnSize);
		}
	}

	/**
	 * Write the Errors into output file
	 * 
	 * @param fileName
	 * @param outputFolder
	 * @param outputFileName
	 * @param errors
	 * @throws WFException
	 */
	private void writeErrorFile(String fileName, String outputFolder, String outputFileName,
			Map<Integer, FileError> errors) throws Exception {
		System.out.println("Generating error file");
		/* Opening the input file */
		XSSFWorkbook workbook = null;
		XSSFWorkbook newWorkbook = null;
		try {
			try {
				workbook = new XSSFWorkbook(new FileInputStream(fileName));
			} catch (FileNotFoundException e) {
				System.out.println(e);
				throw e;
			} catch (IOException e) {
				System.out.println(e);
				throw e;
			}
			XSSFSheet sheet = workbook.getSheetAt(0);

			/* Copy the inputfile into an output excel file */
			String outputFile = outputFolder + outputFileName;
			newWorkbook = new XSSFWorkbook();
			XSSFSheet newSheet = newWorkbook.createSheet();
			ExcelCopyUtil.copySheets(newSheet, sheet, true);
			int col = noOfColumns;
			int i = 0;
			Cell cell = null;
			newSheet = newWorkbook.getSheetAt(0);
			while (i <= newSheet.getLastRowNum()) {
				if (errors.get(i) != null) {
					cell = newSheet.getRow(i).createCell(col);
					cell.setCellValue(errors.get(i).toString());
				}
				i++;
			}

			/* Write the generated error file into the fileSystem */
			FileOutputStream fileOut = null;
			try {
				fileOut = new FileOutputStream(outputFile);
				newWorkbook.write(fileOut);
			} catch (FileNotFoundException e) {
				System.out.println(e);
				throw e;
			} catch (IOException e) {
				System.out.println(e);
				throw e;
			} finally {
				IOUtils.closeQuietly(fileOut);
			}
		} finally {
			closeWorkBook(workbook);
			closeWorkBook(newWorkbook);
		}
		System.out.println("Your error file has been generated!");

	}

	private void closeWorkBook(Workbook workBook) throws Exception {
		try {
			if (workBook != null) {
				workBook.close();
			}
		} catch (IOException e) {
			System.out.println(e);
			throw e;
		}
	}

	private int getPrimaryKeyColumnNumber(XSSFRow row, SDTable sdTable) {
		for (int j = 0; j < noOfColumns; j++) {
			if (sdTable.getTablePrimaryKeyColumn().equals(row.getCell(j).getStringCellValue().toUpperCase())) {
				return j;
			}
		}
		return -1;
	}

	// my version return list
	private List<Integer> getPrimaryKeyColumnNumbers(XSSFRow row, SDTable sdTable) {
		List<Integer> keysInRow = new ArrayList<Integer>();
		String keys[] = sdTable.getTablePrimaryKeyColumn().split(",");
		for (String key : keys) {
			if (columnNamesInFile.contains(key)) {
				keysInRow.add(columnNamesInFile.indexOf(key));
			}
		}
		return keysInRow;
	}
	

}
