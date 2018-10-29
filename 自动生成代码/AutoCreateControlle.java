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
 * 根据数据库表结构 自动生成java MVC中的dao
 * 
 * @author DLHT 2018年7月14日下午5:54:52 AutoCreateDao.java 
 */
public class AutoCreateControlle {

		//驱动
		static String DBDRIVER;
		//服务器地址
		static String DBURL;
		//登录用户名
		static String DBUID;
		//密码
		static String DBPWD;

    private static String tablename;

    private String[] colnames; // 列名数组

    private String[] colTypes; // 列名类型数组

    private int[] colSizes; // 列名大小数组
    static {
		//先创建资源文件，扩展名为.properties
		//内容是以：dbuser=sa  格式
		Properties prop = new Properties();//先获取资源对象
		//创建输入流，读取资源文件
		InputStream in =Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("jdbc.properties");
		try {
			prop.load(in);//加载
			DBDRIVER = prop.getProperty("DBDRIVER");
			DBURL = prop.getProperty("DBURL");
			DBUID = prop.getProperty("DBUID");
			DBPWD = prop.getProperty("DBPWD");
			//System.out.println(DBDRIVER);
		} catch (IOException e) {
			System.out.println("资源文件读取错误，请查看资源文件");
		} 
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    /**
     * 获取指定数据库中包含的表 TBlist
     * 
     * @time 2018年7月14日下午5:54:52
     * @packageName com.util
     * @return 返回所有表名(将表名放到一个集合中)
     * @throws Exception
     */
    public List<String> TBlist() throws Exception {
    	 // 访问数据库 采用 JDBC方式
        Class.forName(DBDRIVER);

        Connection con = DriverManager.getConnection(DBURL, DBUID, DBPWD);


        DatabaseMetaData md = con.getMetaData();

        List<String> list = null;

        ResultSet rs = md.getTables(null, null, null, null);
        if (rs != null) {
            list = new ArrayList<String>();
        }
        while (rs.next()) {
//            System.out.println("|表" + (i++) + ":" + rs.getString("TABLE_NAME"));
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

        // 访问数据库 采用 JDBC方式
        Class.forName(DBDRIVER);
        conn = DriverManager.getConnection(DBURL, DBUID, DBPWD);

        for (int k = 0; k < TBlist.size(); k++) {
            tablename = TBlist.get(k);
            String strsql = "select * from " + tablename;
            pstmt = conn.prepareStatement(strsql);
            rsmd = pstmt.getMetaData();
            int size = rsmd.getColumnCount();
            // 共有多少列
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
     * 创建java 文件 将生成的属性 get/set 方法 保存到 文件中 Bean
     * 
     * @time 2018年7月14日下午5:54:52
     * @packageName fanshe
     * @param className
     *            类名称
     * @param content
     *            类内容 包括属性 getset 方法
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
     * 生成R
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
     * 生成BaseServlet
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
        sb.append("		//进行账号判断\r\n");
        sb.append("		//user(request.getSession().getAttribute(\"user\").toString());\r\n\t\tCode(request,response);\r\n");
        sb.append("		String action=request.getParameter(\"action\");\r\n");
        sb.append("		if(action!=null){\r\n");
        sb.append("			try {\r\n");
        sb.append("				//获取方法\r\n");
        sb.append("				Method method=getClass().getMethod(action, HttpServletRequest.class, HttpServletResponse.class);\r\n");
        sb.append("				if(method!=null){\r\n");
        sb.append("					method.invoke(this, request,response);\r\n");
        sb.append("				}\r\n");
        sb.append("			} catch (Exception e) {\r\n");
        sb.append("				//异常进入500页面\r\n");
        sb.append("				request.getRequestDispatcher(\"Exception500.html\").forward(request, response);\r\n");
        sb.append("			}\r\n");
        sb.append("		}\r\n");
        sb.append("		else{\r\n");
        sb.append("			//进入404页面\r\n");
        sb.append("			request.getRequestDispatcher(\"Exception404.html\").forward(request, response);\r\n");
        sb.append("		}\r\n");
        sb.append("	}\r\n");
        sb.append("	/****\r\n");
        sb.append("	 * 获取用户名\r\n");
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
        sb.append("	 * 设置编码格式\r\n");
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
     * 解析处理(生成实体类主体代码)
     */
    private String parse() {
        StringBuffer sb = new StringBuffer();
        //导入包
        sb.append("import java.io.IOException;\r\n");
        sb.append("import java.util.*;\r\n");
        sb.append("import javax.servlet.ServletException;\r\n");
        sb.append("import javax.servlet.annotation.WebServlet;\r\n");
        sb.append("import javax.servlet.http.HttpServlet;\r\n");
        sb.append("import javax.servlet.http.HttpServletRequest;\r\n");
        sb.append("import javax.servlet.http.HttpServletResponse;\r\n");
        
        //导入对应实体包
        sb.append("import com.bo." + initcap(tablename) + "BO;\r\n");
        sb.append("import com.vo." + initcap(tablename) + ";\r\n");
        sb.append("import com.dao.DBUtil;\r\n");
        //导入方法解体
        sb.append("/**\r\n");
    	sb.append(" * Servlet implementation class "+ initcap(tablename)+"Controlle\r\n");
    	sb.append(" */\r\n");
    	sb.append("@WebServlet(\"/"+ initcap(tablename)+"\"\r\n");
    	sb.append(")\r\n");
    	
    	
    	sb.append("public class "+ initcap(tablename)+"Controlle extends BaseServlet {\r\n");
    	sb.append("\tprivate static final long serialVersionUID = 1L;\r\n");
    	//new出BO
    	sb.append("\t\tstatic " + initcap(tablename) + "BO  bo=new " + initcap(tablename) + "BO(); \r\n");
    	//实例化
    	sb.append("\t\t" + initcap(tablename) +"\t"+ tablename+"=new " + initcap(tablename) + "(); \r\n");
    	//设置字段
    	processAll(sb);
    	//方法
    	processAllMethod(sb);
        sb.append("}\r\n");

        return sb.toString();

    }

    /**
     * 创建java 文件 将生成的属性 get/set 方法 保存到 文件中 markerBean
     * 
     * @time 2018年7月14日下午5:54:52
     * @packageName fanshe
     * @param className
     *            类名称
     * @param content
     *            类内容 包括属性 getset 方法
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
     * 生成所有的方法
     * 
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
    	//------------------------------------------------------------添加
    	 /**
         * 生成添加操作方法名称add
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * 添加操作\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void add(HttpServletRequest request, HttpServletResponse response){\r\n");
    	//接收reqeust和设置set
    	processAllAttrs(sb);
    	//设置添加大于0否则
    	ifinsert(sb);
    	sb.append("\t}\r\n");
    	//------------------------------------------------------------修改
    	 /**
         * 生成修改操作方法名称update
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * 修改操作\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void update(HttpServletRequest request, HttpServletResponse response){\r\n");
    	//获取修改的主键
    	sb.append("\t\t"+initcap(colnames[0])+"=Integer.parseInt(request.getParameter(\""+initcap(colnames[0])+"\"));\r\n");
    	//接收reqeust和设置set
    	processAllAttrs(sb);
    	//设置修改的字段
    	 sb.append("\t\t"+tablename+".set" + initcap(colnames[0])+"("+initcap(colnames[0])+");\r\n");
    	//设置添加大于0否则
    	ifupdate(sb);
    	sb.append("\t}\r\n");
    	//------------------------------------------------------------删除
    	 /**
         * 生成删除操作方法名称delete
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * 修改操作\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void delete(HttpServletRequest request, HttpServletResponse response){\r\n");
    	//获取删除的主键
    	sb.append("\t\t"+initcap(colnames[0])+"=Integer.parseInt(request.getParameter(\""+initcap(colnames[0])+"\"));\r\n");
    	//进行删除操作
    	 ifdelete(sb);
    	sb.append("\t}\r\n");
    	//------------------------------------------------------------查询
    	/**
         * 生成查询操作方法名称select
         * 
         * @param sb
         */
    	sb.append("\t/****\r\n");
    	sb.append("\t * 分页查询操作\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void select(HttpServletRequest request, HttpServletResponse response){\r\n");
    	sb.append("\t\t//分页查询\r\n");
    	sb.append("\t\tPaging(request,response);\r\n");
    	sb.append("\t\t}\r\n");
    	sb.append("\t/****\r\n");
    	sb.append("\t * 全部查询操作\r\n");
    	sb.append("\t * @param request\r\n");
    	sb.append("\t * @param response\r\n");
    	sb.append("\t*/\r\n");
    	sb.append("\tpublic void selectAll(HttpServletRequest request, HttpServletResponse response){\r\n");
    	sb.append("\t\t//全部查询\r\n");
    	sb.append("\t\tfinAll(request,response);\r\n");
    	sb.append("\t\t}\r\n");
    	//全部查询方法
    	listselect(sb);
    	//分页查询方法
    	Pagingselect(sb);
    }
    /**
     * 添加后判断大于0
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
     * 修改后判断大于0
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
     * 删除后判断大于0
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
     * 分页查询
     * @param sb
     */
    private void Pagingselect(StringBuffer sb) {
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 分页查询\r\n");
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
     * 全部查询查询
     * @param sb
     */
    private void listselect(StringBuffer sb) {
    	
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 全部查询\r\n");
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
    /**生成字段
     * 
     * 
     * @return
     */
    private void processAll(StringBuffer sb) {
    	 //生成set属性
        for (int i = 0; i < colnames.length; i++) {
            sb.append("\t\t" + processAllrequest(sqlType2JavaType(colTypes[i]),initcap(colnames[i]))+ "\r\n");
        }
        
    }
    /**
     * 接收getParameter和生成的 set
     * 
     * @return
     */
    private void processAllAttrs(StringBuffer sb) {
    	//接收request.getParameter
        for (int i = 1; i < colnames.length; i++) {
            sb.append("\t\t" + getParameter(sqlType2JavaType(colTypes[i]),initcap(colnames[i]))+ "\r\n");
        }
        //生成set属性
        for (int i = 1; i < colnames.length; i++) {
            sb.append("\t\t"+tablename+".set" + initcap(colnames[i])+"("+initcap(colnames[i])+");\r\n");
        }
        
    }
    /**
    

    /**
     * 把输入字符串的首字母改成大写
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
     * 接收属性
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
     * 定义字段
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
    	//生成BaseServlet
        Bean("BaseServlet", BaseServlet(), "com/controlle");
        //生成R
        Bean("R", R(), "com/controlle");
    	AutoCreateControlle auto = new AutoCreateControlle();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/controlle");

    }
    /**
     * 一键执行
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	//生成BaseServlet
        Bean("BaseServlet", BaseServlet(), "com/controlle");
        //生成R
        Bean("R", R(), "com/controlle");
    	AutoCreateControlle auto = new AutoCreateControlle();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/controlle");
    }
}