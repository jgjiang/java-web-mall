package com.jmall.controller.backend;

import com.jmall.common.Const;
import com.jmall.common.ResponseCode;
import com.jmall.common.ServerResponse;
import com.jmall.pojo.User;
import com.jmall.service.ICategoryService;
import com.jmall.service.IUserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue="0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"user does not login, please login first");
        }

        // check if is admin
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()) {
            // if admin, add logic to handle adding category
            return iCategoryService.addCategory(categoryName,parentId);

        } else {
            return ServerResponse.createByErrorMessage("no permission to add category");
        }
    }


    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"user does not login, please login first");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()) {
            // update category name
            return iCategoryService.updateCategoryName(categoryId, categoryName);

        }else {
            return ServerResponse.createByErrorMessage("no permission to add category");
        }
    }
}
