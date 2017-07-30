import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ExcelCopyUtil {
	public static void copySheets(XSSFSheet sheetForNewWB, XSSFSheet sheetFromOldWB, boolean copyStyle){

		Row row;
		Cell cell;
	    for (int rowIndex = 0; rowIndex < sheetFromOldWB.getPhysicalNumberOfRows(); rowIndex++) {
	        row = sheetForNewWB.createRow(rowIndex); //create row in this new sheet
	        for (int colIndex = 0; colIndex < sheetFromOldWB.getRow(rowIndex).getPhysicalNumberOfCells(); colIndex++) {
	            cell = row.createCell(colIndex); //create cell in this row of this new sheet
	            Cell c = sheetFromOldWB.getRow(rowIndex).getCell(colIndex, Row.CREATE_NULL_AS_BLANK ); //get cell from old/original WB's sheet and when cell is null, return it as blank cells. And Blank cell will be returned as Blank cells. That will not change.
	                if (c.getCellType() == Cell.CELL_TYPE_BLANK){
	                    System.out.println("This is BLANK " +  ((XSSFCell) c).getReference());
	                }
	                else {  //Below is where all the copying is happening. First It copies the styles of each cell and then it copies the content.              
	                CellStyle origStyle = c.getCellStyle();
	                
	                cell.setCellStyle(origStyle);            

	                 switch (c.getCellTypeEnum()) {
	                    case STRING:                            
	                        cell.setCellValue(c.getRichStringCellValue().getString());
	                        break;
	                    case NUMERIC:
	                        if (DateUtil.isCellDateFormatted(cell)) {                             
	                            cell.setCellValue(c.getDateCellValue());
	                        } else {                              
	                            cell.setCellValue(c.getNumericCellValue());
	                        }
	                        break;
	                    case BOOLEAN:

	                        cell.setCellValue(c.getBooleanCellValue());
	                        break;
	                    case FORMULA:

	                        cell.setCellValue(c.getCellFormula());
	                        break;
	                    case BLANK:
	                        cell.setCellValue("who");
	                        break;
	                    default:
	                        System.out.println();
	                    }
	                }
	            }
	        }
	}
}