package kafka.kafka.format;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kafka.kafka.admin.domain.LogFormat;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class FormatService1 {

    private static final String TOPIC_NAME = "format_topic_1";
    private static final String NEXT_TOPIC = "filter_topic_1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
    public void listen(String message) {
        System.out.println("scenario_format_1: " + message);

        Long scenario_id = 1L;
        Scenario scenario = scenarioRepository.findById(scenario_id).orElse(null);
        // LogFormatRepository에서 id가 1인 데이터를 가져옴
        LogFormat logFormat = scenario.getLogFormat();
        if (logFormat == null) {
            System.err.println("LogFormat not found");
            return;
        }
        try {
            // 받은 메시지를 JSON으로 파싱
            JsonNode jsonNode = objectMapper.readTree(message);

            // 새로운 JSON 객체 생성
            ObjectNode newJson = objectMapper.createObjectNode();

            // logFormat의 각 필드를 확인하여 1인 필드만 추가
            if (logFormat.getIdsite() == 1) newJson.set("idsite", jsonNode.get("idsite"));
            if (logFormat.getE_a() == 1) newJson.set("e_a", jsonNode.get("e_a"));
            if (logFormat.getE_c() == 1) newJson.set("e_c", jsonNode.get("e_c"));
            if (logFormat.getREMOTE_ADDR() == 1) newJson.set("REMOTE_ADDR", jsonNode.get("REMOTE_ADDR"));
            if (logFormat.getRec() == 1) newJson.set("rec", jsonNode.get("rec"));
            if (logFormat.getJava() == 1) newJson.set("java", jsonNode.get("java"));
            if (logFormat.getE_n() == 1) newJson.set("e_n", jsonNode.get("e_n"));
            if (logFormat.getPf_dm1() == 1) newJson.set("pf_dm1", jsonNode.get("pf_dm1"));
            if (logFormat.getUadata() == 1) newJson.set("uadata", jsonNode.get("uadata"));
            if (logFormat.getE_v() == 1) newJson.set("e_v", jsonNode.get("e_v"));
            if (logFormat.getPf_tfr() == 1) newJson.set("pf_tfr", jsonNode.get("pf_tfr"));
            if (logFormat.getMemberId() == 1) newJson.set("memberId", jsonNode.get("memberId"));
            if (logFormat.getHTTP_ORIGIN() == 1) newJson.set("HTTP_ORIGIN", jsonNode.get("HTTP_ORIGIN"));
            if (logFormat.getSend_image() == 1) newJson.set("send_image", jsonNode.get("send_image"));
            if (logFormat.getRes() == 1) newJson.set("res", jsonNode.get("res"));
            if (logFormat.getQt() == 1) newJson.set("qt", jsonNode.get("qt"));
            if (logFormat.getCookie() == 1) newJson.set("cookie", jsonNode.get("cookie"));
            if (logFormat.getHTTP_ACCEPT_LANGUAGE() == 1) newJson.set("HTTP_ACCEPT_LANGUAGE", jsonNode.get("HTTP_ACCEPT_LANGUAGE"));
            if (logFormat.getAg() == 1) newJson.set("ag", jsonNode.get("ag"));
            if (logFormat.getHTTP_X_REAL_IP() == 1) newJson.set("HTTP_X_REAL_IP", jsonNode.get("HTTP_X_REAL_IP"));
            if (logFormat.getHTTP_CONTENT_TYPE() == 1) newJson.set("HTTP_CONTENT_TYPE", jsonNode.get("HTTP_CONTENT_TYPE"));
            if (logFormat.getHTTP_X_FORWARDED_PROTO() == 1) newJson.set("HTTP_X_FORWARDED_PROTO", jsonNode.get("HTTP_X_FORWARDED_PROTO"));
            if (logFormat.get_event_record() == 1) newJson.set("_event_record", jsonNode.get("_event_record"));
            if (logFormat.getHTTP_USER_AGENT() == 1) newJson.set("HTTP_USER_AGENT", jsonNode.get("HTTP_USER_AGENT"));
            if (logFormat.get_id() == 1) newJson.set("_id", jsonNode.get("_id"));
            if (logFormat.getPf_net() == 1) newJson.set("pf_net", jsonNode.get("pf_net"));
            if (logFormat.get_refts() == 1) newJson.set("_refts", jsonNode.get("_refts"));
            if (logFormat.getPf_srv() == 1) newJson.set("pf_srv", jsonNode.get("pf_srv"));
            if (logFormat.getWma() == 1) newJson.set("wma", jsonNode.get("wma"));
            if (logFormat.getPf_onl() == 1) newJson.set("pf_onl", jsonNode.get("pf_onl"));
            if (logFormat.getHTTP_REFERER() == 1) newJson.set("HTTP_REFERER", jsonNode.get("HTTP_REFERER"));
            if (logFormat.get_idn() == 1) newJson.set("_idn", jsonNode.get("_idn"));
            if (logFormat.getFla() == 1) newJson.set("fla", jsonNode.get("fla"));
            if (logFormat.getCa() == 1) newJson.set("ca", jsonNode.get("ca"));
            if (logFormat.getUrlref() == 1) newJson.set("urlref", jsonNode.get("urlref"));
            if (logFormat.getHTTP_CONTENT_LENGTH() == 1) newJson.set("HTTP_CONTENT_LENGTH", jsonNode.get("HTTP_CONTENT_LENGTH"));
            if (logFormat.getRealp() == 1) newJson.set("realp", jsonNode.get("realp"));
            if (logFormat.getH() == 1) newJson.set("h", jsonNode.get("h"));
            if (logFormat.getPf_dm2() == 1) newJson.set("pf_dm2", jsonNode.get("pf_dm2"));
            if (logFormat.getM() == 1) newJson.set("m", jsonNode.get("m"));
            if (logFormat.getUrl() == 1) newJson.set("url", jsonNode.get("url"));
            if (logFormat.getR() == 1) newJson.set("r", jsonNode.get("r"));
            if (logFormat.getHTTP_HOST() == 1) newJson.set("HTTP_HOST", jsonNode.get("HTTP_HOST"));
            if (logFormat.getS() == 1) newJson.set("s", jsonNode.get("s"));
            if (logFormat.getPdf() == 1) newJson.set("pdf", jsonNode.get("pdf"));
            if (logFormat.getHTTP_ACCEPT() == 1) newJson.set("HTTP_ACCEPT", jsonNode.get("HTTP_ACCEPT"));
            if (logFormat.getHTTP_X_FORWARDED_FOR() == 1) newJson.set("HTTP_X_FORWARDED_FOR", jsonNode.get("HTTP_X_FORWARDED_FOR"));
            if (logFormat.getHTTP_ACCEPT_ENCODING() == 1) newJson.set("HTTP_ACCEPT_ENCODING", jsonNode.get("HTTP_ACCEPT_ENCODING"));
            if (logFormat.getHTTP_CONNECTION() == 1) newJson.set("HTTP_CONNECTION", jsonNode.get("HTTP_CONNECTION"));
            if (logFormat.getDate() == 1) newJson.set("date", jsonNode.get("date"));


            // 새로운 JSON 객체를 문자열로 변환하여 다음 토픽으로 전송
            String newMessage = objectMapper.writeValueAsString(newJson);
            kafkaTemplate.send(NEXT_TOPIC, newMessage);

        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }
}

