package com.efeiyi.ec.website.base.util;


import com.efeiyi.ec.model.MyUser;
import com.efeiyi.ec.model.Role;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * @author WuYingbo
 */
public class AuthorizationUtil {


    public static MyUser getMyUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object userObject = session.getAttribute("userDetails");
        if (userObject != null) {

            return (MyUser) userObject;
        } else {
            MyUser myUser = new MyUser();
            Role role = new Role();
            role.setBasicType("all");
            myUser.setRole(role);
            return myUser;
        }

    }

    public static String getCurrentSampleUsername(HttpServletRequest request) {
        String username = getMyUser(request).getUsername();
        return username.substring(0, 3) + "****" + username.substring(7, 11);
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        try {
            return getMyUser(request).getId() != null;
        } catch (Exception e) {
            return false;
        }
    }

}
