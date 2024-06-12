package web.web.visualization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.SuccessLogRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VisualizationService {
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;

    public Long[] logCount() {
        Long[] list = new Long[2];
        list[0] = successLogRepository.count();
        list[1] = failureLogRepository.count();
        return list;
    }
}
