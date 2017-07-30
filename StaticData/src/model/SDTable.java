package model;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="SDTable")
public class SDTable implements Serializable{

	private static final long serialVersionUID = 6264159186506795143L;

	@Id
	@Column(name = "TABLE_ID")
	private Long tableId;

	@Column(name = "TABLE_NAME")
	private String tableName;

	@Column(name = "DISPLAY_NAME")
	private String displayName;
	
	@Column(name = "BACKUP_TABLE_NAME")
	private String backupTableName;
	
	@Column(name = "TABLE_ENABLE_FLAG")
	private String tableEnableFlag;
	
	@Column(name = "TABLE_PRIMARY_KEY_COLUMN")
	private String tablePrimaryKeyColumn;
	
	@Column(name = "TABLE_MODEL_PACKAGE")
	private String tableModelPackage;
	
	public Long getTableId() {
		return tableId;
	}

	public void setTableId(Long tableId) {
		this.tableId = tableId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getBackupTableName() {
		return backupTableName;
	}

	public void setBackupTableName(String backupTableName) {
		this.backupTableName = backupTableName;
	}

	public String getTablePrimaryKeyColumn() {
		return tablePrimaryKeyColumn;
	}

	public void setTablePrimaryKeyColumn(String tablePrimaryKeyColumn) {
		this.tablePrimaryKeyColumn = tablePrimaryKeyColumn;
	}

	public String getTableEnableFlag() {
		return tableEnableFlag;
	}

	public void setTableEnableFlag(String tableEnableFlag) {
		this.tableEnableFlag = tableEnableFlag;
	}

	public String getTableModelPackage() {
		return tableModelPackage;
	}

	public void setTableModelPackage(String tableModelPackage) {
		this.tableModelPackage = tableModelPackage;
	}
	

}
