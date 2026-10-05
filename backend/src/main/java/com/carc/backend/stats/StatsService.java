package com.carc.backend.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轻量访问统计服务：内存计数为主，定时落盘 JSON（backend/data/stats.json）。
 */
@Service
public class StatsService {

    private static final File DATA_FILE = new File("data/stats.json");

    private final ConcurrentHashMap<String, AtomicLong> pagePv = new ConcurrentHashMap<>();
    private final AtomicLong totalPv = new AtomicLong(0);

    /** 当日统计：key 为日期字符串，跨天自动切换 */
    private volatile LocalDate today = LocalDate.now();
    private final ConcurrentHashMap<String, AtomicLong> todayPagePv = new ConcurrentHashMap<>();
    private final AtomicLong todayTotalPv = new AtomicLong(0);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 记录一次页面访问 */
    public void record(String uri, String ip, String userAgent) {
        totalPv.incrementAndGet();
        pagePv.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
        recordToday(uri);
    }

    private void recordToday(String uri) {
        LocalDate now = LocalDate.now();
        if (!now.equals(today)) {
            synchronized (this) {
                if (!now.equals(today)) {
                    today = now;
                    todayPagePv.clear();
                    todayTotalPv.set(0);
                }
            }
        }
        todayTotalPv.incrementAndGet();
        todayPagePv.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
    }

    /** 当前统计快照（供 /api/stats 返回） */
    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPv", totalPv.get());
        result.put("today", today.toString());
        result.put("todayPv", todayTotalPv.get());
        result.put("pages", sortedMap(pagePv));
        result.put("todayPages", sortedMap(todayPagePv));
        return result;
    }

    private Map<String, Long> sortedMap(ConcurrentHashMap<String, AtomicLong> source) {
        Map<String, Long> map = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .forEach(e -> map.put(e.getKey(), e.getValue().get()));
        return map;
    }

    @PostConstruct
    public void load() {
        try {
            if (DATA_FILE.exists() && DATA_FILE.length() > 0) {
                JsonSnapshot loaded = objectMapper.readValue(DATA_FILE, JsonSnapshot.class);
                if (loaded != null) {
                    totalPv.set(loaded.totalPv == null ? 0 : loaded.totalPv);
                    if (loaded.pages != null) {
                        loaded.pages.forEach((k, v) -> pagePv.put(k, new AtomicLong(v == null ? 0 : v)));
                    }
                }
            }
        } catch (Exception e) {
            // 数据文件不可读时忽略，从空统计开始
        }
    }

    @Scheduled(initialDelay = 300000, fixedDelay = 300000)
    public void saveScheduled() {
        save();
    }

    @PreDestroy
    public void save() {
        try {
            File parent = DATA_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            JsonSnapshot snapshot = new JsonSnapshot();
            snapshot.totalPv = totalPv.get();
            snapshot.pages = sortedMap(pagePv);
            objectMapper.writeValue(DATA_FILE, snapshot);
        } catch (Exception e) {
            // 落盘失败不影响统计功能
        }
    }

    /** 持久化数据结构 */
    public static class JsonSnapshot {
        public Long totalPv;
        public Map<String, Long> pages;
    }
}