package kafka.kafka.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kafka.kafka.admin.domain.LogFormat;
import kafka.kafka.admin.repository.ScenarioRepository;
import kafka.kafka.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FormatService {

    private static final String NEXT_TOPIC_PREFIX = "filter_topic_";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisService redisService;

    @KafkaListener(topicPattern = "format_topic_.*", groupId = "format_group", concurrency = "5")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws JsonProcessingException {
        Long scenarioId = extractScenarioIdFromTopic(topic);
        String key = "scenario:" + scenarioId + ":format";
        String formatString = redisService.getValue(key);

        LogFormat format;
        if (formatString != null) format = objectMapper.readValue(formatString, LogFormat.class);
        else {
            format = scenarioRepository.findById(scenarioId).orElse(null).getLogFormat();
            redisService.setValueWithTTL(key, objectMapper.writeValueAsString(format), 600);
        }

        if (format == null) return;

        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            ObjectNode newJson = objectMapper.createObjectNode();

            // logFormat의 각 필드를 확인하여 1인 필드만 추가
            if (format.getIdsite() == 1) newJson.set("idsite", jsonNode.get("idsite"));
            if (format.getE_a() == 1) newJson.set("e_a", jsonNode.get("e_a"));
            if (format.getE_c() == 1) newJson.set("e_c", jsonNode.get("e_c"));
            if (format.getREMOTE_ADDR() == 1) newJson.set("REMOTE_ADDR", jsonNode.get("REMOTE_ADDR"));
            if (format.getRec() == 1) newJson.set("rec", jsonNode.get("rec"));
            if (format.getJava() == 1) newJson.set("java", jsonNode.get("java"));
            if (format.getE_n() == 1) newJson.set("e_n", jsonNode.get("e_n"));
            if (format.getPf_dm1() == 1) newJson.set("pf_dm1", jsonNode.get("pf_dm1"));
            if (format.getUadata() == 1) newJson.set("uadata", jsonNode.get("uadata"));
            if (format.getE_v() == 1) newJson.set("e_v", jsonNode.get("e_v"));
            if (format.getPf_tfr() == 1) newJson.set("pf_tfr", jsonNode.get("pf_tfr"));
            if (format.getMemberId() == 1) newJson.set("memberId", jsonNode.get("memberId"));
            if (format.getHTTP_ORIGIN() == 1) newJson.set("HTTP_ORIGIN", jsonNode.get("HTTP_ORIGIN"));
            if (format.getSend_image() == 1) newJson.set("send_image", jsonNode.get("send_image"));
            if (format.getRes() == 1) newJson.set("res", jsonNode.get("res"));
            if (format.getQt() == 1) newJson.set("qt", jsonNode.get("qt"));
            if (format.getCookie() == 1) newJson.set("cookie", jsonNode.get("cookie"));
            if (format.getHTTP_ACCEPT_LANGUAGE() == 1) newJson.set("HTTP_ACCEPT_LANGUAGE", jsonNode.get("HTTP_ACCEPT_LANGUAGE"));
            if (format.getAg() == 1) newJson.set("ag", jsonNode.get("ag"));
            if (format.getHTTP_X_REAL_IP() == 1) newJson.set("HTTP_X_REAL_IP", jsonNode.get("HTTP_X_REAL_IP"));
            if (format.getHTTP_CONTENT_TYPE() == 1) newJson.set("HTTP_CONTENT_TYPE", jsonNode.get("HTTP_CONTENT_TYPE"));
            if (format.getHTTP_X_FORWARDED_PROTO() == 1) newJson.set("HTTP_X_FORWARDED_PROTO", jsonNode.get("HTTP_X_FORWARDED_PROTO"));
            if (format.get_event_record() == 1) newJson.set("_event_record", jsonNode.get("_event_record"));
            if (format.getHTTP_USER_AGENT() == 1) newJson.set("HTTP_USER_AGENT", jsonNode.get("HTTP_USER_AGENT"));
            if (format.get_id() == 1) newJson.set("_id", jsonNode.get("_id"));
            if (format.getPf_net() == 1) newJson.set("pf_net", jsonNode.get("pf_net"));
            if (format.get_refts() == 1) newJson.set("_refts", jsonNode.get("_refts"));
            if (format.getPf_srv() == 1) newJson.set("pf_srv", jsonNode.get("pf_srv"));
            if (format.getWma() == 1) newJson.set("wma", jsonNode.get("wma"));
            if (format.getPf_onl() == 1) newJson.set("pf_onl", jsonNode.get("pf_onl"));
            if (format.getHTTP_REFERER() == 1) newJson.set("HTTP_REFERER", jsonNode.get("HTTP_REFERER"));
            if (format.get_idn() == 1) newJson.set("_idn", jsonNode.get("_idn"));
            if (format.getFla() == 1) newJson.set("fla", jsonNode.get("fla"));
            if (format.getCa() == 1) newJson.set("ca", jsonNode.get("ca"));
            if (format.getUrlref() == 1) newJson.set("urlref", jsonNode.get("urlref"));
            if (format.getHTTP_CONTENT_LENGTH() == 1) newJson.set("HTTP_CONTENT_LENGTH", jsonNode.get("HTTP_CONTENT_LENGTH"));
            if (format.getRealp() == 1) newJson.set("realp", jsonNode.get("realp"));
            if (format.getH() == 1) newJson.set("h", jsonNode.get("h"));
            if (format.getPf_dm2() == 1) newJson.set("pf_dm2", jsonNode.get("pf_dm2"));
            if (format.getM() == 1) newJson.set("m", jsonNode.get("m"));
            if (format.getUrl() == 1) newJson.set("url", jsonNode.get("url"));
            if (format.getR() == 1) newJson.set("r", jsonNode.get("r"));
            if (format.getHTTP_HOST() == 1) newJson.set("HTTP_HOST", jsonNode.get("HTTP_HOST"));
            if (format.getS() == 1) newJson.set("s", jsonNode.get("s"));
            if (format.getPdf() == 1) newJson.set("pdf", jsonNode.get("pdf"));
            if (format.getHTTP_ACCEPT() == 1) newJson.set("HTTP_ACCEPT", jsonNode.get("HTTP_ACCEPT"));
            if (format.getHTTP_X_FORWARDED_FOR() == 1) newJson.set("HTTP_X_FORWARDED_FOR", jsonNode.get("HTTP_X_FORWARDED_FOR"));
            if (format.getHTTP_ACCEPT_ENCODING() == 1) newJson.set("HTTP_ACCEPT_ENCODING", jsonNode.get("HTTP_ACCEPT_ENCODING"));
            if (format.getHTTP_CONNECTION() == 1) newJson.set("HTTP_CONNECTION", jsonNode.get("HTTP_CONNECTION"));
            if (format.getDate() == 1) newJson.set("date", jsonNode.get("date"));

            String newMessage = objectMapper.writeValueAsString(newJson);
            kafkaTemplate.send(NEXT_TOPIC_PREFIX + scenarioId, newMessage);

        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

    private Long extractScenarioIdFromTopic(String topic) {
        Pattern pattern = Pattern.compile("format_topic_(\\d+)");
        Matcher matcher = pattern.matcher(topic);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}

