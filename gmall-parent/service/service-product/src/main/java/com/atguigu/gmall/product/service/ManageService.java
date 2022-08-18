package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/7/28
 * Description: 后台管理的业务层
 */
public interface ManageService{

    /**
     * 查询所有的一级分类
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类id，查询二级分类
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级分类id，查询三级分类
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 新增平台属性
     * @param baseAttrInfo
     * @return
     */
    Boolean saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据选中的分类查询平台属性值
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrValue> getBaseAttrInfo(Long category1Id,
                                        Long category2Id,
                                        Long category3Id);

    /**
     * 根据平台属性id查询对应的BaseAttrValue列表
     * @param baseAttrInfoId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long baseAttrInfoId);

    /**
     * 查询所有的品牌
     * @return
     */
    List<BaseTrademark> getBaseTrademark();

    /**
     * 查询所有的销售属性
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttr();

    /**
     * 保存或修改SpuInfo、SpuImage、SpuSaleAttr、SpuSaleAttrValue
     * @param spuInfo
     * @return
     */
    Boolean saveSpuInfo(SpuInfo spuInfo);

    /**
     * spuInfo的条件分页查询
     * @param category3Id
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<SpuInfo> querySpuInfoPageByCategory3Id(Long pageNum, Long pageSize, Long category3Id);

    /**
     * 根据spuId查询spu图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> querySpuImageBySpuId(Long spuId);

    /**
     * 根据spuId查询所有的销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> querySpuSaleAttrBySpuId(Long spuId);

    /**
     * 保存或修改skuInfo
     * @param skuInfo
     * @return
     */
    Boolean saveSkuInfo(SkuInfo skuInfo);

    /**
     * 分页查询skuInfo
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<SkuInfo> list(Long pageNum, Long pageSize);

    /**
     * 上架或下架商品
     * @param skuId
     * @param status
     * @return
     */
    Boolean upOrDownSku(Long skuId, Short status);

    /**
     * 分页查询品牌
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<BaseTrademark> queryBaseTrademarkPage(Long pageNum, Long pageSize);

    /**
     * 根据id删除Trademark
     * @param id
     * @return
     */
    Boolean deleteTrademark(Long id);

    /**
     * 新增或修改品牌
     * @param baseTrademark
     * @return
     */
    Boolean insertBaseTrademark(BaseTrademark baseTrademark);

    /**
     * 根据id查询 BaseTrademark
     * @param id
     * @return
     */
    BaseTrademark getBaseTrademarkById(Long id);

}
