package tool;

import java.io.File; 
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * �������ݿ��ṹ �Զ�����java MVC�е�dao
 * 
 * @author DLHT 2018��7��14������5:54:52 AutoCreateDao.java 
 */
public class AutoCreateControlle {

		//����
		static String DBDRIVER;
		//��������ַ
		static String DBURL;
		//��¼�û���
		static String DBUID;
		//����
		static String DBPWD;

    private static String tablename;

    private String[] colnames; // ��������

    private String[] colTypes; // ������������

    private int[] colSizes; // ������С����
    static {
		//�ȴ�����Դ�ļ�����չ��Ϊ.properties
		//�������ԣ�dbuser=sa  ��ʽ
		Properties prop = new Properties();//�Ȼ�ȡ��Դ����
		//��������������ȡ��Դ�ļ�
		InputStream in =Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("jdbc.properties");
		try {
			prop.load(in);//����
			DBDRIVER = prop.getProperty("DBDRIVER");
			DBURL = prop.getProperty("DBURL");
			DBUID = prop.getProperty("DBUID");
			DBPWD = prop.getProperty("DBPWD");
			//System.out.println(DBDRIVER);
		} catch (IOException e) {
			System.out.println("��Դ�ļ���ȡ������鿴��Դ�ļ�");
		} 
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    /**
     * ��ȡָ�����ݿ��а����ı� TBlist
     * 
     * @time 2018��7��14������5:54:52
     * @packageName com.util
     * @return �������б���(�������ŵ�һ��������)
     * @throws Exception
     */
    public List<String> TBlist() throws Exception {
    	 // �������ݿ� ���� JDBC��ʽ
        Class.forName(DBDRIVER);

        Connection con = DriverManager.getConnection(DBURL, DBUID, DBPWD);


        DatabaseMetaData md = con.getMetaData();

        List<String> list = null;

        ResultSet rs = md.getTables(null, null, null, null);
        if (rs != null) {
            list = new ArrayList<String>();
        }
        while (rs.next()) {
//            System.out.println("|��" + (i++) + ":" + rs.getString("TABLE_NAME"));
            String tableName = rs.getString("TABLE_NAME");
            list.add(tableName);
        }
        rs = null;
        md = null;
        con = null;
        return list;
    }

    public void GenEntity(List<String> TBlist, String packageName)throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSetMetaData rsmd = null;

        // �������ݿ� ���� JDBC��ʽ
        Class.forName(DBDRIVER);
        conn = DriverManager.getConnection(DBURL, DBUID, DBPWD);

        for (int k = 0; k < TBlist.size(); k++) {
            tablename = TBlist.get(k);
            String strsql = "select * from " + tablename;
            pstmt = conn.prepareStatement(strsql);
            rsmd = pstmt.getMetaData();
            int size = rsmd.getColumnCount();
            // ���ж�����
            colnames = new String[size];
            colTypes = new String[size];
            colSizes = new int[size];
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                colnames[i] = rsmd.getColumnName(i + 1);
                colTypes[i] = rsmd.getColumnTypeName(i + 1);
                colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
            }
            markerBean(initcap(tablename), parse(), packageName);
        }
        
        pstmt = null;
        rsmd = null;
        conn = null;
    }
    /**
     * ����java �ļ� �����ɵ����� get/set ���� ���浽 �ļ��� Bean
     * 
     * @time 2018��7��14������5:54:52
     * @packageName fanshe
     * @param className
     *            ������
     * @param content
     *            ������ �������� getset ����
     */
   
    public static void Bean(String className, String content, String packageName) {
        String folder = System.getProperty("user.dir") + "/src/" + packageName + "/";
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
       String fileName= folder + className + ".java";
        try {
            File newdao = new File(fileName);
            FileWriter fw = new FileWriter(newdao);
            fw.write("package\t" + packageName.replace("/", ".") + ";\r\n");
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����R
     * 
     */
    private static String R(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("import com.dao.DBUtil;\r\n");
    	sb.append("public class R {\r\n");
    	sb.append("	public String toJson(){\r\n");
    	sb.append("        return DBUtil.toJson(this);\r\n");
    	sb.append("    }\r\n");
    	sb.append("		private int code;\r\n");
    	sb.append("		public int getCode() {\r\n");
    	sb.append("			return code;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public void setCode(int code) {\r\n");
    	sb.append("			this.code = code;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public String getMsg() {\r\n");
    	sb.append("			return msg;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public void setMsg(String msg) {\r\n");
    	sb.append("			this.msg = msg;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public int getCount() {\r\n");
    	sb.append("			return count;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public void setCount(int count) {\r\n");
    	sb.append("			this.count = count;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public Object getData() {\r\n");
    	sb.append("			return data;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public void setData(Object data) {\r\n");
    	sb.append("			this.data = data;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public R() {\r\n");
    	sb.append("			super();\r\n");
    	sb.append("		}\r\n");
    	sb.append("		public R(int code, String msg, int count, Object data) {\r\n");
    	sb.append("			super();\r\n");
    	sb.append("			this.code = code;\r\n");
    	sb.append("			this.msg = msg;\r\n");
    	sb.append("			this.count = count;\r\n");
    	sb.append("			this.data = data;\r\n");
    	sb.append("		}\r\n");
    	sb.append("		@Override\r\n");
    	sb.append("		public String toString() {\r\n");
    	sb.append("			return \"R [code=\" + code + \", msg=\" + msg + \", count=\" + count + \", data=\" + data + \"]\";\r\n");
    	sb.append("		}\r\n");
    	sb.append("		private String msg;\r\n");
    	sb.append("		private int count;\r\n");
    	sb.append("		private Object data;\r\n");
    	sb.append("}\r\n");

    	return sb.toString();
    }
    /**
     * ����BaseServlet
     * 
     */
    private static String BaseServlet() {
    	 StringBuffer sb = new StringBuffer();
         sb.append("import java.io.IOException;\r\n");
         sb.append("import java.io.UnsupportedEncodingException;\r\n");
         sb.append("import java.lang.reflect.Method;\r\n");
         sb.append("import javax.servlet.ServletException;\r\n");
         sb.append("import javax.servlet.annotation.WebServlet;\r\n");
         sb.append("import javax.servlet.http.HttpServlet;\r\n");
         sb.append("import javax.servlet.http.HttpServletRequest;\r\n");
         sb.append("import javax.servlet.http.HttpServletResponse;\r\n");
        sb.append("/**\r\n");
        sb.append(" * Servlet implementation class BaseServlet\r\n");
        sb.append(" */\r\n");
        sb.append("@WebServlet(\"/BaseServlet\")\r\n");
        sb.append("public class BaseServlet extends HttpServlet {\r\n");
        sb.append("	private static final long serialVersionUID = 1L;\r\n");
        sb.append("       \r\n");
        sb.append("	/**\r\n");
        sb.append("	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)\r\n");
        sb.append("	 */\r\n");
        sb.append("	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {\r\n");
        sb.append("		//�����˺��ж�\r\n");
        sb.append("		//user(request.getSession().getAttribute(\"user\").toString());\r\n\t\tCode(request,response);\r\n");
        sb.append("		String action=request.getParameter(\"action\");\r\n");
        sb.append("		if(action!=null){\r\n");
        sb.append("			try {\r\n");
        sb.append("				//��ȡ����\r\n");
        sb.append("				Method method=getClass().getMethod(action, HttpServletRequest.class, HttpServletResponse.class);\r\n");
        sb.append("				if(method!=null){\r\n");
        sb.append("					method.invoke(this, request,response);\r\n");
        sb.append("				}\r\n");
        sb.append("			} catch (Exception e) {\r\n");
        sb.append("				//�쳣����500ҳ��\r\n");
        sb.append("				request.getRequestDispatcher(\"Exception500.html\").forward(request, response);\r\n");
        sb.append("			}\r\n");
        sb.append("		}\r\n");
        sb.append("		else{\r\n");
        sb.append("			//����404ҳ��\r\n");
        sb.append("			request.getRequestDispatcher(\"Exception404.html\").forward(request, response);\r\n");
        sb.append("		}\r\n");
        sb.append("	}\r\n");
        sb.append("	/****\r\n");
        sb.append("	 * ��ȡ�û���\r\n");
        sb.append("	 * \r\n");
        sb.append("	 * @param user\r\n");
        sb.append("	 * @return\r\n");
        sb.append("	 */\r\n");
        sb.append("	protected boolean user(String user){\r\n");
        sb.append("		if(user==null)\r\n");
        sb.append("			return false;\r\n");
        sb.append("		return true;\r\n");
        sb.append("	}\r\n");
        sb.append("	/****\r\n");
        sb.append("	 * ���ñ����ʽ\r\n");
        sb.append("	 * \r\n");
        sb.append("	 * @param request\r\n");
        sb.append("	 * @param response\r\n");
        sb.append("	 * @throws UnsupportedEncodingException\r\n");
        sb.append("	 */\r\n");
        sb.append("	protected void Code(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{\r\n");
        sb.append("		request.setCharacterEncoding(\"utf-8\");\r\n");
        sb.append("		response.setContentType(\"application/json;charset=utf-8\");\r\n");
        sb.append("	}\r\n");
        sb.append("\r\n");
        sb.append("}\r\n");
    	 return sb.toString();
    }
    /**
     * ��������(����ʵ�����������)
     */
    private String parse() {
        StringBuffer sb = new StringBuffer();
        //�����
        sb.append("import java.io.IOException;\r\n");
        sb.append("import java.util.*;\r\n");
        sb.append("import javax.servlet.ServletException;\r\n");
        sb.append("import javax.servlet.annotation.WebServlet;\r\n");
        sb.append("import javax.servlet.http.HttpServlet;\r\n");
        sb.append("import javax.servlet.http.HttpServletRequest;\r\n");
        sb.append("import javax.servlet.http.HttpServletResponse;\r\n");
        
        //�����Ӧʵ���
        sb.append("import com.bo." + initcap(tablename) + "BO;\r\n");
        sb.append("import com.vo." + initcap(tablename) + ";\r\n");
        sb.append("import com.dao.DBUtil;\r\n");
        //���뷽������
        sb.append("/**\r\n");
    	sb.append(" * Servlet implementation class "+ initcap(tablename)+"Controlle\r\n");
    	sb.append(" */\r\n");
    	sb.append("@WebServlet(\"/"+ initcap(tablename)+"\"\r\n");
    	sb.append(")\r\n");
    	
    	
    	sb.append("public class "+ initcap(tablename)+"Controlle extends BaseServlet {\r\n");
    	sb.append("\tprivate static final long serialVersionUID = 1L;\r\n");
    	//new��BO
    	sb.append("\t\tstatic " + initcap(tablename) + "BO  bo=new " + initcap(tablename) + "BO(); \r\n");
    	//ʵ����
    	sb.append("\t\t" + initcap(tablename) +"\t"+ tablename+"=new " + initcap(tablename) + "(); \r\n");
    	//�����ֶ�
    	processAll(sb);
    	//����
    	processAllMethod(sb);
        sb.append("}\r\n");

        return sb.toString();

    }

    /**
     * ����java �ļ� �����ɵ����� get/set ���� ���浽 �ļ��� markerBean
     * 
     * @time 2018��7��14������5:54:52
     * @packageName fanshe
     * @param className
     *            ������
     * @param content
     *            ������ �������� getset ����
     */
   
    public void markerBean(String className, String content, String packageName) {
        String folder = System.getProperty("user.dir") + "/src/" + packageName + "/";
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
       String fileName= folder + className + "Controlle.java";
        try {
            File newdao = new File(fileName);
            FileWriter fw = new FileWriter(newdao);
            fw.write("package\t" + packageName.replace("/", ".") + ";\r\n");
            fw.write(content);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * �������еķ���
     * 
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
    	//------------------------------------------------------------���
    	 /**
         * ������Ӳ�����������add
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * ��Ӳ���\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void add(HttpServletRequest request, HttpServletResponse response){\r\n");
    	//����reqeust������set
    	processAllAttrs(sb);
    	//������Ӵ���0����
    	ifinsert(sb);
    	sb.append("\t}\r\n");
    	//------------------------------------------------------------�޸�
    	 /**
         * �����޸Ĳ�����������update
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * �޸Ĳ���\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void update(HttpServletRequest request, HttpServletResponse response){\r\n");
    	//��ȡ�޸ĵ�����
    	sb.append("\t\t"+initcap(colnames[0])+"=Integer.parseInt(request.getParameter(\""+initcap(colnames[0])+"\"));\r\n");
    	//����reqeust������set
    	processAllAttrs(sb);
    	//�����޸ĵ��ֶ�
    	 sb.append("\t\t"+tablename+".set" + initcap(colnames[0])+"("+initcap(colnames[0])+");\r\n");
    	//������Ӵ���0����
    	ifupdate(sb);
    	sb.append("\t}\r\n");
    	//------------------------------------------------------------ɾ��
    	 /**
         * ����ɾ��������������delete
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * �޸Ĳ���\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void delete(HttpServletRequest request, HttpServletResponse response){\r\n");
    	//��ȡɾ��������
    	sb.append("\t\t"+initcap(colnames[0])+"=Integer.parseInt(request.getParameter(\""+initcap(colnames[0])+"\"));\r\n");
    	//����ɾ������
    	 ifdelete(sb);
    	sb.append("\t}\r\n");
    	//------------------------------------------------------------��ѯ
    	/**
         * ���ɲ�ѯ������������select
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * ��ҳ��ѯ����\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void select(HttpServletRequest request, HttpServletResponse response){\r\n");
    	sb.append("\t\t//��ҳ��ѯ\r\n");
    	sb.append("\t\tPaging(request,response);\r\n");
    	sb.append("\t\t}\r\n");
    	sb.append("\t/****\r\n");
    	sb.append("\t * ȫ����ѯ����\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void selectAll(HttpServletRequest request, HttpServletResponse response){\r\n");
    	sb.append("\t\t//ȫ����ѯ\r\n");
    	sb.append("\t\tfinAll(request,response);\r\n");
    	sb.append("\t\t}\r\n");
    	//ȫ����ѯ����
    	listselect(sb);
    	//��ҳ��ѯ����
    	Pagingselect(sb);
    }
    /**
     * ��Ӻ��жϴ���0
     * @param sb
     */
    private void ifinsert(StringBuffer sb) {
    sb.append("\t\tint sum=bo.insert("+ tablename+");\r\n");
	sb.append("\t\ttry {\r\n");
	sb.append("\t\tresponse.getWriter().println(sum);\r\n");
	sb.append("\t\t} catch (IOException e) {\r\n");	
	sb.append("\t\te.printStackTrace();\r\n");
	sb.append("\t\t}\r\n");
    }
    /**
     * �޸ĺ��жϴ���0
     * @param sb
     */
    private void ifupdate(StringBuffer sb) {
    	  sb.append("\t\tint sum=bo.update("+ tablename+");\r\n");
    		sb.append("\t\ttry {\r\n");
    		sb.append("\t\tresponse.getWriter().println(sum);\r\n");
    		sb.append("\t\t} catch (IOException e) {\r\n");	
    		sb.append("\t\te.printStackTrace();\r\n");
    		sb.append("\t\t}\r\n");
    }
    /**
     * ɾ�����жϴ���0
     * @param sb
     */
    private void ifdelete(StringBuffer sb) {
    	 sb.append("\t\tint sum=bo.delete("+ initcap(colnames[0])+");\r\n");
 		sb.append("\t\ttry {\r\n");
 		sb.append("\t\tresponse.getWriter().println(sum);\r\n");
 		sb.append("\t\t} catch (IOException e) {\r\n");	
 		sb.append("\t\te.printStackTrace();\r\n");
 		sb.append("\t\t}\r\n");
    }
    /**
     * ��ҳ��ѯ
     * @param sb
     */
    private void Pagingselect(StringBuffer sb) {
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ��ҳ��ѯ\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tprivate static void Paging(HttpServletRequest request, HttpServletResponse response) {\r\n");
		sb.append("\t\tint page=Integer.parseInt(request.getParameter(\"page\"));\r\n");
    	sb.append("\t\tint limit=Integer.parseInt(request.getParameter(\"limit\"));\r\n");
    	sb.append("\t\tList<"+initcap(tablename)+"> list=bo.Paging(page, limit);\r\n");
    	sb.append("\t\tR r=new R();\r\n");
    	sb.append("\t\tr.setCount(bo.getcount());\r\n");
		sb.append("\t\tr.setData(list);\r\n");
		sb.append("\t\ttry {\r\n");
		sb.append("\t\tresponse.getWriter().println(DBUtil.toJson(r));\r\n");
		sb.append("\t\t} catch (IOException e) {\r\n");
		sb.append("\t\te.printStackTrace();\r\n");
		sb.append("\t\t}\r\n");
		sb.append("\t\t}\r\n");
    }
    /**
     * ȫ����ѯ��ѯ
     * @param sb
     */
    private void listselect(StringBuffer sb) {
    	
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ȫ����ѯ\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tprivate static void finAll(HttpServletRequest request, HttpServletResponse response) {\r\n");
    	sb.append("\t\tList<"+initcap(tablename)+"> list=bo.findAll();\r\n");
    	sb.append("\t\tR r=new R();\r\n");
    	sb.append("\t\tr.setCount(bo.getcount());\r\n");
		sb.append("\t\tr.setData(list);\r\n");
		sb.append("\t\ttry {\r\n");
		sb.append("\t\tresponse.getWriter().println(DBUtil.toJson(r));\r\n");
		sb.append("\t\t} catch (IOException e) {\r\n");
		sb.append("\t\te.printStackTrace();\r\n");
		sb.append("\t\t}\r\n");
		sb.append("\t\t}\r\n");
    }
    /**�����ֶ�
     * 
     * 
     * @return
     */
    private void processAll(StringBuffer sb) {
    	 //����set����
        for (int i = 0; i < colnames.length; i++) {
            sb.append("\t\t" + processAllrequest(sqlType2JavaType(colTypes[i]),initcap(colnames[i]))+ "\r\n");
        }
        
    }
    /**
     * ����getParameter�����ɵ� set
     * 
     * @return
     */
    private void processAllAttrs(StringBuffer sb) {
    	//����request.getParameter
        for (int i = 1; i < colnames.length; i++) {
            sb.append("\t\t" + getParameter(sqlType2JavaType(colTypes[i]),initcap(colnames[i]))+ "\r\n");
        }
        //����set����
        for (int i = 1; i < colnames.length; i++) {
            sb.append("\t\t"+tablename+".set" + initcap(colnames[i])+"("+initcap(colnames[i])+");\r\n");
        }
        
    }
    /**
    

    /**
     * �������ַ���������ĸ�ĳɴ�д
     * 
     * @param str
     * @return
     */
    private String initcap(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }
    /**
     * ��������
     * 
     * @param str
     * @return
     */
    private String getParameter(String sqlType,String names) {
    	 if (sqlType.equalsIgnoreCase("bit")) {
             return names+"=Boolean.parseBoolean(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("tinyint")) {
        	 return names+"=Byte.parseByte(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("smallint")) {
        	 return names+"=Short.parseShort(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("int")) {
        	 return names+"=Integer.parseInt(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("bigint")) {
        	 return names+"=Long.parseLong(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("float")) {
        	 return names+"=Float.parseFloat(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("decimal")
                 || sqlType.equalsIgnoreCase("numeric")
                 || sqlType.equalsIgnoreCase("real")) {
        	 return names+"=Double.parseDouble(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("money")
                 || sqlType.equalsIgnoreCase("smallmoney")) {
        	 return names+"=Double.parseDouble(request.getParameter(\""+names+"\"));";
         } else if (sqlType.equalsIgnoreCase("varchar")
                 || sqlType.equalsIgnoreCase("char")
                 || sqlType.equalsIgnoreCase("nvarchar")
                 || sqlType.equalsIgnoreCase("nchar")
                 || sqlType.equalsIgnoreCase("uniqueidentifier")
                 || sqlType.equalsIgnoreCase("ntext")) {
        	 return names+"=request.getParameter(\""+names+"\");";
         } else if (sqlType.equalsIgnoreCase("datetime")
                 ||sqlType.equalsIgnoreCase("date")){
        	 return names+"=DBUtil.date(request.getParameter(\""+names+"\"));";
         }

         else if (sqlType.equalsIgnoreCase("image")) {
        	 return names+"=Byte.parseByte(request.getParameter(\""+names+"\"));";
         }
    	 return names+"=request.getParameter(\""+names+"\");";
    }
    /**
     * �����ֶ�
     * 
     * @param str
     * @return
     */
    private String processAllrequest(String sqlType,String names) {
    	 if (sqlType.equalsIgnoreCase("bit")) {
             return "boolean "+names+";";
         } else if (sqlType.equalsIgnoreCase("tinyint")) {
        	 return "byte "+names+";";
         } else if (sqlType.equalsIgnoreCase("smallint")) {
        	 return "short "+names+";";
         } else if (sqlType.equalsIgnoreCase("int")) {
        	 return "int "+names+";";
         } else if (sqlType.equalsIgnoreCase("bigint")) {
        	 return "long "+names+";";
         } else if (sqlType.equalsIgnoreCase("float")) {
        	 return "float "+names+";";
         } else if (sqlType.equalsIgnoreCase("decimal")
                 || sqlType.equalsIgnoreCase("numeric")
                 || sqlType.equalsIgnoreCase("real")) {
        	 return "double "+names+";";
         } else if (sqlType.equalsIgnoreCase("money")
                 || sqlType.equalsIgnoreCase("smallmoney")) {
        	 return "double "+names+";";
         } else if (sqlType.equalsIgnoreCase("varchar")
                 || sqlType.equalsIgnoreCase("char")
                 || sqlType.equalsIgnoreCase("nvarchar")
                 || sqlType.equalsIgnoreCase("nchar")
                 || sqlType.equalsIgnoreCase("uniqueidentifier")
                 || sqlType.equalsIgnoreCase("ntext")) {
        	 return "String "+names+";";
         } else if (sqlType.equalsIgnoreCase("datetime")
                 ||sqlType.equalsIgnoreCase("date")){
        	 return "Date "+names+";";
         }

         else if (sqlType.equalsIgnoreCase("image")) {
        	 return "byte "+names+";";
         }
    	 return "String "+names+";";
    }
    private String sqlType2JavaType(String sqlType) {
        if (sqlType.equalsIgnoreCase("bit")) {
            return "boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "short";
        } else if (sqlType.equalsIgnoreCase("int")) {
            return "int";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "float";
        } else if (sqlType.equalsIgnoreCase("decimal")
                || sqlType.equalsIgnoreCase("numeric")
                || sqlType.equalsIgnoreCase("real")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("money")
                || sqlType.equalsIgnoreCase("smallmoney")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("varchar")
                || sqlType.equalsIgnoreCase("char")
                || sqlType.equalsIgnoreCase("nvarchar")
                || sqlType.equalsIgnoreCase("nchar")
                || sqlType.equalsIgnoreCase("uniqueidentifier")
                || sqlType.equalsIgnoreCase("ntext")) {
            return "String";
        } else if (sqlType.equalsIgnoreCase("datetime")
                ||sqlType.equalsIgnoreCase("date")){
            return "Date";
        }
        else if (sqlType.equalsIgnoreCase("image")) {
            return "Blob";
        }
        return "String";
    }
    
    public static void main(String[] args) throws Exception {
    	//����BaseServlet
        Bean("BaseServlet", BaseServlet(), "com/controlle");
        //����R
        Bean("R", R(), "com/controlle");
    	AutoCreateControlle auto = new AutoCreateControlle();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/controlle");

    }
    /**
     * һ��ִ��
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	//����BaseServlet
        Bean("BaseServlet", BaseServlet(), "com/controlle");
        //����R
        Bean("R", R(), "com/controlle");
    	AutoCreateControlle auto = new AutoCreateControlle();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/controlle");
    }
}