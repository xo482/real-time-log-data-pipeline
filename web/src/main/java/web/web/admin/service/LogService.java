package web.web.admin.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import web.web.admin.domain.log.FailureLog;
import web.web.admin.domain.log.SuccessLog;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.SuccessLogRepository;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogService {


    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;

    private Random random = new Random();
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void startScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduleNextSuccessLogInsertion();
        scheduleNextFailureLogInsertion();
    }

    private void scheduleNextSuccessLogInsertion() {
        int delay = ThreadLocalRandom.current().nextInt(1, 4);
        scheduler.schedule(this::insertSuccessData, delay, TimeUnit.SECONDS);
    }

    private void scheduleNextFailureLogInsertion() {
        int delay = ThreadLocalRandom.current().nextInt(1, 4);
        scheduler.schedule(this::insertFailureData, delay, TimeUnit.SECONDS);
    }

    public void insertSuccessData() {
        SuccessLog successLog = new SuccessLog();
        successLog.setScenario_id(random.nextLong(3));  // 시나리오 ID를 랜덤하게 설정 (원하는 로직에 맞게 수정 가능)
        successLog.setMessage("Random message " + random.nextInt(1000));
        successLogRepository.save(successLog);
        System.out.println("Data inserted: " + successLog.getMessage());
        scheduleNextSuccessLogInsertion();  // 다음 작업 예약
    }

    public void insertFailureData() {
        FailureLog failureLog = new FailureLog();
        failureLog.setScenario_id(random.nextLong(3));  // 시나리오 ID를 랜덤하게 설정 (원하는 로직에 맞게 수정 가능)
        failureLog.setMessage("Random message " + random.nextInt(1000));
        failureLogRepository.save(failureLog);
        System.out.println("Data inserted: " + failureLog.getMessage());
        scheduleNextFailureLogInsertion();  // 다음 작업 예약
    }

    @PreDestroy
    public void stopScheduler() {
        scheduler.shutdown();
    }
}
