package tool;

public class Allexecutionһ��ִ�� {

	public static void main(String[] args)  {

		AutoCreateVo vo=new AutoCreateVo();
		try {
			vo.Auto();
		} catch (Exception e) {
			AutoCreateBo bo=new AutoCreateBo();
			try {
				bo.Auto();
			} catch (Exception e1) {
				AutoCreateDao dao=new AutoCreateDao();
				try {
					dao.Auto();
				} catch (Exception e2) {
					AutoCreateControlle controlle=new AutoCreateControlle();
					try {
						controlle.Auto();
					} catch (Exception e3) {
						System.out.println("ִ�гɹ�,��ˢ��������Ŀ����");
					}
				}
			}
		}
		
		
		
	}

}
