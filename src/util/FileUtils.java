package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaeeonjin
 * 파일 입출력 유틸 클래스
 */
public class FileUtils {

	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	private String baseDirectory;

	public FileUtils(String directory) {
		this.baseDirectory = directory;
		makeDirectory();
	}

	/**
	 * 디렉토리 폴더 생성 메소드
	 */
	private void makeDirectory() {
		File path = new File(baseDirectory);
		if( !path.isDirectory() )
			path.mkdir();
	}


	/**
	 * 파일 쓰기 메소드
	 * @param content : 파일에 저장할 데이터
	 * @param fileName : 파일 이름
	 */
	public void writeFile(String content, String filePath) {
		FileWriter file;
		try {
			file = new FileWriter(baseDirectory+filePath);
			file.write(content);
			file.flush();
			file.close();
		} catch(IOException e) {
			logger.error("파일 쓰기 ERROR : {}", filePath);
			e.printStackTrace();
		}
	}

	/**
	 * baseDirectory 내의 모든 파일들의 이름을 저장하고 반환하는 메소드.
	 * 전체 파일을 읽을 때 사용한다.
	 * @return 파일 이름 리스트
	 */
	public List<String> readAllFile() {
		List<String> fileNameList = new ArrayList<>();

		File fileList[] = new File(baseDirectory).listFiles();
		if( fileList.length == 0 || fileList == null ) { 
			logger.warn(baseDirectory+"내에 존재하는 파일이 없습니다.");
			return null;
		}

		for(File file : fileList) {
			fileNameList.add( file.getName() );			
		}

		return fileNameList;
	}

	/**
	 * 파일 삭제 메소드
	 * @param filePath
	 * @return true : 파일 삭제 O / false : 파일 삭제 X
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(baseDirectory+filePath);
		boolean flag = file.delete();

		if( !flag ) logger.error("파일을 삭제할 수 없습니다. 존재하지 않는 파일입니다. {}", filePath);
		return flag;
	}

	public boolean isFile(String filePath) {
		File file = new File(baseDirectory+filePath);
		return file.isFile();
	}

	public String getDirectory() {
		return baseDirectory;
	}

}
