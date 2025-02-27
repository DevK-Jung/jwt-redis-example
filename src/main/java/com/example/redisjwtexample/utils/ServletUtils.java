package com.example.redisjwtexample.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@UtilityClass
public class ServletUtils {
    public Optional<HttpServletRequest> getServletRequest() {
        return getRequestAttributes().map(ServletRequestAttributes::getRequest);
    }

    public Optional<HttpServletResponse> getServletResponse() {
        return getRequestAttributes().map(ServletRequestAttributes::getResponse);
    }

    public Optional<String> getHeader(@NonNull String key) {
        return getServletRequest().map(req -> req.getHeader(key));
    }

    private Optional<ServletRequestAttributes> getRequestAttributes() {
        return Optional.of((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
    }

}
