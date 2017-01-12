package com.efeiyi.ec.website.base.controller;

import com.efeiyi.ec.model.Artistry;
import com.efeiyi.ec.model.Consumer;
import com.efeiyi.ec.security.auth.AbstractSecurity;
import com.efeiyi.ec.security.exception.AuthenticationException;
import com.efeiyi.ec.security.model.UserDetails;
import com.efeiyi.ec.website.organization.service.IConsumerService;
import com.ming800.core.base.service.BaseManager;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

/**
 * Created by WangTao on 2016/11/22 0022.
 * O2O项目相关接口拆分出来独立维护，其他代码不维护
 */
@Controller
public class YuanquController {


    private BaseManager baseManager;

    private IConsumerService consumerService;

    private AbstractSecurity abstractSecurity;

    @Autowired
    public YuanquController(BaseManager baseManager, IConsumerService consumerService, AbstractSecurity abstractSecurity) {
        this.baseManager = baseManager;
        this.consumerService = consumerService;
        this.abstractSecurity = abstractSecurity;
    }

    @RequestMapping({"weixinLogin"})
    @ResponseBody
    public JSONObject weixinLogin(HttpServletRequest request) {
        String nickname = request.getParameter("nickname");
        String sex = request.getParameter("sex");
        String city = request.getParameter("city");
        String headimgurl = request.getParameter("headimgurl");
        String unionId = request.getParameter("unionid");
        UserDetails userDetails;
        JSONObject jsonObject = new JSONObject();
        try {
            userDetails = abstractSecurity.authenticateByUnionId(request, unionId);
        } catch (AuthenticationException ae) {
            userDetails = null;
        }

        if (userDetails == null) {
            consumerService.saveOrUpdateConsumer(nickname, unionId, city, headimgurl, Integer.parseInt(sex));
        } else {
            baseManager.getObject(Consumer.class.getName(), userDetails.getId());
        }
        try {
            userDetails = abstractSecurity.authenticateByUnionId(request, unionId);
        } catch (AuthenticationException ae) {
            jsonObject.put("msg", ae.getMessage());
            return jsonObject;
        }

        jsonObject.put("id", userDetails.getId());
        jsonObject.put("username", userDetails.getUsername());

        return jsonObject;
    }


    @RequestMapping({"/project/hasArtistry"})
    @ResponseBody
    public String hasArtistryByProject(HttpServletRequest request) {
        String projectId = request.getParameter("projectId");
        LinkedHashMap<String, Object> param = new LinkedHashMap<>();
        String hql = "select obj from Artistry obj where obj.project.id=:projectId";
        param.put("projectId", projectId);
        Artistry artistry;
        try {
            artistry = (Artistry) baseManager.getUniqueObjectByConditions(hql, param);
        } catch (Exception e) {
            return "1";
        }
        if (artistry == null) {
            return "1";
        } else {
            return "0";
        }
    }


    //@TODO 实现
    @RequestMapping({"/qrCodeAdapter"})
    public String qrCodeAdapter(HttpServletRequest request) {
        String dataType = request.getParameter("dataType");
        String dataId = request.getParameter("dataId");
        return "";
    }
}
