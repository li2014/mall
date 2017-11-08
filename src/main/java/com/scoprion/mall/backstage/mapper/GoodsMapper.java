package com.scoprion.mall.backstage.mapper;

import com.github.pagehelper.Page;
import com.scoprion.mall.domain.GoodExt;
import com.scoprion.mall.domain.Goods;
import com.scoprion.mall.domain.GoodsImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created on 2017/9/29.
 *
 * @author adming
 */
@Mapper
public interface GoodsMapper {

    /**
     * 首页展示 查询限时购买商品   4条
     *
     * @return
     */
    List<Goods> findLimit4ByTimeGoods();

    /**
     * 分页查询 限时购买商品列表
     *
     * @return
     */
    Page<Goods> findByPageAndLimit();

    /**
     * 创建商品
     *
     * @param goods
     * @return
     */
    int add(Goods goods);

    /**
     * 优选  分页查询
     *
     * @return
     */
    Page<Goods> preferenceGivenByPage();

    /**
     * 根据id查询商品详情
     *
     * @param goodId
     * @return
     */
    GoodExt findById(@Param("goodId") Long goodId);

    /**
     * 商品库存扣减
     *
     * @param goodId
     * @param count
     * @return
     */
    int modifyGoodsDeduction(@Param("goodId") Long goodId, @Param("count") Integer count);

    /**
     * 更新商品信息
     *
     * @param goods
     * @return
     */
    int updateGoods(Goods goods);

    /**
     * 根据条件模糊查询
     *
     * @param searchKey String
     * @return
     */
    Page<Goods> findByCondition(@Param("searchKey") String searchKey);

    /**
     * 商品上下架
     *
     * @param saleStatus saleStatus 1上架 0下架 默认上架
     * @param goodId     商品id
     * @return 更新是否成功 1 成功  0 失败
     */
    int modifySaleStatus(@Param("saleStatus") String saleStatus, @Param("goodId") Long goodId);

    /**
     * 根据商品id删除商品
     *
     * @param id 商品id
     * @return
     */
    int deleteGoodsById(@Param("id") Long id);


    /**
     * 修改图片对应的商品ID
     *
     * @param goodsImage
     * @return
     */
    int updateImageWithGoodsId(GoodsImage goodsImage);

    /**
     * 根据商品ID查找图片列表
     *
     * @param goodId
     * @return
     */
    List<GoodsImage> findImgUrlByGoodsId(@Param("goodId") Long goodId);

    /**
     * 根据商品id删除图片
     *
     * @param goodId
     * @return
     */
    int deleteImageByGoodsId(@Param("goodId") Long goodId);

    /**
     * 库存扣减
     *
     * @param goodId
     * @param stock
     * @return
     */
    int updateGoodStockById(@Param("goodId") Long goodId, @Param("stock") int stock);
}