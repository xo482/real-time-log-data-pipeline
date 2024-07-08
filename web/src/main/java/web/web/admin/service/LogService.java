package web.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.SuccessLogRepository;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
public class LogService {


    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;

    private Random random = new Random();
    private ScheduledExecutorService scheduler;

//    @PostConstruct
//    public void startScheduler() {
//        scheduler = Executors.newScheduledThreadPool(1);
//        scheduleNextSuccessLogInsertion();
//        scheduleNextFailureLogInsertion();
//    }
//
//    private void scheduleNextSuccessLogInsertion() {
//        scheduler.schedule(this::insertMultipleSuccessData, 1, TimeUnit.SECONDS);
//    }
//
//    private void scheduleNextFailureLogInsertion() {
//        scheduler.schedule(this::insertMultipleFailureData, 1, TimeUnit.SECONDS);
//    }
//
//    public void insertMultipleSuccessData() {
//        int numberOfLogs = ThreadLocalRandom.current().nextInt(0, 11); // 0~10개의 로그 생성
//        for (int i = 0; i < numberOfLogs; i++) {
//            insertSuccessData();
//        }
//        scheduleNextSuccessLogInsertion(); // 다음 작업 예약
//    }
//
//    public void insertMultipleFailureData() {
//        int numberOfLogs = ThreadLocalRandom.current().nextInt(0, 11); // 0~10개의 로그 생성
//        for (int i = 0; i < numberOfLogs; i++) {
//            insertFailureData();
//        }
//        scheduleNextFailureLogInsertion(); // 다음 작업 예약
//    }
//
//    public void insertSuccessData() {
//        // 현재 날짜와 시간 불러오기
//        LocalDateTime currentDateTime = LocalDateTime.now();
//        // 포맷터 정의
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//        // 포맷팅
//        String formattedDateTime = currentDateTime.format(formatter);
//
//        SuccessLog successLog = new SuccessLog();
//        successLog.setScenario_id(random.nextLong(2)+1L); // 시나리오 ID를 랜덤하게 설정 (원하는 로직에 맞게 수정 가능)
//        successLog.setMessage("{\"date\":\"" + formattedDateTime + "\"}");
//        successLogRepository.save(successLog);
//        System.out.println("Success Log inserted: " + successLog.getMessage());
//    }
//
//    public void insertFailureData() {
//        // 현재 날짜와 시간 불러오기
//        LocalDateTime currentDateTime = LocalDateTime.now();
//        // 포맷터 정의
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//        // 포맷팅
//        String formattedDateTime = currentDateTime.format(formatter);
//
//        FailureLog failureLog = new FailureLog();
//        failureLog.setScenario_id(random.nextLong(2)+1L); // 시나리오 ID를 랜덤하게 설정 (원하는 로직에 맞게 수정 가능)
//        failureLog.setMessage("{\"date\":\"" + formattedDateTime + "\"}");
//        failureLogRepository.save(failureLog);
//        System.out.println("Failure Log inserted: " + failureLog.getMessage());
//    }
//
//    @PreDestroy
//    public void stopScheduler() {
//        scheduler.shutdown();
//    }
}
