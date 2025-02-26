package com.example.redisjwtexample.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@UtilityClass
public class ServletUtils {
    public HttpServletRequest getServletRequest() {
        return Optional.of(RequestContextHolder.getRequestAttributes())
                .map(ra -> ((ServletRequestAttributes) ra).getRequest())
                .orElse(null);
    }

    public HttpServletResponse getServletResponse() {
        return Optional.of(RequestContextHolder.getRequestAttributes())
                .map(ra -> ((ServletRequestAttributes) ra).getResponse())
                .orElse(null);
    }
}
