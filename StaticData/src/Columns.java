
public class Columns {
	private Boolean columnNullable = false;
	private String columnName;
	private int columnSize;
	private String columnType;
	private int scale;
	private String defaultValue;
	
	
	public Boolean isColumnNullable() {
		return columnNullable;
	}
	public void setColumnNullable(Boolean columnNullable) {
		this.columnNullable = columnNullable;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getColumnSize() {
		return columnSize;
	}
	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	
}
