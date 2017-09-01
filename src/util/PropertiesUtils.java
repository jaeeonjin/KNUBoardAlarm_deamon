package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
 
/**
 * 프로퍼티즈 유틸
 * 파일 경로를 설정해야 사용이 가능하다.
 * @author JaeeonJin
 *
 */
public class PropertiesUtils {
 
	final static String PROPERT_FILE_LOCATION = "boardsystem.property";
	
    public static String readProperty(String propertyName) {
        
    	FileInputStream fis = null;
    	Properties props = null;
    	String result = null;
    	 
        try{                     	
        	fis = new FileInputStream(PROPERT_FILE_LOCATION);
        	
        	props = new Properties();
            props.load(new java.io.BufferedInputStream(fis));                                      
            result = props.getProperty(propertyName) ;
            return result;            
        } catch( Exception e ){        
        	e.printStackTrace();       
        } finally {
        	if( fis != null ) try { fis.close(); } catch (IOException e) { e.printStackTrace(); }
        	props = null;
        }
       
        return result;
    }
    
//    public static void main(String[] args) {
//		System.out.println(new PropertiesUtils().readProperty("fcm.serverKey"));
//	}
    
}
