//import org.json.JSONObject;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class LogParser {
//  public static void main(String[] args) {
//    String logEntry = "2022-06-07 15:13:19.154641861 +0900: ('HTTP_USERAGENT\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebkit/537.36 (KHTML like Gecko) Chrome/102.0.5005.63 Safari/537.36\",\"HTTP_ACCEPT_LANGUAGE\":\"ko-KR,ko;q=0.9,en-JUS;q=0.8,en;q=0.7\",\"message\":\"ldsite=61%2CYblxGr9e&cookie=1&res=1920x1080&uid=UserID&pf_net=0&pf_sry=1&pf_tfr=8&pf_onl=230&pf_dm1=1098&pf_dm2=1098&url=http%3A%2F%2F192.168.20.52%3A8090%2Fecubejadmin%2Fshopping%2Findex.html&urlref=http%3A%2F%2F192.168.20.52%3A8090%2Fecubeadmin%2Fshopping%2Findex.html&e_c=EventCategory\",\"QUERY_OBZ_JP\":\"192.168.34.132\")";
//
//    // Extract timestamp
//    String timestampPattern = "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+ \\+\\d{4})";
//    Pattern pattern = Pattern.compile(timestampPattern);
//    Matcher matcher = pattern.matcher(logEntry);
//    String timestamp = "";
//    if (matcher.find()) {
//      timestamp = matcher.group(1);
//    }
//
//    // Extract userAgent
//    String userAgentPattern = "HTTP_USERAGENT\":\"([^\"]+)\"";
//    matcher = Pattern.compile(userAgentPattern).matcher(logEntry);
//    String userAgent = "";
//    if (matcher.find()) {
//      userAgent = matcher.group(1);
//    }
//
//    // Extract acceptLanguage
//    String acceptLanguagePattern = "HTTP_ACCEPT_LANGUAGE\":\"([^\"]+)\"";
//    matcher = Pattern.compile(acceptLanguagePattern).matcher(logEntry);
//    String acceptLanguage = "";
//    if (matcher.find()) {
//      acceptLanguage = matcher.group(1);
//    }
//
//    // Extract message parameters
//    String messagePattern = "message\":\"([^\"]+)\"";
//    matcher = Pattern.compile(messagePattern).matcher(logEntry);
//    String message = "";
//    if (matcher.find()) {
//      message = matcher.group(1);
//    }
//
//    // Parse message parameters
//    String[] params = message.split("&");
//    String userId = "";
//    String screenResolution = "";
//    String url = "";
//    String referrerUrl = "";
//    int networkTime = 0;
//    int serverResponseTime = 0;
//    int transferTime = 0;
//    int onLoadTime = 0;
//    int domContentLoadedTime = 0;
//    String eventCategory = "";
//    Map<String, String> extraFields = new HashMap<>();
//
//    for (String param : params) {
//      String[] keyValue = param.split("=");
//      if (keyValue.length < 2) continue;
//      switch (keyValue[0]) {
//        case "uid":
//          userId = keyValue[1];
//          break;
//        case "res":
//          screenResolution = keyValue[1];
//          break;
//        case "url":
//          url = keyValue[1];
//          break;
//        case "urlref":
//          referrerUrl = keyValue[1];
//          break;
//        case "pf_net":
//          networkTime = Integer.parseInt(keyValue[1]);
//          break;
//        case "pf_sry":
//          serverResponseTime = Integer.parseInt(keyValue[1]);
//          break;
//        case "pf_tfr":
//          transferTime = Integer.parseInt(keyValue[1]);
//          break;
//        case "pf_onl":
//          onLoadTime = Integer.parseInt(keyValue[1]);
//          break;
//        case "pf_dm1":
//          domContentLoadedTime = Integer.parseInt(keyValue[1]);
//          break;
//        case "e_c":
//          eventCategory = keyValue[1];
//          break;
//        default:
//          extraFields.put(keyValue[0], keyValue[1]);
//          break;
//      }
//    }
//
//    // Construct JSON log
//    JSONObject logJson = new JSONObject();
//    logJson.put("timestamp", timestamp);
//    logJson.put("userAgent", userAgent);
//    logJson.put("acceptLanguage", acceptLanguage);
//    logJson.put("userId", userId);
//    logJson.put("screenResolution", screenResolution);
//    logJson.put("url", url);
//    logJson.put("referrerUrl", referrerUrl);
//
//    JSONObject performanceMetrics = new JSONObject();
//    performanceMetrics.put("networkTime", networkTime);
//    performanceMetrics.put("serverResponseTime", serverResponseTime);
//    performanceMetrics.put("transferTime", transferTime);
//    performanceMetrics.put("onLoadTime", onLoadTime);
//    performanceMetrics.put("domContentLoadedTime", domContentLoadedTime);
//
//    logJson.put("performanceMetrics", performanceMetrics);
//    logJson.put("eventCategory", eventCategory);
//    logJson.put("extraFields", extraFields);
//
//    // Write JSON to file
//    try (FileWriter file = new FileWriter("log.json")) {
//      file.write(logJson.toString(4));
//      System.out.println("Successfully written JSON to file");
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//}
