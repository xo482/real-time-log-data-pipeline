package web.web.visualization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.SuccessLogRepository;

@Controller
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class Visualization {
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;

    @GetMapping("/visualization")
    private void visualization() {

    }
}
