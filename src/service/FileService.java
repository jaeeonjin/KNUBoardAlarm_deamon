package service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dto.BoardDTO;
import util.FileUtils;
import util.PropertiesUtils;

public class FileService {
	
	private static Logger logger = LoggerFactory.getLogger(FileService.class);

	private FileUtils fileUtils;
	private static FileService instance;

	private FileService(String directory) {
		fileUtils = new FileUtils(directory);
	}

	public synchronized static FileService getInstance() {
		if( instance == null ) {
			String directory = PropertiesUtils.readProperty("file.baseDirectory");
			instance = new FileService(directory);
		}
		return instance;
	}


	/**
	 * 파일 내용이 JSON 타입인 .JSON 파일을 생성한다. 
	 * @param filePath : 파일 경로 및 이름
	 * @param board : 게시판 데이터
	 */
	public void writeJSONFile(String filePath, BoardDTO board) {
		logger.info("파일 출력을 시작합니다. {}", filePath);
		String contents = createJSONObject(board).toJSONString();
		fileUtils.writeFile(contents, filePath);
		logger.info("파일 출력을 끝냈습니다. {}", filePath);
	}

	/**
	 * JSON 타입의 텍스트파일을 읽기 위해 필요한 FileReader 객체를 반환하는 메소드
	 * @param filePath
	 * @return FileReader 객체
	 */
	private FileReader getFileReader(String filePath) {
		logger.info("파일 READER 객체를 로딩합니다.");
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileUtils.getDirectory()+filePath);
			logger.info("파일 READER 객체를 로딩했습니다.");
			return fileReader;
		}catch(FileNotFoundException e) {
			logger.error("존재하지 않는 파일입니다. {} " + filePath);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 특정 JSON 파일 1개 읽기
	 * 파일 내 data부분(게시글 url:게시글 type)을 Map으로 저장해서 반환한다.
	 * @param fileName (확장자가 포함된 file name)
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public BoardDTO readJSONFile(String fileName) {
		logger.info("{} 파일 읽기를 시작합니다. ", fileName);
		BoardDTO dto = new BoardDTO();
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		FileReader fileReader = null;

		try {
			fileReader = getFileReader(fileName);
			json = (JSONObject) parser.parse(fileReader);
			
			dto.setData((Map<String, String>) json.get("data"));
			dto.setUri((String) json.get("uri"));
			dto.setType((String) json.get("type"));
			
			logger.info("{} 파일 읽기를 종료합니다. ", fileName);
		} catch (IOException e) {
			logger.error("파일 읽기 ERROR : 파일이 존재하지 않습니다.");
			e.printStackTrace();
		} catch (ParseException e) {
			logger.error("파일 읽기 ERROR : 파일의 형식이 JSON이 아닙니다.");
			e.printStackTrace();
		} finally {
			if( fileReader != null )
				try {
					fileReader.close();
				} catch (IOException e) {
					logger.error("파일 읽기 ERROR : FileReader.close()에서 문제가 발생했습니다.");
					e.printStackTrace();
				}
		}

		return dto;
	}

	/**
	 * 파일 삭제
	 * @param fileName : 확장자 미포함
	 */
	public void removeJSONFile(String fileName) {
		logger.info("파일 삭제를 시작합니다. {}", fileName);
		fileUtils.deleteFile(fileName);
		logger.info("파일 삭제를 종료합니다. {}", fileName);
	}

	/**
	 * 디렉토리 내 모든 .json 파일을 가져온다.
	 * @return
	 */
	public List<String> readAllJsonFileOfName() {
		List<String> fileNameList = fileUtils.readAllFile();
		return fileNameList;
	}

	/**
	 * 파일 출력을 위한 JSON 객체 생성
	 * {
	 * 		"url" : url,
	 * 		"type" : type,
	 * 		"data" : {
	 * 					"url":title,
	 * 					"url":title,
	 * 					"url":title...
	 * 				  }
	 * }
	 * @param board
	 */
	@SuppressWarnings("unchecked")
	private JSONObject createJSONObject(BoardDTO board) {

		JSONObject json = new JSONObject();
		json.put("uri", board.getUri());
		json.put("type", board.getType());
		json.put("data", board.getData());

		return json;
	}

	/**
	 * 디렉토리 내 파일들을 모두 읽어 데이터를 Map(타이틀:데이터) 형태로 저장한다.
	 * @return
	 */
	public Map<String, BoardDTO> readAllFiles() {
		List<String> fileNameList = readAllJsonFileOfName();
		Map<String, BoardDTO> fileDataMap = new HashMap<>();

		// 가져온 모든 파일의 이름으로 모든 파일을 읽어 저장한다.
		if( fileNameList != null && fileNameList.size() != 0 ) {
			for(String fileName : fileNameList) {
				BoardDTO board = null;
				try {
					board = readJSONFile(fileName);

					if(board != null) {
						fileDataMap.put(fileName, board);
					}
				} catch (Exception e) {
					//log.error("** 백업 파일 로딩 ERROR : ");
					//log.error("** 파일 데이터가 JSON이 맞는지, KEY가 제대로 존재하는지 확인해주세요.");
					//log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			// error msg
		}

		return fileDataMap;
	}
}
