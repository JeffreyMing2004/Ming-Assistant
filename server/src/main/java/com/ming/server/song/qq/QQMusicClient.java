package com.ming.server.song.qq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.server.config.ApiException;
import com.ming.server.song.qq.dto.QqTrack;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** 调用QQ音乐公开接口读取歌单（歌名 + 歌手）。 */
@Component
public class QQMusicClient {

    /** 抓取到的单个QQ音乐歌单。 */
    public record QqPlaylistData(String title, int total, List<QqTrack> tracks) {
    }

    private static final String API_TEMPLATE =
            "https://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg"
                    + "?type=1&json=1&utf8=1&onlysong=0&disstid=%s"
                    + "&g_tk=5381&loginUin=0&hostUin=0&format=json&inCharset=utf8"
                    + "&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";

    private static final Pattern ID_RE = Pattern.compile("(\\d{8,})");
    private static final int MAX_TRACKS = 500;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** 从链接或纯数字ID中提取歌单ID。支持的格式：纯数字、.../playlist/{id}、id={id}、disstid={id}。 */
    public static String extractId(String urlOrId) {
        String s = urlOrId == null ? "" : urlOrId.trim();
        if (s.isBlank()) {
            throw ApiException.badRequest("请输入QQ音乐歌单链接或歌单ID");
        }
        if (s.matches("\\d+")) {
            return s;
        }
        Matcher m = ID_RE.matcher(s);
        String id = null;
        while (m.find()) {
            id = m.group(1);
        }
        if (id == null) {
            throw ApiException.badRequest("无法从链接中识别QQ音乐歌单ID");
        }
        return id;
    }

    /** 抓取歌单，返回歌单名与歌曲列表。 */
    public QqPlaylistData fetch(String urlOrId) {
        String id = extractId(urlOrId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_TEMPLATE, id)))
                .timeout(Duration.ofSeconds(15))
                .header("Referer", "https://y.qq.com/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                        + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36")
                .GET()
                .build();

        String body;
        try {
            byte[] bytes = http.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
            body = decode(bytes);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "无法连接QQ音乐接口，请稍后重试");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.BAD_GATEWAY, "请求被中断，请稍后重试");
        }

        try {
            JsonNode root = mapper.readTree(body);
            JsonNode cd = root.path("cdlist");
            if (root.path("code").asInt() != 0 || !cd.isArray() || cd.isEmpty()) {
                throw ApiException.badRequest("未找到该QQ音乐歌单，请确认链接是否正确");
            }
            JsonNode first = cd.path(0);
            String title = first.path("dissname").asText();
            if (title.isBlank()) {
                title = "未命名歌单";
            }
            int total = first.path("songnum").asInt(0);
            JsonNode list = first.path("songlist");
            List<QqTrack> tracks = new ArrayList<>();
            if (list.isArray()) {
                for (JsonNode node : list) {
                    if (tracks.size() >= MAX_TRACKS) {
                        break;
                    }
                    String name = node.path("songname").asText().trim();
                    if (name.isEmpty()) {
                        continue;
                    }
                    tracks.add(new QqTrack(name, singers(node.path("singer"))));
                }
            }
            if (tracks.isEmpty()) {
                throw ApiException.badRequest("该歌单中没有可用的歌曲");
            }
            return new QqPlaylistData(title, total, tracks);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "QQ音乐接口返回数据解析失败，请稍后重试");
        }
    }

    private static String singers(JsonNode arr) {
        if (!arr.isArray()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (JsonNode s : arr) {
            String n = s.path("name").asText().trim();
            if (!n.isEmpty()) {
                names.add(n);
            }
        }
        return String.join("、", names);
    }

    /** QQ接口声明 utf-8 但实际可能返回 GBK 字节：先按 UTF-8 解，若出现替换字符则按 GBK 重解。 */
    private static String decode(byte[] bytes) {
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.indexOf('\uFFFD') < 0) {
            return utf8;
        }
        return new String(bytes, Charset.forName("GBK"));
    }
}