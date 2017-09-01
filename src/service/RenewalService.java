package service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dto.BoardDTO;

/**
 * 갱신 서비스.
 * - 게시판에 새로운 게시글이 있는지 검사한다.
 * - 새 글이 발견되면 푸쉬 알림을 보내고, 기존의 파일과 동기화를 수행한다.
 * @author JaeeonJin
 */
public class RenewalService {

	private static Logger logger = LoggerFactory.getLogger(RenewalService.class);
	
	private HTMLParserService parserService;
	private FileService fileService;
	private FCMService fcmService;

	public RenewalService() {
		parserService = new HTMLParserService();
		fileService = FileService.getInstance();
		fcmService = new FCMService();
	}

	public void startCheck() {
		Map<String, BoardDTO> fileDataMap = fileService.readAllFiles();
		logger.info("파일을 읽고 있습니다.");
		
		if(fileDataMap != null && fileDataMap.size() !=0) {
			logger.info("로딩된 파일에 대해 새 글이 있는지 검사를 수행합니다. ( 로딩된 파일 : {}개", fileDataMap.size());
			
			for(String boardUrl : fileDataMap.keySet() ) {
				///log.info("============================================================");
				///log.info(boardUrl + " -> 새 글 검사 시작");

				BoardDTO board = fileDataMap.get(boardUrl);	
				try {
					checkNewBoardData(board);
				} catch (Exception e) {
					//log.error("URL : " + boardUrl +" >> 새 글 검사 중 ERROR 발생!! \n ");
					//log.error(e.getMessage());
					e.printStackTrace();
					continue;
				}

				logger.info("새 글 검사를 완료했습니다.");
			}
		} else {
			logger.info("파일 데이터가 없습니다.");
		}
	}

	/**
	 * 특정 게시판에 새로운 게시글이 있는지 검사한다.
	 * @param map
	 * @throws Exception 
	 */
	private void checkNewBoardData(BoardDTO board) throws Exception {

		logger.info("{}.board 파일 검사를 시작합니다.", board.getUri());
		
		BoardDTO beforeBoard;
		Map<String, String> parsedData;

		String boardUri = board.getUri();

		// 1. 해당 URL의 게시판을 파싱한다.
		parsedData = parserService.parsingBoard(boardUri, board.getType());
		//log.info("게시판 데이터를 파싱했습니다. uri : " + boardUri);
		//log.info("데이터 개수 : " + parsedData.size());

		// 2. 기존 URL의 게시판 정보를 로딩한다.
		//		beforeBoard = storageManager.select(boardUri);
		beforeBoard = fileService.readJSONFile(boardUri+".board");

		// 2-1. 내부 메모리에 게시판이 이미 존재한다면
		if( beforeBoard != null ) { 

			// 3. 기존 게시판 데이터와 새로 파싱한 데이터를 비교한다
			equalBoardData(boardUri, beforeBoard.getData(), parsedData);
			//log.info("새 글 검사를 완료했습니다.");

			// 4. 모든 업데이트를 끝내고 내부 저장소를 갱신한다.
			// 이 코드는 필요가 없을 뿐더러 에러를 발생시킨다..
			// BeforeMap은 Storage안에 있는 맵을 참조하는 오브젝트.
			// 따라서 이미 equalBoardData 메소드 수행 중 업데이트 사항이 그대로 반영됨.
		}

		// 2-2. 내부 메모리에 게시판이 존재하지 않는다면
		else {
			// 3. 서버 내부 메모리에 저장(파일이 추가되면 자동으로 수행, filewatchservice)
			// storageManager.insert(beforeBoard); 
			// log.info("새로운 게시판을 [서버 메모리]에 추가했습니다.");
		}

		// 공통
		fileService.writeJSONFile(boardUri, beforeBoard);
		
		logger.info("{}.board 파일 검사를 완료했습니다.", board.getUri());
		// log.info("파일이 출력되었습니다 >> " + boardUri + ".json");
	}

	/**
	 * 파싱 데이터와 기존 데이터를 비교하여 갱신 작업 및 푸쉬 알림을 보낸다.
	 * @param beforeBoard : STORAGE에 저장된 게시판
	 * @param parsedData : 새로 파싱한 게시판 데이터
	 * @throws Exception 
	 */
	private void equalBoardData(String boardUri, Map<String, String> beforeData, Map<String, String> parsedData) {
		
		for(String key : parsedData.keySet()) { 
			// 존재하지 않는 키 -> 새로운 게시글
			if( !beforeData.containsKey(key) ) { 
				logger.info("새로운 데이터가 발견되었습니다. >> " + parsedData.get(key));

				// FCM 발신
				fcmService.sendMessage(boardUri, key, parsedData.get(key));

				// 새로운 글이 발견되었으므로 beforeMap을 갱신한다.(overwrite)
				beforeData.put(key, parsedData.get(key));
			}
		}

		// 메모리 상에서, beforeData에는 이미 최신정보가 기록되어있다.
		// 하지만 기존의 beforeData에서 삭제된 데이터가 있을 수도 있다.
		// beforeData에 존재하는 key가 parsedData에서 존재하지 않을 수 있다는 뜻.
		// ex)
		// 기존에 파싱된 데이터 :: A, B, C
		// 새롭게 파싱된 데이터 :: A, B, D
		// 알림 전송되는 데이터 : D
		// 그렇다면 C는? 없애야한다.
		// 아래와 같은 로직이 위 프로세스를 수행한다.
		for(String key : beforeData.keySet()) {
			// 파싱데이터에 기존데이터의 어떤 항목이 없으면..
			if(!parsedData.containsKey(key)) {
				beforeData.remove(key);
			}
		}

	}

}
