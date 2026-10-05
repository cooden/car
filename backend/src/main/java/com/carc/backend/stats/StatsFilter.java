package com.carc.backend.stats;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 全局页面访问统计过滤器：只统计页面 GET 请求，排除接口、统计面板与静态资源。
 */
@Component
public class StatsFilter implements Filter {

    private final StatsService statsService;

    public StatsFilter(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        if ("GET".equalsIgnoreCase(httpRequest.getMethod()) && shouldCount(uri)) {
            statsService.record(uri, clientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        }
        chain.doFilter(request, response);
    }

    private boolean shouldCount(String uri) {
        // 页面路由不含扩展名；排除 /api 接口与 /stats 面板自身
        if (uri.contains(".")) {
            return false;
        }
        return !uri.startsWith("/api") && !uri.equals("/stats");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}