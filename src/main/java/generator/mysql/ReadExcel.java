package generator.mysql;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class  ReadExcel{
	
	private String filePath;
	private List list = new ArrayList();
	
	public ReadExcel(String filePath){
		this.filePath = filePath;
	}
	
	private void readExcel() throws IOException, BiffException{
		//创建输入流
		InputStream stream = new FileInputStream(filePath);
		//获取Excel文件对象
		Workbook  rwb = Workbook.getWorkbook(stream);
		//获取文件的指定工作表 默认的第一个
	    Sheet sheet = rwb.getSheet(0);  
	    //行数(表头的目录不需要，从1开始)
	    for(int i=0; i<sheet.getRows(); i++){
	    	 //创建一个数组 用来存储每一列的值
	        String[] str = new String[sheet.getColumns()];
	        Cell cell = null;
		    //列数
		    for(int j=0; j<sheet.getColumns(); j++){
		      //获取第i行，第j列的值
		      cell = sheet.getCell(j,i);    
		      str[j] = cell.getContents();
		    }
		  //把刚获取的列存入list
		  list.add(str);
	    }
	}
	private void outData(){
		 for(int i=0;i<list.size();i++){
		     String[] str = (String[])list.get(i);
		     for(int j=0;j<str.length;j++){
		      System.out.print(str[j]+'\t');
		     }
		     System.out.println();
		 }
	}
	
	public static void main(String args[]) throws BiffException, IOException{
		ReadExcel excel = new ReadExcel("/media/liuqiang/repo/liuqiang/repo/SVN/Repos/merchant/trunk/18.资料/1804.数据收集/i爱康APP老客户信息统计表逆变器 - 整理后.xlsx");
		excel.readExcel();
		excel.outData();
	}
}