package com.ming.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.server.config.ApiException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 极验 GT4（公有验证码 4.0）服务端校验。
 *
 * 流程：前端完成验证后回传 lot_number / captcha_output / pass_token / gen_time；
 * 服务端用 app.geetest.captcha-key 对 lot_number 做 HMAC-SHA256 得到 sign_token，
 * 调 {@code https://gcaptcha4.geetest.com/validate} 校验，result=success 才算通过。
 *
 * 未配置 captcha-id / captcha-key（环境变量 MING_GT4_CAPTCHA_ID / MING_GT4_CAPTCHA_KEY）时不校验（开发模式）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeetestService {

    private static final String VALIDATE_URL = "https://gcaptcha4.geetest.com/validate";

    private final ObjectMapper objectMapper;

    @Value("${app.geetest.captcha-id:}")
    private String captchaId;

    @Value("${app.geetest.captcha-key:}")
    private String captchaKey;

    public boolean enabled() {
        return captchaId != null && !captchaId.isBlank()
                && captchaKey != null && !captchaKey.isBlank();
    }

    /**
     * 校验 GT4 验证结果；校验不通过或服务异常时抛错。
     * 未配置验证码时直接放行。
     */
    public void verify(String lotNumber, String captchaOutput, String passToken, String genTime) {
        if (!enabled()) {
            return;
        }
        if (isBlank(lotNumber) || isBlank(captchaOutput) || isBlank(passToken) || isBlank(genTime)) {
            throw ApiException.badRequest("验证数据不完整，请重新完成安全验证");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("captcha_id", captchaId);
        params.put("lot_number", lotNumber);
        params.put("captcha_output", captchaOutput);
        params.put("pass_token", passToken);
        params.put("gen_time", genTime);

        try {
            params.put("sign_token", hmacSha256Hex(lotNumber, captchaKey));
            String query = params.entrySet().stream()
                    .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                    .collect(Collectors.joining("&"));
            String body = httpGet(VALIDATE_URL + "?" + query);
            JsonNode json = objectMapper.readTree(body);
            String result = json.path("result").asText();
            if (!"success".equals(result)) {
                log.warn("GT4 验证未通过：result={} reason={}", result, json.path("reason").asText());
                throw ApiException.badRequest("安全验证未通过，请重新完成验证");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("GT4 校验请求失败", e);
            throw ApiException.badRequest("安全验证服务暂时不可用，请稍后重试");
        }
    }

    private String httpGet(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String hmacSha256Hex(String message, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(raw.length * 2);
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}