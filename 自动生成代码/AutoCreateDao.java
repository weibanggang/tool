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
public class AutoCreateDao {

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
     * @time 2017年7月14日下午5:54:52
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
     * 解析处理(生成实体类主体代码)
     */
    private String parse() {
        StringBuffer sb = new StringBuffer();
        //导入包
        sb.append("import java.sql.*;\r\n");
        sb.append("import java.util.*;\r\n");
        //导入对应实体包
        sb.append("import com.vo." + initcap(tablename) + ";\r\n");
        
        sb.append("public class " + initcap(tablename) + "DAO {\r\n");
        processAllMethod(sb);
        sb.append("}\r\n");

        return sb.toString();

    }
    public static void Bean(String className, String content, String packageName) {
        String folder = System.getProperty("user.dir") + "/src/" + packageName + "/";

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = folder + className + ".java";

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
        String fileName = folder + className + "DAO.java";

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
     * 生成DBUtil方法
     * 
     * @param 
     */
    private static String DBUtil(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("\timport java.io.IOException;\r\n");
    	sb.append("\timport java.io.InputStream;\r\n");
    	sb.append("\timport java.sql.*;\r\n");
    	sb.append("\timport java.text.ParsePosition;\r\n");
    	sb.append("\timport java.text.SimpleDateFormat;\r\n");
    	sb.append("\timport java.util.ArrayList;\r\n");
    	sb.append("\timport java.util.Calendar;\r\n");
    	sb.append("\timport java.util.Date;\r\n");
    	sb.append("\timport java.util.List;\r\n");
    	sb.append("\timport java.util.Properties;\r\n");
    	sb.append("\t\r\n");
    	sb.append("\timport com.fasterxml.jackson.core.JsonParseException;\r\n");
    	sb.append("\timport com.fasterxml.jackson.core.JsonProcessingException;\r\n");
    	sb.append("\timport com.fasterxml.jackson.databind.JsonMappingException;\r\n");
    	sb.append("\timport com.fasterxml.jackson.databind.ObjectMapper;\r\n");
    	sb.append("\t\r\n");
    	sb.append("\tpublic class DBUtil {\r\n");
    	sb.append("\t	//连接对象\r\n");
    	sb.append("\t	//Statement 命令对象\r\n");
    	sb.append("\t	//打开连接\r\n");
    	sb.append("\t	//关闭连接\r\n");
    	sb.append("\t	//得到一个连接对象\r\n");
    	sb.append("\t	//查询（有参，无参）\r\n");
    	sb.append("\t	//修改（有参，无参）\r\n");
    	sb.append("\t	static Statement stmt = null;\r\n");
    	sb.append("\t	//驱动，服务器地址，登录用户名，密码	\r\n");
    	sb.append("\t	static String DBDRIVER;\r\n");
    	sb.append("\t	static String DBURL;\r\n");
    	sb.append("\t	static String DBUID;\r\n");
    	sb.append("\t	static String DBPWD;\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t	static {\r\n");
    	sb.append("\t		//先创建资源文件，扩展名为.properties\r\n");
    	sb.append("\t		//内容是以：dbuser=sa  格式\r\n");
    	sb.append("\t		\r\n");
    	sb.append("\t		Properties prop = new Properties();//先获取资源对象\r\n");
    	sb.append("\t		//创建输入流，读取资源文件\r\n");
    	sb.append("\t		InputStream in =Thread.currentThread().getContextClassLoader()\r\n");
    	sb.append("\t				.getResourceAsStream(\"jdbc.properties\");\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			prop.load(in);//加载\r\n");
    	sb.append("\t			DBDRIVER = prop.getProperty(\"DBDRIVER\");\r\n");
    	sb.append("\t			DBURL = prop.getProperty(\"DBURL\");\r\n");
    	sb.append("\t			DBUID = prop.getProperty(\"DBUID\");\r\n");
    	sb.append("\t			DBPWD = prop.getProperty(\"DBPWD\");\r\n");
    	sb.append("\t			//System.out.println(DBDRIVER);\r\n");
    	sb.append("\t		} catch (IOException e) {\r\n");
    	sb.append("\t			System.out.println(\"资源文件读取错误，请查看资源文件\");\r\n");
    	sb.append("\t		} \r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			in.close();\r\n");
    	sb.append("\t		} catch (IOException e) {\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	//打开连接\r\n");
    	sb.append("\t	 static  {\r\n");
    	sb.append("\t		//加载驱动\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			Class.forName(DBDRIVER);\r\n");
    	sb.append("\t		} catch (ClassNotFoundException e) {\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	//关闭连接\r\n");
    	sb.append("\t	public static void close(Connection conn) {\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			if(stmt!=null)\r\n");
    	sb.append("\t					stmt.close();\r\n");
    	sb.append("\t			if(conn!=null && !conn.isClosed())\r\n");
    	sb.append("\t				conn.close();\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * 用于关闭\r\n");
    	sb.append("\t	 * @param rs\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static void close(ResultSet rs) {\r\n");
    	sb.append("\t        Statement st = null;\r\n");
    	sb.append("\t        Connection con = null;\r\n");
    	sb.append("\t        try {\r\n");
    	sb.append("\t            try {\r\n");
    	sb.append("\t                if (rs != null) {\r\n");
    	sb.append("\t                    st = rs.getStatement();\r\n");
    	sb.append("\t                    rs.close();\r\n");
    	sb.append("\t                }\r\n");
    	sb.append("\t            } finally {\r\n");
    	sb.append("\t                try {\r\n");
    	sb.append("\t                    if (st != null) {\r\n");
    	sb.append("\t                        con = st.getConnection();\r\n");
    	sb.append("\t                        st.close();\r\n");
    	sb.append("\t                    }\r\n");
    	sb.append("\t                } finally {\r\n");
    	sb.append("\t                    if (con != null) {\r\n");
    	sb.append("\t                        con.close();\r\n");
    	sb.append("\t                    }\r\n");
    	sb.append("\t                }\r\n");
    	sb.append("\t            }\r\n");
    	sb.append("\t        } catch (SQLException e) {\r\n");
    	sb.append("\t            e.printStackTrace();\r\n");
    	sb.append("\t        }\r\n");
    	sb.append("\t    }\r\n");
    	sb.append("\t	//得到一个连接对象，当用户使用DBUtil无法解决个性问题时\r\n");
    	sb.append("\t	//可以通过本方法获得连接对象\r\n");
    	sb.append("\t	public static Connection getConnection() {\r\n");
    	sb.append("\t		 Connection conn = null;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			conn=DriverManager.getConnection(DBURL,DBUID,DBPWD);\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return conn;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t	//executeQuery\r\n");
    	sb.append("\t	//executeUpdate\r\n");
    	sb.append("\t	//execute\r\n");
    	sb.append("\t	//获得查询的数据集\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * 查询所有数据,转入查询字符串\r\n");
    	sb.append("\t	 * @param sql\r\n");
    	sb.append("\t	 * @return\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static ResultSet executeQuery(String sql) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			stmt = conn.createStatement();\r\n");
    	sb.append("\t			return stmt.executeQuery(sql);\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return null;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t	//修改表格内容\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * 进行修改,转入修改字符串\r\n");
    	sb.append("\t	 * @param sql\r\n");
    	sb.append("\t	 * @return\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static int executeUpdate(String sql) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		int result = 0;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			stmt = conn.createStatement();\r\n");
    	sb.append("\t			result = stmt.executeUpdate(sql);\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		} finally {\r\n");
    	sb.append("\t			close(conn);\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return result;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	//如果执行的查询或存储过程，会返回多个数据集，或多个执行成功记录数\r\n");
    	sb.append("\t	//可以调用本方法，返回的结果，\r\n");
    	sb.append("\t	//是一个List<ResultSet>或List<Integer>集合\r\n");
    	sb.append("\t	public static Object execute(String sql) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		boolean b=false;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			stmt = conn.createStatement();\r\n");
    	sb.append("\t			b = stmt.execute(sql);			\r\n");
    	sb.append("\t			//true,执行的是一个查询语句，我们可以得到一个数据集\r\n");
    	sb.append("\t			//false,执行的是一个修改语句，我们可以得到一个执行成功的记录数\r\n");
    	sb.append("\t			if(b){\r\n");
    	sb.append("\t				return stmt.getResultSet();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			else {\r\n");
    	sb.append("\t				return stmt.getUpdateCount();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		} finally {\r\n");
    	sb.append("\t			if(!b) {\r\n");
    	sb.append("\t				close(conn);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return null;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t	//\r\n");
    	sb.append("\t	//select * from student where name=? and sex=?\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * 进行条件查询,转入查询字符串,和参数\r\n");
    	sb.append("\t	 * @param sql\r\n");
    	sb.append("\t	 * @return\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static ResultSet executeQuery(String sql,Object[] in) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			PreparedStatement pst = conn.prepareStatement(sql);\r\n");
    	sb.append("\t			for(int i=0;i<in.length;i++)\r\n");
    	sb.append("\t				pst.setObject(i+1, in[i]);\r\n");
    	sb.append("\t			stmt = pst;//只是为了关闭命令对象pst\r\n");
    	sb.append("\t			return pst.executeQuery();\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return null;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t	public static int executeUpdate(String sql,Object[] in) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			PreparedStatement pst = conn.prepareStatement(sql);\r\n");
    	sb.append("\t			for(int i=0;i<in.length;i++)\r\n");
    	sb.append("\t				pst.setObject(i+1, in[i]);\r\n");
    	sb.append("\t			stmt = pst;//只是为了关闭命令对象pst\r\n");
    	sb.append("\t			return pst.executeUpdate();\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}finally {\r\n");
    	sb.append("\t			close(conn);\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return 0;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	public static Object execute(String sql,Object[] in) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		boolean b=false;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			PreparedStatement pst = conn.prepareStatement(sql);\r\n");
    	sb.append("\t			for(int i=0;i<in.length;i++)\r\n");
    	sb.append("\t				pst.setObject(i+1, in[i]);\r\n");
    	sb.append("\t			b = pst.execute();\r\n");
    	sb.append("\t			//true,执行的是一个查询语句，我们可以得到一个数据集\r\n");
    	sb.append("\t			//false,执行的是一个修改语句，我们可以得到一个执行成功的记录数\r\n");
    	sb.append("\t			if(b){\r\n");
    	sb.append("\t				System.out.println(\"----\");\r\n");
    	sb.append("\t				/*List<ResultSet> list = new ArrayList<ResultSet>();\r\n");
    	sb.append("\t				list.add(pst.getResultSet());\r\n");
    	sb.append("\t				while(pst.getMoreResults()) {\r\n");
    	sb.append("\t					list.add(pst.getResultSet());\r\n");
    	sb.append("\t				}*/\r\n");
    	sb.append("\t				return pst.getResultSet();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			else {\r\n");
    	sb.append("\t				System.out.println(\"****\");\r\n");
    	sb.append("\t				List<Integer> list = new ArrayList<Integer>();\r\n");
    	sb.append("\t				list.add(pst.getUpdateCount());\r\n");
    	sb.append("\t				while(pst.getMoreResults()) {\r\n");
    	sb.append("\t					list.add(pst.getUpdateCount());\r\n");
    	sb.append("\t				}\r\n");
    	sb.append("\t				return list;\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		} finally {\r\n");
    	sb.append("\t			if(!b) {\r\n");
    	sb.append("\t				System.out.println(\"====\");\r\n");
    	sb.append("\t				close(conn);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return null;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	//调用存储过程  proc_Insert(?,?,?)\r\n");
    	sb.append("\t	public static Object executeProcedure(String procName,Object[] in) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			procName = \"{call \"+procName+\"(\";\r\n");
    	sb.append("\t			String link=\"\";\r\n");
    	sb.append("\t			for(int i=0;i<in.length;i++) {\r\n");
    	sb.append("\t				procName+=link+\"?\";\r\n");
    	sb.append("\t				link=\",\";\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			procName+=\")}\";\r\n");
    	sb.append("\t			CallableStatement cstmt = conn.prepareCall(procName);\r\n");
    	sb.append("\t			for(int i=0;i<in.length;i++) {\r\n");
    	sb.append("\t				cstmt.setObject(i+1, in[i]);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			if(cstmt.execute())\r\n");
    	sb.append("\t			{\r\n");
    	sb.append("\t				return cstmt.getResultSet();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			else {\r\n");
    	sb.append("\t				return cstmt.getUpdateCount();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		\r\n");
    	sb.append("\t		return null;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t\r\n");
    	sb.append("\t	/*\r\n");
    	sb.append("\t	 * 调用存储过程，并有输出参数\r\n");
    	sb.append("\t	 * @procName ，存储过程名称：proc_Insert(?,?)\r\n");
    	sb.append("\t	 * @in ,输入参数集合\r\n");
    	sb.append("\t	 * @output,输出参数集合\r\n");
    	sb.append("\t	 * @type,输出参数类型集合\r\n");
    	sb.append("\t	 * */\r\n");
    	sb.append("\t	public static Object executeOutputProcedure(String procName,\r\n");
    	sb.append("\t			Object[] in,Object[] output,int[] type){\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		Object result = null;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			CallableStatement cstmt = conn.prepareCall(\"{call \"+procName+\"}\");\r\n");
    	sb.append("\t			//设置存储过程的参数值\r\n");
    	sb.append("\t			int i=0;\r\n");
    	sb.append("\t			for(;i<in.length;i++){//设置输入参数\r\n");
    	sb.append("\t				cstmt.setObject(i+1, in[i]);\r\n");
    	sb.append("\t				//print(i+1);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			int len = output.length+i;\r\n");
    	sb.append("\t			for(;i<len;i++){//设置输出参数\r\n");
    	sb.append("\t				cstmt.registerOutParameter(i+1,type[i-in.length]);\r\n");
    	sb.append("\t				//print(i+1);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			boolean b = cstmt.execute();\r\n");
    	sb.append("\t			//获取输出参数的值\r\n");
    	sb.append("\t			for(i=in.length;i<output.length+in.length;i++)\r\n");
    	sb.append("\t				output[i-in.length] = cstmt.getObject(i+1);\r\n");
    	sb.append("\t			if(b) {\r\n");
    	sb.append("\t				result = cstmt.getResultSet();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			else {\r\n");
    	sb.append("\t				result = cstmt.getUpdateCount();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		} catch (SQLException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return result;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	public static String toJson(Object obj){\r\n");
    	sb.append("\t		String reuqest=null;\r\n");
    	sb.append("\t		//对象映射\r\n");
    	sb.append("\t		ObjectMapper mapper=new ObjectMapper();\r\n");
    	sb.append("\t		//设置时间格式\r\n");
    	sb.append("\t		SimpleDateFormat dateFormat=new SimpleDateFormat(\"yyyy年MM月dd日\");\r\n");
    	sb.append("\t		mapper.setDateFormat(dateFormat);\r\n");
    	sb.append("\t			try {\r\n");
    	sb.append("\t				reuqest=mapper.writeValueAsString(obj);\r\n");
    	sb.append("\t			} catch (JsonProcessingException e) {\r\n");
    	sb.append("\t				// TODO Auto-generated catch block\r\n");
    	sb.append("\t				e.printStackTrace();\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t		return reuqest;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	public static <T> T toObject(String src,Class<T> valueType){\r\n");
    	sb.append("\t		T request=null;\r\n");
    	sb.append("\t			//对象反射\r\n");
    	sb.append("\t		  ObjectMapper mapper=new ObjectMapper();\r\n");
    	sb.append("\t		  try {\r\n");
    	sb.append("\t			request=mapper.readValue(src, valueType);\r\n");
    	sb.append("\t		} catch (JsonParseException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		} catch (JsonMappingException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		} catch (IOException e) {\r\n");
    	sb.append("\t			// TODO Auto-generated catch block\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t		return request;\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * 字符串转入时间格式,返回Date类型\r\n");
    	sb.append("\t	 * @param date_str\r\n");
    	sb.append("\t	 * @return\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static Date date(String date_str) {\r\n");
    	sb.append("\t        try {\r\n");
    	sb.append("\t            Calendar zcal = Calendar.getInstance();//日期类\r\n");
    	sb.append("\t            Timestamp timestampnow = new Timestamp(zcal.getTimeInMillis());//转换成正常的日期格式\r\n");
    	sb.append("\t            SimpleDateFormat formatter = new SimpleDateFormat(\"yyyy-MM-dd\");//改为需要的东西\r\n");
    	sb.append("\t            ParsePosition pos = new ParsePosition(0);\r\n");
    	sb.append("\t            java.util.Date current = formatter.parse(date_str, pos);\r\n");
    	sb.append("\t            timestampnow = new Timestamp(current.getTime());\r\n");
    	sb.append("\t            return timestampnow;\r\n");
    	sb.append("\t        }\r\n");
    	sb.append("\t        catch (NullPointerException e) {\r\n");
    	sb.append("\t            return null;\r\n");
    	sb.append("\t        }\r\n");
    	sb.append("\t    }\r\n");
    	sb.append("\t}\r\n");
    	return sb.toString();
    }
    /**
     * 生成所有的方法
     * @time 2018年7月14日下午5:54:52
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
    	
    	//findById
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 根据id查询,返回" + initcap(tablename) + ",参数int id   根据id查询\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic " + initcap(tablename) + " findById(" + sqlType2JavaType(colTypes[0]) + " " + colnames[0] + ") {\r\n");
    	sb.append("\t\tString sql = \"select * from " + initcap(tablename) + " where " + colnames[0] + "=?\";\r\n");
    	sb.append("\t\tObject[] in = {" + colnames[0] + "};\r\n");
    	sb.append("\t\tResultSet rs = DBUtil.executeQuery(sql, in);\r\n");
    	sb.append("\t\t" + initcap(tablename) + " model = null;\r\n");
    	sb.append("\t\ttry {\r\n");
    	sb.append("\t\t\tif(rs.next()) {\r\n");
    	sb.append("\t\t\t\tmodel = new " + initcap(tablename) + "(\r\n");
    	String link="";
    	for (int i = 0; i < colnames.length; i++) {
        	sb.append(link+"\t\t\t\t\trs.get" + sqlTypeJavaType(colTypes[i]) + "(\"" + colnames[i] + "\")"); 
        	link=",\r\n";
    	}
    	sb.append(");\r\n");
    	sb.append("\t\t\t}\r\n");
    	sb.append("\t\t\tDBUtil.close(rs);\r\n");
    	sb.append("\t\t} catch (SQLException e) {\r\n");
    	sb.append("\t\t\te.printStackTrace();\r\n");
    	sb.append("\t\t}\r\n");
    	sb.append("\t\treturn model;\r\n");
    	sb.append("\t}\r\n");  

    	//findAll
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 查询全部,返回List<" + initcap(tablename) + ">\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> findAll() {\r\n");
    	sb.append("\t\tString sql = \"select * from " + initcap(tablename) + "\";\r\n");
    	sb.append("\t\tResultSet rs = DBUtil.executeQuery(sql);\r\n");
    	sb.append("\t\tList<" + initcap(tablename) + "> modelList = new ArrayList<" + initcap(tablename) + ">();\r\n");
    	sb.append("\t\t" + initcap(tablename) + " model = null;\r\n");
    	sb.append("\t\ttry {\r\n");
    	sb.append("\t\t\twhile(rs.next()) {\r\n");
    	sb.append("\t\t\t\tmodel = new " + initcap(tablename) + "(\r\n");
    	
    	link="";
    	for (int i = 0; i < colnames.length; i++) {
        	sb.append(link+"\t\t\t\t\trs.get" + sqlTypeJavaType(colTypes[i]) + "(\"" + colnames[i] + "\")"); 
        	link=",\r\n";
    	}
    	sb.append(");\r\n");
    	sb.append("\t\t\t\tmodelList.add(model);\r\n");
    	sb.append("\t\t\t}\r\n");
    	sb.append("\t\t\tDBUtil.close(rs);\r\n");
    	sb.append("\t\t} catch (SQLException e) {\r\n");
    	sb.append("\t\t\te.printStackTrace();\r\n");
    	sb.append("\t\t}\r\n");
    	sb.append("\t\treturn modelList;\r\n");
    	sb.append("\t}\r\n"); 
    	
    	//分页Paging
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 分页查询,返回List<" + initcap(tablename) + ">,int page,int limit 第一个参数是第几页,第二个参数是多少条数据\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> Paging(int page,int limit) {\r\n");
		sb.append("\t//开始\r\n");
		sb.append("\t//第几页-1乘每页多少条数据+1\r\n");
		sb.append("\tint start=(page-1)*limit+1;//公式用于获取从哪里开始\r\n");
		sb.append("\t//结束\r\n");
		sb.append("\t//第几页乘每页多少条数据\r\n");
		sb.append("\tint end=page*limit;\r\n");
		sb.append("\tString sql = \"select * from(SELECT *,ROW_NUMBER() over(order by " + colnames[0] + " desc) row FROM [" + initcap(tablename) + "]) t where t.row>=\"+start+\" and t.row<=\"+end;\r\n");
    	sb.append("\t\tResultSet rs = DBUtil.executeQuery(sql);\r\n");
    	sb.append("\t\tList<" + initcap(tablename) + "> modelList = new ArrayList<" + initcap(tablename) + ">();\r\n");
    	sb.append("\t\t" + initcap(tablename) + " model = null;\r\n");
    	sb.append("\t\ttry {\r\n");
    	sb.append("\t\t\twhile(rs.next()) {\r\n");
    	sb.append("\t\t\t\tmodel = new " + initcap(tablename) + "(\r\n");
    	
    	link="";
    	for (int i = 0; i < colnames.length; i++) {
        	sb.append(link+"\t\t\t\t\trs.get" + sqlTypeJavaType(colTypes[i]) + "(\"" + colnames[i] + "\")"); 
        	link=",\r\n";
    	}
    	sb.append(");\r\n");
    	sb.append("\t\t\t\tmodelList.add(model);\r\n");
    	sb.append("\t\t\t}\r\n");
    	sb.append("\t\t\tDBUtil.close(rs);\r\n");
    	sb.append("\t\t} catch (SQLException e) {\r\n");
    	sb.append("\t\t\te.printStackTrace();\r\n");
    	sb.append("\t\t}\r\n");
    	sb.append("\t\treturn modelList;\r\n");
    	sb.append("\t}\r\n"); 
    	//insert
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 添加方法,返回是否删除成功\r\n");
   	 	sb.append("\t* @param\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int insert(" + initcap(tablename) + " model) {\r\n");
    	link="";
    	String values="";
    	for (int i = 0; i < colnames.length-1; i++) {
        	values+=link+"?";
        	link=",";
    	}
    	sb.append("\t\tString sql = \"insert into " + initcap(tablename) + " values("+values+") \";\r\n");
    	sb.append("\t\tObject[] in = {");
    	link="";
    	for (int i = 1; i < colnames.length; i++) {
        	sb.append(link+"model.get"+initcap(colnames[i])+"()"); 
        	link=",";
    	}    	
    	sb.append("};\r\n");
    	sb.append("\t\treturn DBUtil.executeUpdate(sql, in);\r\n");
    	sb.append("\t}\r\n"); 

    	//update
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 修改方法,返回是否删除成功,参数传入对象\r\n");
   	 	sb.append("\t* @param model\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int update(" + initcap(tablename) + " model) {\r\n");
    	sb.append("\t\tString sql = \"update " + initcap(tablename) + " set ");
    	
    	link="";
    	for (int i = 1; i < colnames.length; i++) {
        	sb.append(link+colnames[i]+"=?"); 
        	link=",";
    	} 
    	sb.append(" where "+colnames[0]+"=? \";\r\n");
    	sb.append("\t\tObject[] in = {");
    	link="";
    	for (int i = 1; i < colnames.length; i++) {
        	sb.append(link+"model.get"+initcap(colnames[i])+"()"); 
        	link=",";
    	}
    	sb.append(link+"model.get"+initcap(colnames[0])+"()");
    	sb.append("};\r\n");
    	sb.append("\t\treturn DBUtil.executeUpdate(sql, in);\r\n");
    	sb.append("\t}\r\n"); 
    	

    	//delete
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 删除方法,返回是否删除成功,参数传入id 根据id进行删除\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int delete(" + sqlType2JavaType(colTypes[0]) + " " + colnames[0] + ") {\r\n");
    	sb.append("\t\tString sql = \"delete from " + initcap(tablename) + " where " + colnames[0] + "=? \";\r\n");
    	sb.append("\t\tObject[] in = {" + colnames[0] + "};\r\n");
    	sb.append("\t\treturn DBUtil.executeUpdate(sql, in);\r\n");
    	sb.append("\t}\r\n"); 
    	
    	//getcount
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 获取行数方法,返回多少行\r\n");
   	 	sb.append("\t* @param\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int getcount(){\r\n");
		sb.append("\tint count=0;\r\n");
		sb.append("\tString sql = \"select count(*) from " + initcap(tablename) + "\";\r\n");
		sb.append("\tResultSet rs = DBUtil.executeQuery(sql);\r\n");
		sb.append("\ttry {\r\n");
		sb.append("\t	while(rs.next()) {\r\n");
		sb.append("\t		count=rs.getInt(1);\r\n");
		sb.append("\t	}\r\n");
		sb.append("\t	DBUtil.close(rs);\r\n");
		sb.append("\t} catch (SQLException e) {\r\n");
		sb.append("\t	e.printStackTrace();\r\n");
		sb.append("\t}\r\n");
		sb.append("\treturn count;\r\n");
		sb.append("\t}	\r\n");
    }

    

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
    

    private String sqlTypeJavaType(String sqlType) {
        if (sqlType.equalsIgnoreCase("bit")) {
            return "Boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "Byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "Short";
        } else if (sqlType.equalsIgnoreCase("int")) {
            return "Int";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "Long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "Float";
        } else if (sqlType.equalsIgnoreCase("decimal")
                || sqlType.equalsIgnoreCase("numeric")
                || sqlType.equalsIgnoreCase("real")) {
            return "Double";
        } else if (sqlType.equalsIgnoreCase("money")
                || sqlType.equalsIgnoreCase("smallmoney")) {
            return "Double";
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
    	//生成DBUtil
    	 Bean("DBUtil", DBUtil(), "com/dao");
        AutoCreateDao auto = new AutoCreateDao();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/dao");

    }
    /**
     * 一键执行
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	//生成DBUtil
   	 Bean("DBUtil", DBUtil(), "com/dao");
       AutoCreateDao auto = new AutoCreateDao();
       List<String> list = auto.TBlist();
       auto.GenEntity(list, "com/dao");
    }

}