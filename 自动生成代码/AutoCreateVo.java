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
 * 根据数据库表结构 自动生成java Bean
 * 
 * @author DLHT 2017年7月14日下午5:00:28 AutoCreateClass.java DLHT
 */
public class AutoCreateVo {
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

    private boolean f_util = false; // 是否需要导入包java.util.*

    private boolean f_sql = false; // 是否需要导入包java.sql.*
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
        conn = DriverManager.getConnection(DBURL,  DBUID, DBPWD);

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
                if (colTypes[i].equalsIgnoreCase("datetime")) {
                    f_util = true;
                }
                if (colTypes[i].equalsIgnoreCase("image")
                        || colTypes[i].equalsIgnoreCase("text")) {
                    f_sql = true;
                }
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
        if (f_util) {
            sb.append("import java.util.Date;\r\n");
        }
        if (f_sql) {
            sb.append("import java.sql.*;\r\n\r\n\r\n");
        }
        sb.append("public class " + initcap(tablename) + " {\r\n");
        processAllAttrs(sb);
        processConstructor(sb,initcap(tablename));
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
     * 生成所有的方法
     * 
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
        for (int i = 0; i < colnames.length; i++) {
            sb.append("\tpublic void set" + initcap(colnames[i]) + "("
                    + sqlType2JavaType(colTypes[i]) + " " + colnames[i]
                    + "){\r\n");
            sb.append("\t\tthis." + colnames[i] + " = " + colnames[i] + ";\r\n");
            sb.append("\t}\r\n");

            sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get"
                    + initcap(colnames[i]) + "(){\r\n");
            sb.append("\t\treturn " + colnames[i] + ";\r\n");
            sb.append("\t}\r\n");
        }
        
    }

    /**
     * 解析输出属性
     * 
     * @return
     */
    private void processAllAttrs(StringBuffer sb) {
        for (int i = 0; i < colnames.length; i++) {
            sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + colnames[i] + ";\r\n");
        }
    }
    /**
     * 解析生成构造函数
     * 
     * @return
     */
    private void processConstructor(StringBuffer sb,String tableName) {
    	sb.append("\tpublic "+tableName+"(){}\r\n");
    	sb.append("\tpublic "+tableName+"(");
    	String link="";
        for (int i = 0; i < colnames.length; i++) {
            sb.append(link + sqlType2JavaType(colTypes[i]) + " " + colnames[i] );
            link=",";
        }
    	sb.append("){\r\n");
        for (int i = 0; i < colnames.length; i++) {
        	sb.append("\t\tthis."+colnames[i]+"="+colnames[i]+";\r\n");
        }
    	sb.append("\t}\r\n");
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

    public static void main(String[] args) throws Exception {
        AutoCreateVo auto = new AutoCreateVo();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/vo");

    }
    /**
     * 一键执行
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	 AutoCreateVo auto = new AutoCreateVo();
         List<String> list = auto.TBlist();
         auto.GenEntity(list, "com/vo");
    }
}