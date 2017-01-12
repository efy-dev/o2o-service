package com.efeiyi.ec.security.filter;

import com.efeiyi.ec.security.auth.AbstractSecurity;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2016/12/19 0019.
 */
public class SecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        AbstractSecurity security = (AbstractSecurity) WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()).getBean("securityStrategy");
        if (security.isAuthenticated(httpRequest)) {
            chain.doFilter(request, response);
        } else {
            security.saveRequest(httpRequest);
            httpResponse.sendRedirect(security.getLoginUrl());
        }

    }

    @Override
    public void destroy() {

    }
}
