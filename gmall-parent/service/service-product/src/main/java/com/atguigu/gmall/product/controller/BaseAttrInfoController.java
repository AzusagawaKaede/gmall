package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description:
 */
@RestController
@RequestMapping("/baseAttrInfo")
public class BaseAttrInfoController {

    @Autowired
    private BaseAttrInfoService baseAttrInfoService;

    /**
     * 新增BaseAttrInfo
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody BaseAttrInfo baseAttrInfo){
        baseAttrInfoService.save(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 根据id删除BaseAttrInfo
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        baseAttrInfoService.removeById(id);
        return Result.ok();
    }

    /**
     * 修改BaseAttrInfo
     * @param baseAttrInfo
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody BaseAttrInfo baseAttrInfo){
        baseAttrInfoService.updateById(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/queryById/{id}")
    public Result queryById(@PathVariable Long id){
        BaseAttrInfo baseAttrInfo = baseAttrInfoService.getById(id);
        return Result.ok(baseAttrInfo);
    }

    /**
     * 查询全部
     * @return
     */
    @GetMapping("/queryList")
    public Result queryList(){
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoService.list(null);
        return Result.ok(baseAttrInfoList);
    }


    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    @GetMapping("/queryByCondition")
    public Result queryByCondition(@RequestBody BaseAttrInfo baseAttrInfo){
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoService.queryByCondition(baseAttrInfo);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/queryPage/{pageNum}/{pageSize}")
    public Result queryPage(@PathVariable Long pageNum, @PathVariable Long pageSize){
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoService.queryPage(pageNum, pageSize);
        return Result.ok(baseAttrInfoList);
    }

    @GetMapping("/queryPageByCondition/{pageNum}/{pageSize}")
    public Result queryPageByCondition(@PathVariable Long pageNum,
                                       @PathVariable Long pageSize,
                                       @RequestBody BaseAttrInfo baseAttrInfo){
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoService.queryPageByCondition(pageNum, pageSize, baseAttrInfo);
        return Result.ok(baseAttrInfoList);
    }

}
