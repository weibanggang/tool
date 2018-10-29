package	com.dao;
	import java.io.IOException;
	import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.*;
	import java.text.ParsePosition;
	import java.text.SimpleDateFormat;
	import java.util.ArrayList;
	import java.util.Calendar;
	import java.util.Date;
	import java.util.List;
	import java.util.Properties;
	
	import com.fasterxml.jackson.core.JsonParseException;
	import com.fasterxml.jackson.core.JsonProcessingException;
	import com.fasterxml.jackson.databind.JsonMappingException;
	import com.fasterxml.jackson.databind.ObjectMapper;
	
	public class DBUtil {
		//���Ӷ���
		//Statement �������
		//������
		//�ر�����
		//�õ�һ�����Ӷ���
		//��ѯ���вΣ��޲Σ�
		//�޸ģ��вΣ��޲Σ�
		static Statement stmt = null;
		//��������������ַ����¼�û���������	
		static String DBDRIVER;
		static String DBURL;
		static String DBUID;
		static String DBPWD;
		
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
		//������
		 static  {
			//��������
			try {
				Class.forName(DBDRIVER);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		//�ر�����
		public static void close(Connection conn) {
			try {
				if(stmt!=null)
						stmt.close();
				if(conn!=null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/**
		 * ���ڹر�
		 * @param rs
		 */
		public static void close(ResultSet rs) {
	        Statement st = null;
	        Connection con = null;
	        try {
	            try {
	                if (rs != null) {
	                    st = rs.getStatement();
	                    rs.close();
	                }
	            } finally {
	                try {
	                    if (st != null) {
	                        con = st.getConnection();
	                        st.close();
	                    }
	                } finally {
	                    if (con != null) {
	                        con.close();
	                    }
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
		//�õ�һ�����Ӷ��󣬵��û�ʹ��DBUtil�޷������������ʱ
		//����ͨ��������������Ӷ���
		public static Connection getConnection() {
			 Connection conn = null;
			try {
				conn=DriverManager.getConnection(DBURL,DBUID,DBPWD);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return conn;
		}
		
		//executeQuery
		//executeUpdate
		//execute
		//��ò�ѯ�����ݼ�
		/**
		 * ��ѯ��������,ת���ѯ�ַ���
		 * @param sql
		 * @return
		 */
		public static ResultSet executeQuery(String sql) {
			Connection conn = getConnection();
			try {
				stmt = conn.createStatement();
				return stmt.executeQuery(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		//�޸ı������
		/**
		 * �����޸�,ת���޸��ַ���
		 * @param sql
		 * @return
		 */
		public static int executeUpdate(String sql) {
			Connection conn = getConnection();
			int result = 0;
			try {
				stmt = conn.createStatement();
				result = stmt.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				close(conn);
			}
			return result;
		}
		//���ִ�еĲ�ѯ��洢���̣��᷵�ض�����ݼ�������ִ�гɹ���¼��
		//���Ե��ñ����������صĽ����
		//��һ��List<ResultSet>��List<Integer>����
		public static Object execute(String sql) {
			Connection conn = getConnection();
			boolean b=false;
			try {
				stmt = conn.createStatement();
				b = stmt.execute(sql);			
				//true,ִ�е���һ����ѯ��䣬���ǿ��Եõ�һ�����ݼ�
				//false,ִ�е���һ���޸���䣬���ǿ��Եõ�һ��ִ�гɹ��ļ�¼��
				if(b){
					return stmt.getResultSet();
				}
				else {
					return stmt.getUpdateCount();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(!b) {
					close(conn);
				}
			}
			return null;
		}
		
		//
		//select * from student where name=? and sex=?
		/**
		 * ����������ѯ,ת���ѯ�ַ���,�Ͳ���
		 * @param sql
		 * @return
		 */
		public static ResultSet executeQuery(String sql,Object[] in) {
			Connection conn = getConnection();
			try {
				PreparedStatement pst = conn.prepareStatement(sql);
				for(int i=0;i<in.length;i++)
					pst.setObject(i+1, in[i]);
				stmt = pst;//ֻ��Ϊ�˹ر��������pst
				return pst.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public static int executeUpdate(String sql,Object[] in) {
			Connection conn = getConnection();
			try {
				PreparedStatement pst = conn.prepareStatement(sql);
				for(int i=0;i<in.length;i++)
					pst.setObject(i+1, in[i]);
				stmt = pst;//ֻ��Ϊ�˹ر��������pst
				return pst.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				close(conn);
			}
			return 0;
		}
		public static Object execute(String sql,Object[] in) {
			Connection conn = getConnection();
			boolean b=false;
			try {
				PreparedStatement pst = conn.prepareStatement(sql);
				for(int i=0;i<in.length;i++)
					pst.setObject(i+1, in[i]);
				b = pst.execute();
				//true,ִ�е���һ����ѯ��䣬���ǿ��Եõ�һ�����ݼ�
				//false,ִ�е���һ���޸���䣬���ǿ��Եõ�һ��ִ�гɹ��ļ�¼��
				if(b){
					System.out.println("----");
					/*List<ResultSet> list = new ArrayList<ResultSet>();
					list.add(pst.getResultSet());
					while(pst.getMoreResults()) {
						list.add(pst.getResultSet());
					}*/
					return pst.getResultSet();
				}
				else {
					System.out.println("****");
					List<Integer> list = new ArrayList<Integer>();
					list.add(pst.getUpdateCount());
					while(pst.getMoreResults()) {
						list.add(pst.getUpdateCount());
					}
					return list;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(!b) {
					System.out.println("====");
					close(conn);
				}
			}
			return null;
		}
		//���ô洢����  proc_Insert(?,?,?)
		public static Object executeProcedure(String procName,Object[] in) {
			Connection conn = getConnection();
			try {
				procName = "{call "+procName+"(";
				String link="";
				for(int i=0;i<in.length;i++) {
					procName+=link+"?";
					link=",";
				}
				procName+=")}";
				CallableStatement cstmt = conn.prepareCall(procName);
				for(int i=0;i<in.length;i++) {
					cstmt.setObject(i+1, in[i]);
				}
				if(cstmt.execute())
				{
					return cstmt.getResultSet();
				}
				else {
					return cstmt.getUpdateCount();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
	
		/*
		 * ���ô洢���̣������������
		 * @procName ���洢�������ƣ�proc_Insert(?,?)
		 * @in ,�����������
		 * @output,�����������
		 * @type,����������ͼ���
		 * */
		public static Object executeOutputProcedure(String procName,
				Object[] in,Object[] output,int[] type){
			Connection conn = getConnection();
			Object result = null;
			try {
				CallableStatement cstmt = conn.prepareCall("{call "+procName+"}");
				//���ô洢���̵Ĳ���ֵ
				int i=0;
				for(;i<in.length;i++){//�����������
					cstmt.setObject(i+1, in[i]);
					//print(i+1);
				}
				int len = output.length+i;
				for(;i<len;i++){//�����������
					cstmt.registerOutParameter(i+1,type[i-in.length]);
					//print(i+1);
				}
				boolean b = cstmt.execute();
				//��ȡ���������ֵ
				for(i=in.length;i<output.length+in.length;i++)
					output[i-in.length] = cstmt.getObject(i+1);
				if(b) {
					result = cstmt.getResultSet();
				}
				else {
					result = cstmt.getUpdateCount();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		public static String toJson(Object obj){
			String reuqest=null;
			//����ӳ��
			ObjectMapper mapper=new ObjectMapper();
			//����ʱ���ʽ
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy��MM��dd��");
			mapper.setDateFormat(dateFormat);
				try {
					reuqest=mapper.writeValueAsString(obj);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return reuqest;
		}
		public static String toJsonMMddHHmmss(Object obj){
			String reuqest=null;
			//����ӳ��
			ObjectMapper mapper=new ObjectMapper();
			//����ʱ���ʽ
			SimpleDateFormat dateFormat=new SimpleDateFormat("MMddHHmmss");
			mapper.setDateFormat(dateFormat);
				try {
					reuqest=mapper.writeValueAsString(obj);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return reuqest;
		}
		public static <T> T toObject(String src,Class<T> valueType){
			T request=null;
				//������
			  ObjectMapper mapper=new ObjectMapper();
			  try {
				request=mapper.readValue(src, valueType);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return request;
		}
		/**
		 * �ַ���ת��ʱ���ʽ,����Date����
		 * @param date_str
		 * @return
		 */
		public static Date date(String date_str) {
	        try {
	            Calendar zcal = Calendar.getInstance();//������
	            Timestamp timestampnow = new Timestamp(zcal.getTimeInMillis());//ת�������������ڸ�ʽ
	            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");//��Ϊ��Ҫ�Ķ���
	            ParsePosition pos = new ParsePosition(0);
	            java.util.Date current = formatter.parse(date_str, pos);
	            timestampnow = new Timestamp(current.getTime());
	            return timestampnow;
	        }
	        catch (NullPointerException e) {
	            return null;
	        }
	    }
		/**
		 * MD5����
		 */
		 public static String MD5(String key) {
		        char hexDigits[] = {
		                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
		        };
		        try {
		            byte[] btInput = key.getBytes();
		            // ���MD5ժҪ�㷨�� MessageDigest ����
		            MessageDigest mdInst = MessageDigest.getInstance("MD5");
		            // ʹ��ָ�����ֽڸ���ժҪ
		            mdInst.update(btInput);
		            // �������
		            byte[] md = mdInst.digest();
		            // ������ת����ʮ�����Ƶ��ַ�����ʽ
		            int j = md.length;
		            char str[] = new char[j * 2];
		            int k = 0;
		            for (int i = 0; i < j; i++) {
		                byte byte0 = md[i];
		                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
		                str[k++] = hexDigits[byte0 & 0xf];
		            }
		            return new String(str);
		        } catch (Exception e) {
		            return null;
		        }
		    }
	}
