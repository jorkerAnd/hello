package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.utils.BigDecimalUtil;
import com.mmall.utils.PropertiesUtils;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Jorker
 * @date 2018/6/28 17:59
 */
@Service("iCartService")
@Slf4j
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 无论是添加还是更新后都进行dp的校验，满足数据的合理性
     *
     * @param userId
     * @param productId
     * @param productNum
     * @return
     */

    public ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer productNum) {
        if (productId == null || productNum == null)//userId一定不会为空
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        if(productMapper.selectByPrimaryKey(productId)==null)
            return ServiceResponse.createByErrorMessage("商品不存在");

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartItem.setQuantity(productNum);
            cartMapper.insert(cartItem);
        } else {
            int count = cart.getQuantity() + productNum;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo = getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }

    public ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer productNum) {
        if (productId == null || productNum == null)//userId一定不会为空
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null)
            cart.setQuantity(productNum);
        /**
         * 是选择性的更新,s
         */
        cartMapper.updateByPrimaryKeySelective(cart);
        CartVo cartVo = getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }


    //可能删除多个productId
    public ServiceResponse<CartVo> delete(Integer userId, String productIds) {
        if (productIds == null)//userId一定不会为空
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        //todo
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        //CollectionUtils.isEmpty是判断size!=0并且集合不为空
        if (CollectionUtils.isEmpty(productIdList))
            return ServiceResponse.createByCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getMsg());
        cartMapper.deleteByUserIdProductIds(userId, productIdList);
        return list(userId);
    }


    public ServiceResponse<CartVo> list(Integer userId) {

        return ServiceResponse.createBySuccess(getCartVoLimit(userId));
    }

    /**
     * 确保在调用之前两个参数不为空
     *
     * @param userId
     * @param checked
     * @return
     */
    public ServiceResponse<CartVo> selectOrUnselect(Integer userId, Integer checked, Integer productId) {

        if(productId!=null){
            Cart cart=cartMapper.selectCartByUserIdProductId(userId,productId);
            if(cart==null)
                return ServiceResponse.createByErrorMessage("购物车中改列单不存在");
        }
        cartMapper.checkOrUncheckedProduct(userId, checked, productId);
        //cartMapper.checkOrUncheckedProduct(userId, checked,null);也可以这么改，方法中的productid删除，但是单独选的话还需要重新定义个方法，所以为了方法的高复用还是要放在方法的参数当中
        return list(userId);
    }

    public ServiceResponse<Integer> getCartProductCount(Integer userId) {
        return ServiceResponse.createBySuccess(cartMapper.selectCatrProcductCount(userId));
    }

    /**
     * 用之前先判断userId是否未null
     *
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId) {
        if (userId == null) {
            log.error("用户ID为空");
            return null;
        }
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartToalPrice = new BigDecimal("0");

        if (!CollectionUtils.isEmpty(cartList)) {
            /**
             *将cart对象拼装成cartProduct对象
             */
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        /**
                         * 因为数量大于了商品的库存，所以将购物车的数量改为最大数量
                         */
                        Cart cart = new Cart();
                        cart.setId(cartItem.getId());
                        cart.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cart);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    /**
                     * 因为Bigmeical中的参数为double类型，所以先将其设置double 类型
                     */
                    cartProductVo.setProductToalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                    if (cartProductVo.getProductChecked() == Const.Cart.CHECKED) {
                        //先计算单个商品的价格，然后计算总价格的时候将每个商品的总价格进行相加即可
                        cartToalPrice = BigDecimalUtil.add(cartToalPrice.doubleValue(), cartProductVo.getProductToalPrice().doubleValue());
                    }
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartToalPrice(cartToalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtils.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null)
            return false;
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0 ? true : false;
    }


}
