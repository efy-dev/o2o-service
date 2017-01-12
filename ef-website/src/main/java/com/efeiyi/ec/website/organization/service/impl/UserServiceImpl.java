package com.efeiyi.ec.website.organization.service.impl;

import com.efeiyi.ec.security.exception.AuthenticationException;
import com.efeiyi.ec.security.model.UserDetails;
import com.efeiyi.ec.website.organization.service.UserService;
import com.ming800.core.base.service.BaseManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;


public class UserServiceImpl implements UserService {

    @Autowired
    private BaseManager baseManager;


    @Override
    public UserDetails getUserByUsername(String username) throws AuthenticationException {
        String hql = "select obj from MyUser obj where obj.username=:username and obj.status!='0'";
        LinkedHashMap<String, Object> param = new LinkedHashMap<>();
        param.put("username", username);
        UserDetails userDetails = (UserDetails) baseManager.getUniqueObjectByConditions(hql, param);
        return userDetails;
    }

    @Override
    public UserDetails getUserByUnionId(String unionId) throws AuthenticationException {
        String hql = "select obj from MyUser obj where obj.unionId=:unionId and obj.status!='0'";
        LinkedHashMap<String, Object> param = new LinkedHashMap<>();
        param.put("unionId", unionId);
        UserDetails userDetails = (UserDetails) baseManager.getUniqueObjectByConditions(hql, param);
        return userDetails;
    }
}
