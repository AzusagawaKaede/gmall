package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.list.client.ListFeignService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/7/28
 * Description: 后天管理的业务层
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class ManageServiceImpl implements ManageService {

    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;
    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;
    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;
    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;
    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;
    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Resource
    private SpuInfoMapper spuInfoMapper;
    @Resource
    private SpuImageMapper spuImageMapper;
    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Resource
    private ListFeignService listFeignService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询所有的一级分类
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据一级分类id，查询二级分类
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        LambdaQueryWrapper<BaseCategory2> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategory2::getCategory1Id, category1Id);
        return baseCategory2Mapper.selectList(wrapper);
    }

    /**
     * 根据二级分类id，查询三级分类
     *
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        LambdaQueryWrapper<BaseCategory3> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategory3::getCategory2Id, category2Id);
        return baseCategory3Mapper.selectList(wrapper);
    }

    /**
     * 新增平台属性
     *
     * @param baseAttrInfo
     * @return
     */
    @Override
    public Boolean saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo == null ||
                StringUtils.isEmpty(baseAttrInfo.getAttrName())) {
            throw new RuntimeException("参数错误");
        }
        if (baseAttrInfo.getId() == null) {
            //没有id，说明是新增
            int insert = baseAttrInfoMapper.insert(baseAttrInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增平台属性失败");
            }
            baseAttrInfo.getAttrValueList().forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                int insert1 = baseAttrValueMapper.insert(baseAttrValue);
                if (insert1 <= 0) {
                    throw new RuntimeException("新增平台属性值失败");
                }
            });
        } else {
            //说明有id，是修改
            int update = baseAttrInfoMapper.updateById(baseAttrInfo);
            if (update < 0) {
                throw new RuntimeException("修改平台属性失败");
            }
            //将该id对应的所有的BaseAttrValue删除，重新新增一遍
            LambdaQueryWrapper<BaseAttrValue> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BaseAttrValue::getAttrId, baseAttrInfo.getId());
            int delete = baseAttrValueMapper.delete(wrapper);
            if (delete < 0) {
                throw new RuntimeException("修改平台属性值失败");
            }
            //重新插入
            baseAttrInfo.getAttrValueList().forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                int insert = baseAttrValueMapper.insert(baseAttrValue);
                if (insert <= 0) {
                    throw new RuntimeException("修改平台属性值失败");
                }
            });
        }
        return true;
    }

    /**
     * 根据选中的分类查询平台属性值
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrValue> getBaseAttrInfo(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrValueMapper.selectBaseAttrInfoByCategoryId(category1Id, category2Id, category3Id);
    }

    /**
     * 根据平台属性id查询对应的BaseAttrValue列表
     *
     * @param baseAttrInfoId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long baseAttrInfoId) {
        LambdaQueryWrapper<BaseAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseAttrValue::getAttrId, baseAttrInfoId);
        return baseAttrValueMapper.selectList(wrapper);
    }

    /**
     * 查询所有的品牌
     *
     * @return
     */
    @Override
    public List<BaseTrademark> getBaseTrademark() {
        return baseTrademarkMapper.selectList(null);
    }

    /**
     * 查询所有的销售属性
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttr() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 保存或修改SpuInfo、SpuImage、SpuSaleAttr、SpuSaleAttrValue
     *
     * @param spuInfo
     * @return
     */
    @Override
    public Boolean saveSpuInfo(SpuInfo spuInfo) {
        //校验 spuInfo
        if (spuInfo == null) {
            throw new RuntimeException("参数错误");
        }
        //是否有id
        if (spuInfo.getId() == null) {
            //没有id，说明是新增
            //新增spuInfo
            int insert = spuInfoMapper.insert(spuInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增SpuInfo失败");
            }
        } else {
            //有id，说明是修改
            //修改spuInfo
            int update = spuInfoMapper.updateById(spuInfo);
            if (update < 0) {
                throw new RuntimeException("修改SpuInfo失败");
            }
            //删除该id下所有的spuImage
            int deleteSpuImage = delteSpuImageBySpuId(spuInfo.getId());
            //删除该id下所有的spuSaleAttr
            int deleteSpuSaleAttr = deleteSpuSaleAttrBySpuId(spuInfo.getId());
            //删除该id下所有的spuSaleAttrValue
            int deleteSpuSaleAttrValue = deleteSpuSaleAttrValueBySpuId(spuInfo.getId());
            if (deleteSpuImage < 0 ||
                    deleteSpuSaleAttr < 0 ||
                    deleteSpuSaleAttrValue < 0) {
                throw new RuntimeException("修改SpuInfo失败");
            }
        }
        //获取spuInfo的id
        Long spuId = spuInfo.getId();
        //新增spuImage
        spuInfo.getSpuImageList().stream().forEach(spuImage -> {
            spuImage.setSpuId(spuId);
            int insert = spuImageMapper.insert(spuImage);
            if (insert <= 0) {
                throw new RuntimeException("新增spuImage失败");
            }
        });
        //新增spuSaleAttr
        spuInfo.getSpuSaleAttrList().stream().forEach(spuSaleAttr -> {
            spuSaleAttr.setSpuId(spuId);
            int insert = spuSaleAttrMapper.insert(spuSaleAttr);
            if (insert <= 0) {
                throw new RuntimeException("新增spuSaleAttr失败");
            }
            //新增spuSaleAttrValue
            spuSaleAttr.getSpuSaleAttrValueList().stream().forEach(spuSaleAttrValue -> {
                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                int insert1 = spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                if (insert1 <= 0) {
                    throw new RuntimeException("新增spuSaleAttrValue失败");
                }
            });
        });
        return true;
    }


    /**
     * 根据Category3Id条件分页查询
     *
     * @param category3Id
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<SpuInfo> querySpuInfoPageByCategory3Id(Long pageNum,
                                                       Long pageSize,
                                                       Long category3Id) {
        Page<SpuInfo> page = new Page<>(pageNum, pageSize);
        spuInfoMapper.selectPage(page, new LambdaQueryWrapper<SpuInfo>().eq(SpuInfo::getCategory3Id, category3Id));
        return page;
    }

    /**
     * 根据spuId查询spuImage
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> querySpuImageBySpuId(Long spuId) {
        return spuImageMapper.selectList(
                new LambdaQueryWrapper<SpuImage>()
                        .eq(SpuImage::getSpuId, spuId));
    }

    /**
     * 根据spuId查询所有的销售属性
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> querySpuSaleAttrBySpuId(Long spuId) {
        return spuSaleAttrMapper.querySpuSaleAttrBySpuId(spuId);
    }

    /**
     * 保存或修改skuInfo
     *
     * @param skuInfo
     * @return
     */
    @Override
    public Boolean saveSkuInfo(SkuInfo skuInfo) {
        //校验skuInfo是否为null
        if (skuInfo == null) {
            throw new RuntimeException("参数错误");
        }
        //是否有id
        if (skuInfo.getId() == null) {
            //没有id就是新增
            //插入到skuInfo表
            int insert = skuInfoMapper.insert(skuInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增skuInfo失败");
            }
        } else {
            //有id就是更新
            //更新到skuInfo
            int update = skuInfoMapper.updateById(skuInfo);
            if (update < 0) {
                throw new RuntimeException("更新skuInfo失败");
            }
            //根据skuId删除skuImage
            int delete = deleteSkuImageBySkuId(skuInfo.getId());
            //根据skuId删除skuAttrValue
            int delete1 = deleteSkuAttrValue(skuInfo.getId());
            //根据skuId删除skuSaleAttrValue
            int delete2 = deleteSkuSaleAttrValue(skuInfo.getId());
        }
        //获取skuInfo的id
        Long skuId = skuInfo.getId();
        //新增skuImage
        skuInfo.getSkuImageList().stream().forEach(skuImage -> {
            skuImage.setSkuId(skuId);
            int insert = skuImageMapper.insert(skuImage);
            if (insert <= 0) {
                throw new RuntimeException("新增skuImage失败");
            }
        });
        //新增skuAttrValue
        skuInfo.getSkuAttrValueList().stream().forEach(skuAttrValue -> {
            skuAttrValue.setSkuId(skuId);
            int insert = skuAttrValueMapper.insert(skuAttrValue);
            if (insert <= 0) {
                throw new RuntimeException("新增skuAttrValue失败");
            }
        });
        //新增skuSaleAttrValue
        skuInfo.getSkuSaleAttrValueList().stream().forEach(skuSaleAttrValue -> {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            int insert = skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            if (insert <= 0) {
                throw new RuntimeException("新增skuSaleAttrValue失败");
            }
        });
        return true;
    }

    /**
     * 分页查询skuInfo
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<SkuInfo> list(Long pageNum, Long pageSize) {
        Page<SkuInfo> page = new Page<>(pageNum, pageSize);
        skuInfoMapper.selectPage(page, null);
        return page;
    }

    /**
     * 上架或下架商品
     *
     * @param skuId
     * @param status
     * @return
     */
    @Override
    public Boolean upOrDownSku(Long skuId, Short status) {
        //校验skuId是否为null
        if (skuId == null) {
            throw new RuntimeException("参数异常");
        }
        //判断是否存在该sku，并且判断id是否为null，防止出现空指针
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo == null || skuInfo.getId() == null) {
            throw new RuntimeException("该Sku不存在");
        }
        //更新上下架状态
        skuInfo.setIsSale(status);
        int update = skuInfoMapper.updateById(skuInfo);
        if (update < 0) {
            throw new RuntimeException("更新失败");
        }

        //设置可靠性投递 -- return回调
//        rabbitTemplate.setReturnCallback((message, code, text, exchange, routingKey) -> {
////            String skuId = new String(message.getBody());
//            if (routingKey.equals("sku_up")) {
//                log.error("商品" + skuId + "上架失败！");
//            } else if (routingKey.equals("sku_down")) {
//                log.error("商品" + skuId + "下架失败！");
//            }
//            log.error("失败状态码：" + code);
//            log.error("失败的原因：" + text);
//            log.error("使用的交换机：" + exchange);
//            log.error("使用的路由：" + routingKey);
//        });

        //上架或下架
        if (ProductConst.SKUINFO_ON_SALE.equals(status)) {
            //说明是上架，将商品保存到Es中
            //发送信息到消息队列中，实现异步将商品保存到ES中。传入skuId传入String类型的
            rabbitTemplate.convertAndSend("sku_up_or_down_exchange", "sku_up", skuId + "");
        } else {
            //说明是下架，将商品从Es中删除
            //发送消息到消息队列中，实现异步将商品从ES中删除
            rabbitTemplate.convertAndSend("sku_up_or_down_exchange", "sku_down", skuId + "");
        }
        return true;
    }

    /**
     * 分页查询品牌
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<BaseTrademark> queryBaseTrademarkPage(Long pageNum, Long pageSize) {
        Page<BaseTrademark> page = new Page<>(pageNum, pageSize);
        baseTrademarkMapper.selectPage(page, null);
        return page;
    }

    /**
     * 根据id删除Trademark
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteTrademark(Long id) {
        int delete = baseTrademarkMapper.deleteById(id);
        if (delete <= 0) {
            throw new RuntimeException("删除失败");
        }
        return true;
    }

    /**
     * 新增或修改品牌
     *
     * @param baseTrademark
     * @return
     */
    @Override
    public Boolean insertBaseTrademark(BaseTrademark baseTrademark) {
        //参数校验
        if (baseTrademark == null) {
            throw new RuntimeException("参数错误");
        }
        //判断是否有id，没有id是新增
        if (baseTrademark.getId() == null) {
            int insert = baseTrademarkMapper.insert(baseTrademark);
            if (insert <= 0) {
                throw new RuntimeException("新增失败");
            }
        } else {
            //有id说明是修改
            int update = baseTrademarkMapper.updateById(baseTrademark);
            if (update < 0) {
                throw new RuntimeException("更新失败");
            }
        }
        return true;
    }

    /**
     * 根据id查询 BaseTrademark
     *
     * @param id
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademarkById(Long id) {
        return baseTrademarkMapper.selectById(id);
    }

    /**
     * 根据skuId删除SkuSaleAttrValue
     *
     * @param skuId
     * @return
     */
    private int deleteSkuSaleAttrValue(Long skuId) {
        return skuSaleAttrValueMapper.delete(
                new LambdaQueryWrapper<SkuSaleAttrValue>()
                        .eq(SkuSaleAttrValue::getSkuId, skuId));
    }

    /**
     * 根据skuId删除SkuAttrValue
     *
     * @param skuId
     * @return
     */
    private int deleteSkuAttrValue(Long skuId) {
        return skuAttrValueMapper.delete(
                new LambdaQueryWrapper<SkuAttrValue>()
                        .eq(SkuAttrValue::getSkuId, skuId));
    }

    /**
     * 根据skuId删除skuImage
     *
     * @param skuId
     * @return
     */
    private int deleteSkuImageBySkuId(Long skuId) {
        return skuImageMapper.delete(
                new LambdaQueryWrapper<SkuImage>()
                        .eq(SkuImage::getSkuId, skuId));
    }

    /**
     * 根据spuId删除对应的spuSaleAttrValue
     *
     * @param spuId
     * @return
     */
    private int deleteSpuSaleAttrValueBySpuId(Long spuId) {
        return spuSaleAttrValueMapper.delete(
                new LambdaQueryWrapper<SpuSaleAttrValue>()
                        .eq(SpuSaleAttrValue::getSpuId, spuId));
    }

    /**
     * 根据spuId删除对应的spuSaleAttr
     *
     * @param spuId
     * @return
     */
    private int deleteSpuSaleAttrBySpuId(Long spuId) {
        return spuSaleAttrMapper.delete(
                new LambdaQueryWrapper<SpuSaleAttr>()
                        .eq(SpuSaleAttr::getSpuId, spuId));
    }

    /**
     * 根据spuId删除对应的spuImage
     *
     * @param spuId
     * @return
     */
    private int delteSpuImageBySpuId(Long spuId) {
        return spuImageMapper.delete(
                new LambdaQueryWrapper<SpuImage>()
                        .eq(SpuImage::getSpuId, spuId));
    }
}
