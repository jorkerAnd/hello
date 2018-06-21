package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
@Slf4j
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 目录的添加
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServiceResponse addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || parentId == null) {
            return ServiceResponse.createByErrorMessage("添加商品参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0)
            return ServiceResponse.createBySuccess("添加商品成功");
        return ServiceResponse.createByErrorMessage("添加商品失败");

    }

    public ServiceResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (StringUtils.isBlank(categoryName) || categoryId == null) {
            return ServiceResponse.createByErrorMessage("更新商品参数错误");
        }
        if (categoryMapper.selectByPrimaryKey(categoryId) == null)
            return ServiceResponse.createByErrorMessage("需要更新的商品类目不存在");
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        //根据主键进行有选择性的更新
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0)
            return ServiceResponse.createBySuccess("更新类目成功");
        return ServiceResponse.createByErrorMessage("更新类目失败");


    }


    public ServiceResponse<List<Category>> getChildenParallelCategory(Integer categoryId) {

        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            log.info("未找到当前分类的子分类");
        }
        /**
         * 无论categoryList是否为空，都要向前端返回list，
         */
        return ServiceResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点的id和子节点的id
     *
     * @param
     * @return
     */
    public ServiceResponse selectCategoryAndChildrenById(Integer categoryId) {
        /**
         * categoryId一定有默认值
         */
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet, categoryId);
        List<Integer> categoryList = Lists.newArrayList();
        for (Category category : categorySet) {
            categoryList.add(category.getId());
        }
        return ServiceResponse.createBySuccess(categoryList);
    }

    /**
     * 递归算法，算出本节点和子节点
     *
     * @return
     */
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null)
            categorySet.add(category);
//并不会出现null的情况，这是因为sql操作的内部逻辑原理,
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
//如果找不到对应的id的category则不会进行下面的循环
        for (Category category1 : categoryList) {
            findChildCategory(categorySet, category1.getId());
        }
        return categorySet;
    }


}

