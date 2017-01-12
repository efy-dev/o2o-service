package com.efeiyi.ec.security.auth;

import com.efeiyi.ec.security.exception.AuthenticationException;
import com.efeiyi.ec.security.model.UserDetails;

/**
 * Created by Administrator on 2016/12/20 0020.
 */
public interface UserDetailsService {

    UserDetails getUserByUsername(String username) throws AuthenticationException;

    UserDetails getUserByUnionId(String unionId) throws AuthenticationException;

}
