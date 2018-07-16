package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.utils.BigDecimalUtil;
import com.mmall.utils.DateTimeUtils;
import com.mmall.utils.FTPUtil;
import com.mmall.utils.PropertiesUtils;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProudcutVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.ServletCookieValueMethodArgumentResolver;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Jorker
 * @date 2018/7/2 21:16
 */
@Slf4j
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;


    public ServiceResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSzie) {
        PageHelper.startPage(pageNum, pageSzie);
        /**
         * 如果不存在，就直接返回一个长度为0的list对象
         */
        List<Order> orderList = orderMapper.selectByUserId(userId);

        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
/**
 * 先调用dao层的对象
 */
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageInfo);


    }

    /**
     * 将List<Order>拼接成List<OrderVo>
     * 根据userid是否为空来进行判断是不是管理员
     *
     * @param orderList
     * @param userId
     * @return
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //todo 不是管理员时的一个查询
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.getByUserIdAndOrderNo(userId, order.getOrderNo());

            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;

    }


    public ServiceResponse<OrderVo> getOrderDeatil(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderno(userId, orderNo);
        if (order == null)
            return ServiceResponse.createByErrorMessage("订单不存在");
        List<OrderItem> orderItemList = orderItemMapper.getByUserIdAndOrderNo(userId, orderNo);
        if (CollectionUtils.isEmpty(orderItemList))
            return ServiceResponse.createByErrorMessage("订单详情为空");

        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServiceResponse.createBySuccess(orderVo);


    }


    public ServiceResponse getOrderCartProduct(Integer userId) {
        OrderProudcutVo orderProudcutVo = new OrderProudcutVo();
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServiceResponse serviceResponse = getCartOrderItem(userId, cartList);
        if (!serviceResponse.isSuccess())
            return serviceResponse;
        List<OrderItem> orderItemList = (List<OrderItem>) serviceResponse.getData();
        if (CollectionUtils.isEmpty(orderItemList))
            return ServiceResponse.createByErrorMessage("购物车为空");
        List<OrderItemVo> orderItemVoList = orderItemToOrderItemVo(orderItemList);
        BigDecimal orderPayement = getOrderToalPrice(orderItemList);
        orderProudcutVo.setImageHost(PropertiesUtils.getProperty("ftp.server.http.prefix"));
        orderProudcutVo.setOrderItemVoList(orderItemVoList);
        orderProudcutVo.setProductTotalPrice(orderPayement);
        return ServiceResponse.createBySuccess(orderProudcutVo);
    }


    public ServiceResponse<String> cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderno(userId, orderNo);
        if (order == null)
            return ServiceResponse.createByErrorMessage("订单不存在");
        if (order.getStatus() != Const.OrderStatusEnnum.NO_PAY.getCode())
            return ServiceResponse.createByErrorMessage("已经付款，无法进行退款");
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnnum.CANCEL.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0)
            return ServiceResponse.createBySuccess("success");
        return ServiceResponse.createByErrorMessage("failed");
    }


    public ServiceResponse<Map> pay(Integer userId, Long orderNo, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderno(userId, orderNo);
        if (order == null)
            return ServiceResponse.createByErrorMessage("查询订单失败");
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymmall扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持，（可以认为是连锁店）
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        //查找对应的orderItem的列表
        List<OrderItem> orderItemList = orderItemMapper.getByUserIdAndOrderNo(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goodsDetail = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(), orderItem.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).
                        setTotalAmount(totalAmount).
                        setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).
                        setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).
                        setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtils.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();


        /**
         * 主要是看返回的结果,一切的返回信息都会存在到返回信息当中的,进入到设置的进入回调的网址
         */
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");
                //第一次收到支付宝的回调通知，通知也可能为null
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                //开始进行整合
                File file = new File(path);//入参path，
                if (!file.exists()) {
                    file.setWritable(true);
                    file.mkdirs();
                }
                // 需要修改为运行机器上的路径
                //细节，因为为拼装路径，所以要注意”/“是否书写的正确
                String qrPath = String.format(path + "/qr-%s.png",
                        response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                log.info("filePath:" + qrPath);
                //支付宝封装的方法，将支付宝返回的二维码放在指定的文件目录下面
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrFileName);
                //将二维码上传到ftp服务器上面
                File targetFile = new File(path, qrFileName);
                Boolean isSuccess = false;

                try {
                    isSuccess = FTPUtil.uoloadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("上传二维码异常", e);
                }
                if (!isSuccess)
                    return ServiceResponse.createByErrorMessage("上传到ftp服务器发生了错误");


                log.info("qrPath:" + qrPath);
                String qUrl = PropertiesUtils.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qUrl);
                return ServiceResponse.createBySuccess(resultMap);
            case FAILED:
                log.error("系统异常，预下单失败!!!");
                return ServiceResponse.createByErrorMessage("系统异常，预下单失败!!!");
            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServiceResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServiceResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");

        }


    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    /**
     * 进行一个回调的验证
     *
     * @param params
     * @return
     */
    public ServiceResponse aliCallback(Map<String, String> params) {

        /**
         * 先将下面三个变量拿出然后进行回调的验证
         */
        Long ordeNo = Long.valueOf(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(ordeNo);
        if (ordeNo == null)
            return ServiceResponse.createByErrorMessage("不是本商店的订单");
        if (order.getStatus() >= Const.OrderStatusEnnum.PAID.getCode())
            return ServiceResponse.createBySuccess("支付宝重复调用");
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {//如果交易成功的话，将其信息更改为交易成功的界面
            order.setPaymentTime(DateTimeUtils.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //payInfo的记录只要是回调就应该被记录一次
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(ordeNo);
        payInfo.setPayPlatform(Const.PayPlatfromEnum.ALIPAY.getCode());
        payInfo.setPlatfromNumber(tradeNo);
        payInfo.setPlatfromStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServiceResponse.createBySuccess();
    }

    public ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderno(userId, orderNo);
        if (order == null)
            return ServiceResponse.createByErrorMessage("用户没有该订单");
        if (order.getStatus() >= Const.OrderStatusEnnum.PAID.getCode())
            return ServiceResponse.createBySuccess();
        return ServiceResponse.createByError();

    }

    /**
     * 进行订单的查询
     *
     * @param userId
     * @param shippingId
     * @return
     */
    public ServiceResponse createOrder(Integer userId, Integer shippingId) {
        //从cart数据库中取出对应userid并且为选中的商品
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //计算商品的总价
        ServiceResponse serviceResponse = getCartOrderItem(userId, cartList);
        if (!serviceResponse.isSuccess())
            return serviceResponse;//判断了购物车是否存在
/**
 * 进行强转
 */
        List<OrderItem> orderItemList = (List<OrderItem>) serviceResponse.getData();
        if (CollectionUtils.isEmpty(orderItemList))
            return ServiceResponse.createByErrorMessage("购物车为空");
        BigDecimal payment = getOrderToalPrice(orderItemList);
        //生成订单
        Order order = assemableOrder(userId, shippingId, payment);
        if (order == null)
            return ServiceResponse.createByErrorMessage("生成订单错误");

        //需要将orderItem中的order详情放进订单号
        for (OrderItem orderItem : orderItemList)
            orderItem.setOrderNo(order.getOrderNo());

        //mybatis的批量插入
        orderItemMapper.batchInsert(orderItemList);
        //生成成功，减少库存
        //清空购物车
        cleanCart(cartList);
        //返回给前端的明细assembleOrderVo
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServiceResponse.createBySuccess(orderVo);
    }

    /**
     * 多复用的类
     *
     * @param order
     * @param orderItemList
     * @return
     */

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeof(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setCreateTime(DateTimeUtils.dateToStr(new Date()));
        orderVo.setStatusDesc(Const.OrderStatusEnnum.codeof(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        /**
         * 有的话就进行设置，以后可以
         */
        orderVo.setPaymentTime(DateTimeUtils.dateToStr(order.getPaymentTime()));
        orderVo.setCloseTime(DateTimeUtils.dateToStr(order.getCloseTime()));
        orderVo.setEndTime(DateTimeUtils.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtils.dateToStr(order.getCreateTime()));
        orderVo.setImageHost(PropertiesUtils.getProperty("ftp.server.http.prefix"));
        orderVo.setOrderItemList(orderItemToOrderItemVo(orderItemList));
        return orderVo;
    }

    /**
     * 将list<OrderItem>拼装成List<OrderItemVo>
     *
     * @param orderItemList
     * @return
     */
    private List<OrderItemVo> orderItemToOrderItemVo(List<OrderItem> orderItemList) {

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setCurentUnitPrice(orderItem.getCurentUnitPrice());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
            orderItemVo.setCreateTime(DateTimeUtils.dateToStr(orderItem.getCreateTime()));
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }


    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }


    /**
     * 减少库存的方法
     *
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }
    /**
     * 清空购物车
     *
     * @param cartList
     */

    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());

        }
    }


    private Order assemableOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        //订单id是自增的,没有设置paymentTime
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(Const.OrderStatusEnnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ON_LINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //发货时间
        //付款时间
        int rowcount = orderMapper.insert(order);
        if (rowcount > 0)
            return order;
        return null;
    }

    /**
     * 订单号的生成是很重要的
     * 防止并发情况发生
     *
     * @return
     */
    private synchronized long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + currentTime % 9;
    }

    private BigDecimal getOrderToalPrice(List<OrderItem> orderItemListd) {
        BigDecimal toalAmout = new BigDecimal(0);
        for (OrderItem orderItem : orderItemListd) {
            toalAmout = BigDecimalUtil.add(orderItem.getTotalPrice().doubleValue(), toalAmout.doubleValue());
        }

        return toalAmout;
    }

    /**
     * 将LisT<Cart>转换为List<OrderItem>
     * 如果success则为转换成功
     *
     * @param userId
     * @param carts
     * @return
     */
    private ServiceResponse getCartOrderItem(Integer userId, List<Cart> carts) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(carts))
            return ServiceResponse.createByErrorMessage("购物车为空");
        //校验购物车的数据，包括产品的数量和状态
        for (Cart cart : carts) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product == null || Const.productStatusEnum.ON_SALE.getCode() != product.getStatus())
                return ServiceResponse.createByErrorMessage(  " 商品不存在或者是下架状态，不可选");
            //虽然在加入购物车的时候已经校验库存，但是在下单的时候还是需要加入库存大小的验证
            if (cart.getQuantity() > product.getStock()) {
                return ServiceResponse.createByErrorMessage("库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            //一定要存入实时的价格
            orderItem.setCurentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCreateTime(new Date());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity().doubleValue(), product.getPrice().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServiceResponse.createBySuccess(orderItemList);

    }


    //backend

    public ServiceResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServiceResponse.createBySuccess(pageInfo);

    }


    public ServiceResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null)
            return ServiceResponse.createByErrorMessage("该订单不存在");
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServiceResponse.createBySuccess(orderVo);

    }

    /**
     * 支持模糊查询
     */
    public ServiceResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo pageResult = assembleOrderVo(order, orderItemList);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(pageResult));
            return ServiceResponse.createBySuccess(pageInfo);
        }
        return ServiceResponse.createByErrorMessage("订单不存在");


    }

    public ServiceResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null)
            return ServiceResponse.createByErrorMessage("该订单不存在");
        if (order.getStatus() == Const.OrderStatusEnnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServiceResponse.createBySuccess("发货成功");
        }
        return ServiceResponse.createByErrorMessage("订单状态不正确");

    }


}