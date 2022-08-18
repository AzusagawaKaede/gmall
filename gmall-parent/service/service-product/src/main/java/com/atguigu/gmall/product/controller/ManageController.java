package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/28
 * Description: 后台管理的控制层
 */
@RestController
@RequestMapping("/admin/product")
public class ManageController {

    @Autowired
    private ManageService manageService;

    /**
     * 获取一级分类
     *
     * @return
     */
    @GetMapping("/getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> category1List = manageService.getCategory1();
        return Result.ok(category1List);
    }

    /**
     * 根据一级分类id，获取二级分类
     *
     * @param category1Id
     * @return
     */
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id) {
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    /**
     * 根据二级分类id，查询三级分类
     *
     * @param category2Id
     * @return
     */
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id) {
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    /**
     * 新增平台属性
     *
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveBaseAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 根据选中的分类查询平台属性值
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id) {
        List<BaseAttrValue> baseAttrInfoList = manageService.getBaseAttrInfo(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * 根据平台属性id查询对应的BaseAttrValue列表
     *
     * @param baseAttrInfoId
     * @return
     */
    @GetMapping("/getAttrValueList/{baseAttrInfoId}")
    public Result getAttrValueList(@PathVariable Long baseAttrInfoId) {
        List<BaseAttrValue> attrValueList = manageService.getAttrValueList(baseAttrInfoId);
        return Result.ok(attrValueList);
    }

    /**
     * 查询所有品牌
     *
     * @return
     */
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        return Result.ok(manageService.getBaseTrademark());
    }

    /**
     * 查询所有的销售属性
     *
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList() {
        return Result.ok(manageService.getBaseSaleAttr());
    }

    /**
     * 保存或修改SpuInfo、SpuImage、SpuSaleAttr、SpuSaleAttrValue
     *
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * 根据三级分类id，分页查询SpuInfo
     *
     * @param pageNum
     * @param pageSize
     * @param category3Id
     * @return
     */
    @GetMapping("/{pageNum}/{pageSize}")
    public Result querySpuInfoPageByCategory3Id(@PathVariable Long pageNum,
                                                @PathVariable Long pageSize,
                                                Long category3Id) {
        Page<SpuInfo> page = manageService.querySpuInfoPageByCategory3Id(pageNum, pageSize, category3Id);
        return Result.ok(page);
    }

    /**
     * 根据spuId查询spu图片列表
     *
     * @param spuId
     * @return
     */
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId) {
        return Result.ok(manageService.querySpuImageBySpuId(spuId));
    }

    /**
     * 根据spuId查询对应销售属性列表
     * @param spuId
     * @return
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId){
        return Result.ok(manageService.querySpuSaleAttrBySpuId(spuId));
    }

    /**
     * 保存或修改skuInfo
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * 分页查询skuInfo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/list/{pageNum}/{pageSize}")
    public Result list(@PathVariable Long pageNum, @PathVariable Long pageSize){
        return Result.ok(manageService.list(pageNum, pageSize));
    }

    /**
     * 上架方法
     * @param skuId
     * @return
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        manageService.upOrDownSku(skuId, ProductConst.SKUINFO_ON_SALE);
        return Result.ok();
    }

    /**
     * 下架方法
     * @param skuId
     * @return
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        manageService.upOrDownSku(skuId, ProductConst.SKUINFO_CANCEL_SALE);
        return Result.ok();
    }

    /**
     * 品牌分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/baseTrademark/{pageNum}/{pageSize}")
    public Result baseTrademark(@PathVariable Long pageNum, @PathVariable Long pageSize){
        return Result.ok(manageService.queryBaseTrademarkPage(pageNum, pageSize));
    }

    /**
     * 根据id删除品牌
     * @param id
     * @return
     */
    @DeleteMapping("/baseTrademark/remove/{id}")
    public Result remove(@PathVariable Long id){
        manageService.deleteTrademark(id);
        return Result.ok();
    }

    /**
     * 新增品牌
     * @param baseTrademark
     * @return
     */
    @PostMapping("/baseTrademark/save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        manageService.insertBaseTrademark(baseTrademark);
        return Result.ok();
    }

    /**
     * 根据id获取品牌
     * @param id
     * @return
     */
    @GetMapping("/baseTrademark/get/{id}")
    public Result getBaseTrademark(@PathVariable Long id){
        return Result.ok(manageService.getBaseTrademarkById(id));
    }

    /**
     * 修改品牌
     * @param baseTrademark
     * @return
     */
    @PutMapping("/baseTrademark/update")
    public Result updateBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        manageService.insertBaseTrademark(baseTrademark);
        return Result.ok();
    }
}
