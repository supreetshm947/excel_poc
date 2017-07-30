import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileError {
	private Map<Integer, List<String>> rowErrors = new HashMap<>();

	public void setRowErrors(int col, String des) {
		if(rowErrors.containsKey(col)) {
			rowErrors.get(col).add(des);
		} else {
			List<String> lst = new LinkedList<>();
			lst.add(des);
			rowErrors.put(col, lst);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer,List<String>> entry : rowErrors.entrySet()) {
			sb.append(entry.getKey() + " : ");
			for(String str : entry.getValue()) {
				sb.append(str).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("; ");
		}
		return sb.toString();
	}
}
