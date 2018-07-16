package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.utils.PropertiesUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * save和更新都用这个接口
     *如果产品有id则为更新，如果没有则为新增
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServiceResponse productSave(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }


    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServiceResponse setProductStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }


    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iProductService.manageProductDetail(productId);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }


    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }

    /**
     * 对商品进行查询
     *
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServiceResponse ProductSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }

    /**
     * 实现文件的上传
     *
     * @param multipartFile
     * @param httpServletRequest
     * @return
     */

    @RequestMapping("upload.do")
    @ResponseBody
    public ServiceResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile multipartFile, HttpServletRequest httpServletRequest) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServiceResponse.createByCodeMessage(10, "用户未登陆，请进行登陆");
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加产品的逻辑
            String path = httpServletRequest.getSession().getServletContext().getRealPath("upload");//找到部署项目的目录下面，就是tomcat服务器下面
            String targetFileName = iFileService.upload(multipartFile, path);
            String url = PropertiesUtils.getProperty("ftp.server.http.prefix") + targetFileName;//uuid生成的唯一的文件识别码
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServiceResponse.createBySuccess(fileMap);
        } else {
            return ServiceResponse.createByErrorMessage("不是管理员，没有权限进行操作");
        }
    }


    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile multipartFile, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Map result = Maps.newHashMap();
        if (user == null) {
            result.put("success", false);
            result.put("msg", "请登录管理员");
            return result;
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
            /*除了返回的形式，还有返回头需要改
             * {
             *            "success": true/false,
             *                 "msg": "error message", # optional
             *            "file_path": "[real file path]"
             *         }
             */
            String path = httpServletRequest.getSession().getServletContext().getRealPath("upload");
            String targetName = iFileService.upload(multipartFile, path);
            if (StringUtils.isBlank(targetName)) {
                result.put("success", false);
                result.put("msg", "上传失败");
                return result;
            }
            String url = PropertiesUtils.getProperty("ftp.server.http.prefix")+targetName;
            result.put("success", true);
            result.put("msg", "上传成功");
            result.put("url", url);
            httpServletResponse.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return result;
        } else {
            result.put("success", false);
            result.put("msg", "无权进行操作");
            return result;
        }

    }


}
