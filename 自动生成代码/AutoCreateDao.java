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
public class AutoCreateDao {

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
     * @time 2017��7��14������5:54:52
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
     * ��������(����ʵ�����������)
     */
    private String parse() {
        StringBuffer sb = new StringBuffer();
        //�����
        sb.append("import java.sql.*;\r\n");
        sb.append("import java.util.*;\r\n");
        //�����Ӧʵ���
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
     * ����DBUtil����
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
    	sb.append("\t	//���Ӷ���\r\n");
    	sb.append("\t	//Statement �������\r\n");
    	sb.append("\t	//������\r\n");
    	sb.append("\t	//�ر�����\r\n");
    	sb.append("\t	//�õ�һ�����Ӷ���\r\n");
    	sb.append("\t	//��ѯ���вΣ��޲Σ�\r\n");
    	sb.append("\t	//�޸ģ��вΣ��޲Σ�\r\n");
    	sb.append("\t	static Statement stmt = null;\r\n");
    	sb.append("\t	//��������������ַ����¼�û���������	\r\n");
    	sb.append("\t	static String DBDRIVER;\r\n");
    	sb.append("\t	static String DBURL;\r\n");
    	sb.append("\t	static String DBUID;\r\n");
    	sb.append("\t	static String DBPWD;\r\n");
    	sb.append("\t	\r\n");
    	sb.append("\t	static {\r\n");
    	sb.append("\t		//�ȴ�����Դ�ļ�����չ��Ϊ.properties\r\n");
    	sb.append("\t		//�������ԣ�dbuser=sa  ��ʽ\r\n");
    	sb.append("\t		\r\n");
    	sb.append("\t		Properties prop = new Properties();//�Ȼ�ȡ��Դ����\r\n");
    	sb.append("\t		//��������������ȡ��Դ�ļ�\r\n");
    	sb.append("\t		InputStream in =Thread.currentThread().getContextClassLoader()\r\n");
    	sb.append("\t				.getResourceAsStream(\"jdbc.properties\");\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			prop.load(in);//����\r\n");
    	sb.append("\t			DBDRIVER = prop.getProperty(\"DBDRIVER\");\r\n");
    	sb.append("\t			DBURL = prop.getProperty(\"DBURL\");\r\n");
    	sb.append("\t			DBUID = prop.getProperty(\"DBUID\");\r\n");
    	sb.append("\t			DBPWD = prop.getProperty(\"DBPWD\");\r\n");
    	sb.append("\t			//System.out.println(DBDRIVER);\r\n");
    	sb.append("\t		} catch (IOException e) {\r\n");
    	sb.append("\t			System.out.println(\"��Դ�ļ���ȡ������鿴��Դ�ļ�\");\r\n");
    	sb.append("\t		} \r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			in.close();\r\n");
    	sb.append("\t		} catch (IOException e) {\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	//������\r\n");
    	sb.append("\t	 static  {\r\n");
    	sb.append("\t		//��������\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			Class.forName(DBDRIVER);\r\n");
    	sb.append("\t		} catch (ClassNotFoundException e) {\r\n");
    	sb.append("\t			e.printStackTrace();\r\n");
    	sb.append("\t		}\r\n");
    	sb.append("\t	}\r\n");
    	sb.append("\t	//�ر�����\r\n");
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
    	sb.append("\t	 * ���ڹر�\r\n");
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
    	sb.append("\t	//�õ�һ�����Ӷ��󣬵��û�ʹ��DBUtil�޷������������ʱ\r\n");
    	sb.append("\t	//����ͨ��������������Ӷ���\r\n");
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
    	sb.append("\t	//��ò�ѯ�����ݼ�\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * ��ѯ��������,ת���ѯ�ַ���\r\n");
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
    	sb.append("\t	//�޸ı������\r\n");
    	sb.append("\t	/**\r\n");
    	sb.append("\t	 * �����޸�,ת���޸��ַ���\r\n");
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
    	sb.append("\t	//���ִ�еĲ�ѯ��洢���̣��᷵�ض�����ݼ�������ִ�гɹ���¼��\r\n");
    	sb.append("\t	//���Ե��ñ����������صĽ����\r\n");
    	sb.append("\t	//��һ��List<ResultSet>��List<Integer>����\r\n");
    	sb.append("\t	public static Object execute(String sql) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		boolean b=false;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			stmt = conn.createStatement();\r\n");
    	sb.append("\t			b = stmt.execute(sql);			\r\n");
    	sb.append("\t			//true,ִ�е���һ����ѯ��䣬���ǿ��Եõ�һ�����ݼ�\r\n");
    	sb.append("\t			//false,ִ�е���һ���޸���䣬���ǿ��Եõ�һ��ִ�гɹ��ļ�¼��\r\n");
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
    	sb.append("\t	 * ����������ѯ,ת���ѯ�ַ���,�Ͳ���\r\n");
    	sb.append("\t	 * @param sql\r\n");
    	sb.append("\t	 * @return\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static ResultSet executeQuery(String sql,Object[] in) {\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			PreparedStatement pst = conn.prepareStatement(sql);\r\n");
    	sb.append("\t			for(int i=0;i<in.length;i++)\r\n");
    	sb.append("\t				pst.setObject(i+1, in[i]);\r\n");
    	sb.append("\t			stmt = pst;//ֻ��Ϊ�˹ر��������pst\r\n");
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
    	sb.append("\t			stmt = pst;//ֻ��Ϊ�˹ر��������pst\r\n");
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
    	sb.append("\t			//true,ִ�е���һ����ѯ��䣬���ǿ��Եõ�һ�����ݼ�\r\n");
    	sb.append("\t			//false,ִ�е���һ���޸���䣬���ǿ��Եõ�һ��ִ�гɹ��ļ�¼��\r\n");
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
    	sb.append("\t	//���ô洢����  proc_Insert(?,?,?)\r\n");
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
    	sb.append("\t	 * ���ô洢���̣������������\r\n");
    	sb.append("\t	 * @procName ���洢�������ƣ�proc_Insert(?,?)\r\n");
    	sb.append("\t	 * @in ,�����������\r\n");
    	sb.append("\t	 * @output,�����������\r\n");
    	sb.append("\t	 * @type,����������ͼ���\r\n");
    	sb.append("\t	 * */\r\n");
    	sb.append("\t	public static Object executeOutputProcedure(String procName,\r\n");
    	sb.append("\t			Object[] in,Object[] output,int[] type){\r\n");
    	sb.append("\t		Connection conn = getConnection();\r\n");
    	sb.append("\t		Object result = null;\r\n");
    	sb.append("\t		try {\r\n");
    	sb.append("\t			CallableStatement cstmt = conn.prepareCall(\"{call \"+procName+\"}\");\r\n");
    	sb.append("\t			//���ô洢���̵Ĳ���ֵ\r\n");
    	sb.append("\t			int i=0;\r\n");
    	sb.append("\t			for(;i<in.length;i++){//�����������\r\n");
    	sb.append("\t				cstmt.setObject(i+1, in[i]);\r\n");
    	sb.append("\t				//print(i+1);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			int len = output.length+i;\r\n");
    	sb.append("\t			for(;i<len;i++){//�����������\r\n");
    	sb.append("\t				cstmt.registerOutParameter(i+1,type[i-in.length]);\r\n");
    	sb.append("\t				//print(i+1);\r\n");
    	sb.append("\t			}\r\n");
    	sb.append("\t			boolean b = cstmt.execute();\r\n");
    	sb.append("\t			//��ȡ���������ֵ\r\n");
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
    	sb.append("\t		//����ӳ��\r\n");
    	sb.append("\t		ObjectMapper mapper=new ObjectMapper();\r\n");
    	sb.append("\t		//����ʱ���ʽ\r\n");
    	sb.append("\t		SimpleDateFormat dateFormat=new SimpleDateFormat(\"yyyy��MM��dd��\");\r\n");
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
    	sb.append("\t			//������\r\n");
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
    	sb.append("\t	 * �ַ���ת��ʱ���ʽ,����Date����\r\n");
    	sb.append("\t	 * @param date_str\r\n");
    	sb.append("\t	 * @return\r\n");
    	sb.append("\t	 */\r\n");
    	sb.append("\t	public static Date date(String date_str) {\r\n");
    	sb.append("\t        try {\r\n");
    	sb.append("\t            Calendar zcal = Calendar.getInstance();//������\r\n");
    	sb.append("\t            Timestamp timestampnow = new Timestamp(zcal.getTimeInMillis());//ת�������������ڸ�ʽ\r\n");
    	sb.append("\t            SimpleDateFormat formatter = new SimpleDateFormat(\"yyyy-MM-dd\");//��Ϊ��Ҫ�Ķ���\r\n");
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
     * �������еķ���
     * @time 2018��7��14������5:54:52
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
    	
    	//findById
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ����id��ѯ,����" + initcap(tablename) + ",����int id   ����id��ѯ\r\n");
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
   	 	sb.append("\t* ��ѯȫ��,����List<" + initcap(tablename) + ">\r\n");
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
    	
    	//��ҳPaging
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ��ҳ��ѯ,����List<" + initcap(tablename) + ">,int page,int limit ��һ�������ǵڼ�ҳ,�ڶ��������Ƕ���������\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> Paging(int page,int limit) {\r\n");
		sb.append("\t//��ʼ\r\n");
		sb.append("\t//�ڼ�ҳ-1��ÿҳ����������+1\r\n");
		sb.append("\tint start=(page-1)*limit+1;//��ʽ���ڻ�ȡ�����￪ʼ\r\n");
		sb.append("\t//����\r\n");
		sb.append("\t//�ڼ�ҳ��ÿҳ����������\r\n");
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
   	 	sb.append("\t* ��ӷ���,�����Ƿ�ɾ���ɹ�\r\n");
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
   	 	sb.append("\t* �޸ķ���,�����Ƿ�ɾ���ɹ�,�����������\r\n");
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
   	 	sb.append("\t* ɾ������,�����Ƿ�ɾ���ɹ�,��������id ����id����ɾ��\r\n");
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
   	 	sb.append("\t* ��ȡ��������,���ض�����\r\n");
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
    	//����DBUtil
    	 Bean("DBUtil", DBUtil(), "com/dao");
        AutoCreateDao auto = new AutoCreateDao();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/dao");

    }
    /**
     * һ��ִ��
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	//����DBUtil
   	 Bean("DBUtil", DBUtil(), "com/dao");
       AutoCreateDao auto = new AutoCreateDao();
       List<String> list = auto.TBlist();
       auto.GenEntity(list, "com/dao");
    }

}