package com.mmall.Converter;

import com.mmall.pojo.Product;
import com.mmall.utils.PropertiesUtils;
import com.mmall.vo.ProductListVo;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ProductListConverterProdcutVoList {


    public static ProductListVo productListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        BeanUtils.copyProperties(product, productListVo);
        productListVo.setImageHost(PropertiesUtils.getProperty("ftp.server.http.prefix"));
        return productListVo;
    }

    public static List<ProductListVo> productListVoList(List<Product> products) {

        return products.stream().map(e -> productListVo(e)).collect(Collectors.toList());

    }


}
