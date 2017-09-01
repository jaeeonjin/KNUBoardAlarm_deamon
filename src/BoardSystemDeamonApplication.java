import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.RenewalService;

/**
 * 메인
 * TODO : 새학기로 넘어가는 시점에 기존의 강의 게시판이 모두 초기화 되기 때문에 쿼츠 스케줄링 기반으로 스케줄링을 작성해야한다...
 * TODO : 사용자가 추가한 게시판이 어느 시점에서는 삭제되어 더 이상 검사할 필요성이 없을 때가 있다. 이럴 때는 해당 게시판에 대한 토큰리스트를 조회할 때 토큰이 한 개도 존재하지 않는다면 삭제하면 된다. 
 * 
 * @author JaeeonJin
 *
 */
public class BoardSystemDeamonApplication {

	private static Logger logger = LoggerFactory.getLogger(BoardSystemDeamonApplication.class);
	
	final static int INTERVAL_TIME = 3600000; // 1 hour

	public static void main(String[] args)  {
		RenewalService mainService = new RenewalService();
		int startCount = 1;
		
		// 예외 1
		// Q. 만약 시작을 했는데 내부에 저장된 게시판데이터가 1도 존재하지 않아서 작업을 시작할 수 없다면?
		// A. 그냥 끝나네..
		while(true) {
			logger.info("{}번째 실시간 검사를 시작합니다. ( 간격 : {}ms ) ", startCount, INTERVAL_TIME);
			mainService.startCheck();
			logger.info("{}번째 실시간 검사를 종료합니다.", startCount);
			try {
				Thread.sleep(INTERVAL_TIME);
			} catch (InterruptedException e) {
				logger.error("메인 Service에서 문제가 발생했습니다.");
				e.printStackTrace();
			}
		}
	}

}