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
 * �������ݿ��ṹ �Զ�����java Bean
 * 
 * @author DLHT 2017��7��14������5:00:28 AutoCreateClass.java DLHT
 */
public class AutoCreateVo {
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

    private boolean f_util = false; // �Ƿ���Ҫ�����java.util.*

    private boolean f_sql = false; // �Ƿ���Ҫ�����java.sql.*
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
        conn = DriverManager.getConnection(DBURL,  DBUID, DBPWD);

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
     * ��������(����ʵ�����������)
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
     * �������еķ���
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
     * �����������
     * 
     * @return
     */
    private void processAllAttrs(StringBuffer sb) {
        for (int i = 0; i < colnames.length; i++) {
            sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + colnames[i] + ";\r\n");
        }
    }
    /**
     * �������ɹ��캯��
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

    public static void main(String[] args) throws Exception {
        AutoCreateVo auto = new AutoCreateVo();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/vo");

    }
    /**
     * һ��ִ��
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	 AutoCreateVo auto = new AutoCreateVo();
         List<String> list = auto.TBlist();
         auto.GenEntity(list, "com/vo");
    }
}