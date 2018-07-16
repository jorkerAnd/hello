package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.StringUtil;
import com.google.common.collect.Lists;
import com.mmall.Converter.ProductListConverterProdcutVoList;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.utils.DateTimeUtils;
import com.mmall.utils.PropertiesUtils;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    /**
     * 插入和更新放在一个方法里面，插入还是更新放在方法内部进行判断
     *
     * @return
     */
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;

    public ServiceResponse saveOrUpdateProduct(Product product) {
        if (product == null)
            return ServiceResponse.createByErrorMessage("新增或者更新的参数不正确");

        if (StringUtils.isNotBlank(product.getSubImages())) {
            String[] subImageArray = product.getSubImages().split(",");
            if (subImageArray.length > 0)
                product.setMainImage(subImageArray[0]);
        }
/**
 * 判断id是否为空，若为空则为插入新的商品，如果不为空，则是进行更新
 */
        if (product.getId() != null) {
            if (productMapper.updateByPrimaryKeySelective(product) > 0)
                return ServiceResponse.createBySuccessMessage("更新成功");
            return ServiceResponse.createByErrorMessage("更新失败");
        } else {
            if (productMapper.insertSelective(product) > 0)
                return ServiceResponse.createBySuccessMessage("创建新产品成功");
            return ServiceResponse.createByErrorMessage("创建新产品失败");
        }
    }

    /**
     * 因为商品的状态有3个属性，所以不能使用myabatis的类型转换，使其转为Boolean类型
     *
     * @param productId
     * @param status
     * @return
     */
    public ServiceResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0)
            return ServiceResponse.createBySuccess("修改产品销售状态成功");
        return ServiceResponse.createByErrorMessage("修改产品失败");

    }

    public ServiceResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null)
            return ServiceResponse.createByErrorMessage("商品不存在");
        //开始拼装数据
        ProductDetailVo productDetailVo = assemableProductDetail(product);
        return ServiceResponse.createBySuccess(productDetailVo);
    }

    //todo
    private ProductDetailVo assemableProductDetail(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        //todo BeanUtils.copyProperties(product, productDetailVo);//将第一个的属性传到

        productDetailVo.setImageHost(PropertiesUtils.getProperty("ftp.server.http.prefix", "http://image.imooc.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setCategoryId(0);
        } else {
            productDetailVo.setParentCategord(category.getParentId());
        }
  /*
  因为从数据库中拿出数据是毫秒的形式
   */
        //createtime

        productDetailVo.setCreateTime(DateTimeUtils.dateToStr(product.getCreateTime()));
        //updatetime

        productDetailVo.setUpdateTime(DateTimeUtils.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    /**
     * 后台管理的商品分页和动态排序功能
     */
    public ServiceResponse<PageInfo> getProductList(int pageNum, int pageSize) {
/**
 * pageHepler的使用方法
 * 1.startPag--start
 * 2.填充自己的sql查询逻辑
 * 3.pageHepler收尾
 */
        //1
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVos = ProductListConverterProdcutVoList.productListVoList(productList);
        //2
        /**
         * PageInfo后面的参数必须是produtList，得用这个来进行分页的处理
         */
        PageInfo pageInfo = new PageInfo(productList);
        /**
         * 因为需要展现前端的是productVo的对象，所以再将其setList设为我们转换成功的List
         */
        pageInfo.setList(productListVos);
        return ServiceResponse.createBySuccess(pageInfo);
    }


    /**
     * 进行商品的一个查询，传过来的是productid或者productname或者两个都传，不可以两个都不进行传递
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServiceResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        if (StringUtils.isBlank(productName) && productId == null) {
            return ServiceResponse.createByErrorMessage("查找信息不正确");
        }
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName))
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        /**
         * 是空的也没有关系
         */
        List<Product> products = productMapper.selectByNameAndProductId(StringUtils.isBlank(productName)?null:productName, productId);
        List<ProductListVo> productListVos = ProductListConverterProdcutVoList.productListVoList(products);
        PageInfo pageInfo = new PageInfo(products);
        pageInfo.setList(productListVos);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    public ServiceResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null)
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null)
            return ServiceResponse.createByErrorMessage("商品不存在");
        //开始拼装数据
        if (product.getStatus() != Const.productStatusEnum.ON_SALE.getCode())
            return ServiceResponse.createByErrorMessage("产品已经下架或者被删除");
        ProductDetailVo productDetailVo = assemableProductDetail(product);
        return ServiceResponse.createBySuccess(productDetailVo);
    }

    public ServiceResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId,
                                                                 int pageNum, int pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        }
        List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null) {
            Category category1 = categoryMapper.selectByPrimaryKey(categoryId);
            if (category1 == null && StringUtils.isBlank(keyword)) {
                //当没有该分类并且没有关键字的时候
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServiceResponse.createBySuccess(pageInfo);
            }//拿到父类和子类的全部id
            /**
             * 其categorylist和keyword绝对有一存在
             */
            categoryList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        if (StringUtils.isNotBlank(keyword))
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();

        PageHelper.startPage(pageNum, pageSize);
        /**
         * 动态排序处理
         */
        if (StringUtils.isNotBlank(orderBy)) {
            /**
             * 细节，对于list.contains的时间复杂度是n
             * 而set的时间复杂度是1,所以为了效率一般尽量使用set,
             * 如果不进行orderby的传参，那么就不进行排序了
             */
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }

        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword, categoryList.size() == 0 ? null : categoryList);
        List<ProductListVo> productListVoList = ProductListConverterProdcutVoList.productListVoList(productList);
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServiceResponse.createBySuccess(pageInfo);


    }


}
