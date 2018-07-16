package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

//todo 因为再进行每操作之前都要进行是否为管理员的操作，在想可以创建一个切面，来每次都从seesionz中进行判断，如果不是管理员，则抛出一个异常，再进行异常的捕捉
@Controller
@RequestMapping("manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServiceResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parenId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请先登陆");
        //再进行校验是否未管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
//是管理员。开始处理逻辑
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServiceResponse.createByErrorMessage("无权进行操作，需要管理员进行操作");
        }
    }


    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServiceResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请先登陆");
         //再进行校验是否未管理员
        if (iUserService.checkAdminRole(user).isSuccess())
        {
         //是管理员,更新目录
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        } else
        {
            return ServiceResponse.createByErrorMessage("无权进行操作，需要管理员进行操作");
        }
    }


    /**
     * 查询指定节点的子节点(同级节点)，且不进行递归
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category_name.do")
    @ResponseBody
    public ServiceResponse<List<Category>> getChildrenParalleCategory(HttpSession session, @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){
    User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        return ServiceResponse.createByCodeMessage(10, "用户未登陆，请先登陆");
    //再进行校验是否未管理员
    if (iUserService.checkAdminRole(user).isSuccess())
    {
        //是管理员,查询子节点
        return iCategoryService.getChildenParallelCategory(categoryId);
    } else
    {
        return ServiceResponse.createByErrorMessage("无权进行操作，需要管理员进行操作");
    }
    }


    /**
     * 查询指定节点和其递归子节点
     * @param session
     * @param categoryId
     * @returngit
     */
    @RequestMapping("get_deep_name.do")
    @ResponseBody
    public ServiceResponse<List<Category>> getCategoryAndDeepChildCategory(HttpSession session, @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请先登陆");
        //再进行校验是否未管理员
        if (iUserService.checkAdminRole(user).isSuccess())
        {
         return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else
        {
            return ServiceResponse.createByErrorMessage("无权进行操作，需要管理员进行操作");
        }
    }







}
