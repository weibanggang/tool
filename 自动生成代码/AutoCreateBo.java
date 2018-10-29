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

public class AutoCreateBo {

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
        // 访问数据库 加载驱动
        Class.forName(DBDRIVER);
        //进行连接
        Connection con = DriverManager.getConnection(DBURL, DBUID, DBPWD);
        //得到表的结构信息，比如字段数、字段名等
        DatabaseMetaData md = con.getMetaData();
        //定义接收数组
        List<String> list = null;
        /**
         * 参数：
		 *	catalog - 类别名称，因为存储在数据库中，所以它必须匹配类别名称。该参数为 "" 则检索没有类别的描述，为 null 则表示该类别名称不应用于缩小搜索范围
		 *	schemaPattern - 模式名称的模式，因为存储在数据库中，所以它必须匹配模式名称。该参数为 "" 则检索那些没有模式的描述，为 null 则表示该模式名称不应用于缩小搜索范围
		 *	tableNamePattern - 表名称模式，因为存储在数据库中，所以它必须匹配表名称
		 *	types - 要包括的表类型组成的列表，null 表示返回所有类型
         * 
         */
        ResultSet rs = md.getTables(null, null, null, null);
        //不是空的就new个新的对象
        
        if (rs!= null) {
            list = new ArrayList<String>();
        }
        //循环读取
        while (rs.next()) {
        	//@TABLE_NAME 数据表名
            list.add(rs.getString("TABLE_NAME"));
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

        // 访问数据库 加载驱动
        Class.forName(DBDRIVER);
        
        conn = DriverManager.getConnection(DBURL, DBUID, DBPWD);
        //循环表
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
                /*
                if (colTypes[i].equalsIgnoreCase("datetime")) {
                    f_util = true;
                }
                if (colTypes[i].equalsIgnoreCase("image")
                        || colTypes[i].equalsIgnoreCase("text")) {
                    f_sql = true;
                }*/
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
        //导入对应实体包
        sb.append("import java.util.*;\r\n");
        sb.append("import com.dao." + initcap(tablename) + "DAO;\r\n");
        sb.append("import com.vo." + initcap(tablename) + ";\r\n");
        sb.append("public class " + initcap(tablename) + "BO {\r\n");
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
        String fileName = folder + className + "BO.java";

        try {
            File newbo = new File(fileName);
            FileWriter fw = new FileWriter(newbo);
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
    	
    	//findById
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 根据id查询,返回" + initcap(tablename) + ",参数int id   根据id查询\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\t" + initcap(tablename) + "DAO dao=new "+ initcap(tablename) + "DAO(); \r\n");
    	sb.append("\tpublic " + initcap(tablename) + " findById(" + sqlType2JavaType(colTypes[0]) + " " + colnames[0] + ") {\r\n");
    	sb.append("\t\treturn dao.findById(" + colnames[0] + ");\r\n");
    	sb.append("\t}\r\n");  

    	//findAll
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 查询全部,返回List<" + initcap(tablename) + ">\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> findAll() {\r\n");
    	sb.append("\t\treturn dao.findAll();\r\n");
    	sb.append("\t}\r\n"); 

    	//分页Paging
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 分页查询,返回List<" + initcap(tablename) + ">,int page,int limit 第一个参数是第几页,第二个参数是多少条数据\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> Paging(int page,int limit) {\r\n");
    	sb.append("\t\treturn dao.Paging(page,limit);\r\n");
    	sb.append("\t}\r\n"); 
    	//insert
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 添加方法,返回是否删除成功\r\n");
   	 	sb.append("\t* @param\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int insert(" + initcap(tablename) + " model) {\r\n");
    	sb.append("\t\treturn dao.insert(model);\r\n");
    	sb.append("\t}\r\n"); 
    	//update
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 修改方法,返回是否删除成功,参数传入对象\r\n");
   	 	sb.append("\t* @param model\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int update(" + initcap(tablename) + " model) {\r\n");
    	sb.append("\t\treturn dao.update(model);\r\n");
    	sb.append("\t}\r\n"); 																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																						
    	//delete
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 删除方法,返回是否删除成功,参数传入id 根据id进行删除\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int delete(" + sqlType2JavaType(colTypes[0]) + " " + colnames[0] + ") {\r\n");
    	sb.append("\t\treturn dao.delete(" + colnames[0] + ");\r\n");
    	sb.append("\t}\r\n"); 
    	//getcount
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* 获取行数方法,返回多少行\r\n");
   	 	sb.append("\t* @param\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int getcount(){\r\n");
    	sb.append("\t\treturn dao.getcount();\r\n");
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
    	AutoCreateBo auto = new AutoCreateBo();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/bo");

    }
    /**
     * 一键执行
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	AutoCreateBo auto = new AutoCreateBo();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/bo");

    }
}
