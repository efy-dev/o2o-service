package com.efeiyi.ec.security.auth;


import com.efeiyi.ec.security.model.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IsAuthenticatedSecurityStrategy extends AbstractSecurity {


    private List<String> urlList = new CopyOnWriteArrayList<>();

    public IsAuthenticatedSecurityStrategy(UserDetailsService userDetailsService, String loginUrl, List<String> urlList) {
        super(userDetailsService, loginUrl);
        this.urlList = urlList;
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest request) {
        boolean isNeedAuthenticate = false;
        boolean result;
        String requestUrl = request.getRequestURI();
        for (String url : urlList) {
            if (url.endsWith("**")) {
                String urlTemp = url.replaceAll("\\*", "");
                if (requestUrl.startsWith(urlTemp)) {
                    isNeedAuthenticate = true;
                }
            } else {
                if (url.equals(requestUrl)) {
                    isNeedAuthenticate = true;
                }
            }

        }

        if (isNeedAuthenticate) {
            HttpSession session = request.getSession();
            UserDetails userDetails = (UserDetails) session.getAttribute("userDetails");
            result = userDetails != null;
        } else {
            result = true;
        }

        return result;
    }


    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }
}
