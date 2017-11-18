import java.io.IOException;

import MyClass.WebPage;


public class test {

	public test() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WebPage wb=new WebPage();
		try {
			System.out.println(wb.getPage("http://www.baidu.com/p/hehexiexieni/detail"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
