package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description:
 */
@RestController
@RequestMapping("/baseAttrValue")
public class BaseAttrValueController {

    @Autowired
    private BaseAttrValueService baseAttrValueService;

    /**
     * 新增BaseAttrValue
     * @param baseAttrValue
     * @return
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody BaseAttrValue baseAttrValue){
        baseAttrValueService.save(baseAttrValue);
        return Result.ok();
    }

    /**
     * 删除BaseAttrValue
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        baseAttrValueService.removeById(id);
        return Result.ok();
    }

    /**
     * 修改BaseAttrValue
     * @param baseAttrValue
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody BaseAttrValue baseAttrValue){
        baseAttrValueService.updateById(baseAttrValue);
        return Result.ok();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/queryById/{id}")
    public Result queryById(@PathVariable Long id){
        BaseAttrValue baseAttrValue = baseAttrValueService.getById(id);
        return Result.ok(baseAttrValue);
    }

    /**
     * 查询所有
     * @return
     */
    @GetMapping("/queryList")
    public Result queryList(){
        return Result.ok(baseAttrValueService.list(null));
    }

    /**
     * 条件查询
     * @param baseAttrValue
     * @return
     */
    @GetMapping("/queryByCondition")
    public Result queryByCondition(@RequestBody BaseAttrValue baseAttrValue){
        return Result.ok(baseAttrValueService.queryByCondition(baseAttrValue));
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/queryPage/{pageNum}/{pageSize}")
    public Result queryPage(@PathVariable Long pageNum, @PathVariable Long pageSize){
        return Result.ok(baseAttrValueService.queryPage(pageNum, pageSize));
    }

    @GetMapping("/queryPageByCondition/{pageNum}/{pageSize}")
    public Result queryPageByCondition(@PathVariable Long pageNum,
                                       @PathVariable Long pageSize,
                                       @RequestBody BaseAttrValue baseAttrValue){
        return Result.ok(baseAttrValueService.queryPageByCondition(pageNum, pageSize, baseAttrValue));
    }
}
