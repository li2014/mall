package com.scoprion.mall.wx.controller;

import com.scoprion.mall.domain.Suggest;
import com.scoprion.mall.wx.service.user.WxUserService;
import com.scoprion.result.BaseResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ycj
 * @version V1.0 <>
 * @date 2017-11-17 10:15
 */
@RestController
@RequestMapping("/wx/member")
public class WxMemberController {

    @Autowired
    WxUserService wxUserService;

    @ApiOperation("意见、建议")
    @RequestMapping(value = "/suggest", method = RequestMethod.POST)
    public BaseResult suggest(@RequestBody Suggest suggest) {
        return wxUserService.suggest(suggest);
    }
}
