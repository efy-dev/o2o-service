package com.efeiyi.ec.website.organization.service.impl;

import com.efeiyi.ec.model.MyUser;
import com.efeiyi.ec.security.model.UserDetails;
import com.ming800.core.base.service.BaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 12-10-15
 * Time: 下午5:02
 * To change this template use File | Settings | File Templates.
 */

@Service
@Transactional
public class UserManagerImpl {

    @Autowired
    private BaseManager baseManager;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        String queryStr = "SELECT u FROM MyUser u WHERE u.username=:username AND u.status != 0";
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("username", username);
        try {
            MyUser myUser = (MyUser) baseManager.getUniqueObjectByConditions(queryStr, queryParamMap);
            if (myUser == null) {
                throw new UsernameNotFoundException("用户名或密码错误");
            } else {
                return myUser;
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

    }
}
