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
        // �������ݿ� ��������
        Class.forName(DBDRIVER);
        //��������
        Connection con = DriverManager.getConnection(DBURL, DBUID, DBPWD);
        //�õ���Ľṹ��Ϣ�������ֶ������ֶ�����
        DatabaseMetaData md = con.getMetaData();
        //�����������
        List<String> list = null;
        /**
         * ������
		 *	catalog - ������ƣ���Ϊ�洢�����ݿ��У�����������ƥ��������ơ��ò���Ϊ "" �����û������������Ϊ null ���ʾ��������Ʋ�Ӧ������С������Χ
		 *	schemaPattern - ģʽ���Ƶ�ģʽ����Ϊ�洢�����ݿ��У�����������ƥ��ģʽ���ơ��ò���Ϊ "" �������Щû��ģʽ��������Ϊ null ���ʾ��ģʽ���Ʋ�Ӧ������С������Χ
		 *	tableNamePattern - ������ģʽ����Ϊ�洢�����ݿ��У�����������ƥ�������
		 *	types - Ҫ�����ı�������ɵ��б�null ��ʾ������������
         * 
         */
        ResultSet rs = md.getTables(null, null, null, null);
        //���ǿյľ�new���µĶ���
        
        if (rs!= null) {
            list = new ArrayList<String>();
        }
        //ѭ����ȡ
        while (rs.next()) {
        	//@TABLE_NAME ���ݱ���
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

        // �������ݿ� ��������
        Class.forName(DBDRIVER);
        
        conn = DriverManager.getConnection(DBURL, DBUID, DBPWD);
        //ѭ����
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
     * ��������(����ʵ�����������)
     */
    private String parse() {
        StringBuffer sb = new StringBuffer();
        //�����Ӧʵ���
        sb.append("import java.util.*;\r\n");
        sb.append("import com.dao." + initcap(tablename) + "DAO;\r\n");
        sb.append("import com.vo." + initcap(tablename) + ";\r\n");
        sb.append("public class " + initcap(tablename) + "BO {\r\n");
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
     * �������еķ���
     * 
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
    	
    	//findById
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ����id��ѯ,����" + initcap(tablename) + ",����int id   ����id��ѯ\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\t" + initcap(tablename) + "DAO dao=new "+ initcap(tablename) + "DAO(); \r\n");
    	sb.append("\tpublic " + initcap(tablename) + " findById(" + sqlType2JavaType(colTypes[0]) + " " + colnames[0] + ") {\r\n");
    	sb.append("\t\treturn dao.findById(" + colnames[0] + ");\r\n");
    	sb.append("\t}\r\n");  

    	//findAll
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ��ѯȫ��,����List<" + initcap(tablename) + ">\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> findAll() {\r\n");
    	sb.append("\t\treturn dao.findAll();\r\n");
    	sb.append("\t}\r\n"); 

    	//��ҳPaging
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ��ҳ��ѯ,����List<" + initcap(tablename) + ">,int page,int limit ��һ�������ǵڼ�ҳ,�ڶ��������Ƕ���������\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic List<" + initcap(tablename) + "> Paging(int page,int limit) {\r\n");
    	sb.append("\t\treturn dao.Paging(page,limit);\r\n");
    	sb.append("\t}\r\n"); 
    	//insert
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ��ӷ���,�����Ƿ�ɾ���ɹ�\r\n");
   	 	sb.append("\t* @param\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int insert(" + initcap(tablename) + " model) {\r\n");
    	sb.append("\t\treturn dao.insert(model);\r\n");
    	sb.append("\t}\r\n"); 
    	//update
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* �޸ķ���,�����Ƿ�ɾ���ɹ�,�����������\r\n");
   	 	sb.append("\t* @param model\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int update(" + initcap(tablename) + " model) {\r\n");
    	sb.append("\t\treturn dao.update(model);\r\n");
    	sb.append("\t}\r\n"); 																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																						
    	//delete
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ɾ������,�����Ƿ�ɾ���ɹ�,��������id ����id����ɾ��\r\n");
   	 	sb.append("\t* @param id\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int delete(" + sqlType2JavaType(colTypes[0]) + " " + colnames[0] + ") {\r\n");
    	sb.append("\t\treturn dao.delete(" + colnames[0] + ");\r\n");
    	sb.append("\t}\r\n"); 
    	//getcount
    	sb.append("\t/**\r\n");
   	 	sb.append("\t* ��ȡ��������,���ض�����\r\n");
   	 	sb.append("\t* @param\r\n");
   	 	sb.append("\t* @return\r\n");
   	 	sb.append("\t*/\r\n");
    	sb.append("\tpublic int getcount(){\r\n");
    	sb.append("\t\treturn dao.getcount();\r\n");
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
    	AutoCreateBo auto = new AutoCreateBo();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/bo");

    }
    /**
     * һ��ִ��
     * @throws Exception
     */
    public static void Auto() throws Exception{
    	AutoCreateBo auto = new AutoCreateBo();
        List<String> list = auto.TBlist();
        auto.GenEntity(list, "com/bo");

    }
}
