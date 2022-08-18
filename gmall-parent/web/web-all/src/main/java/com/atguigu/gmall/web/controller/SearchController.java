package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.util.Page;
import com.atguigu.gmall.list.client.SearchFeignService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/7
 * Description: 搜索页面的查询
 */
@Controller
@RequestMapping("/page/search")
public class SearchController {

    @Resource
    private SearchFeignService searchFeignService;

    @Value("${item.detailHtml}")
    private String detailHtml;

    /**
     * 搜索页面的查询
     * @param searchData
     * @return
     */
    @GetMapping
    public String search(@RequestParam Map<String, String> searchData, Model model) throws Exception {
        Map<String, Object> search = searchFeignService.search(searchData);

        //数据回显
        model.addAttribute("searchData", searchData);

        //从Es中获取的商品数据
        model.addAllAttributes(search);

        //拼接url
        String url = getUrl(searchData);
        model.addAttribute("url", url);

        //从Es返回的数据集中获取总数
        Object totalHits = search == null ? 0 : search.get("totalHits");
        //获取前端传入的页码
        String pageNum = searchData.get("pageNum");
        //分页信息
        Page page = new Page(
                Long.valueOf(totalHits.toString()),
                getCurrentPage(pageNum),
                50
        );
        model.addAttribute("page", page);

        return "list";
    }

    private int getCurrentPage(String pageNum) {
        try {
            return Integer.parseInt(pageNum)>0 ? Integer.parseInt(pageNum) : 1;
        }catch (Exception e){
            return 1;
        }
    }

    /**
     * 跳转到详情页
     * @param id
     * @return
     */
    @GetMapping("/toDetailHtml/{id}")
    public String toDetailHtml(@PathVariable Long id){
        return "redirect:" + detailHtml + id + ".html";
    }

    /**
     * 拼接url
     * @param searchData
     * @return
     */
    private String getUrl(Map<String, String> searchData) {
        StringBuffer sb = new StringBuffer();
        //初始url
        sb.append("/page/search?");

        //拼接url
        searchData.entrySet().stream().forEach(entry -> {
            if(!"price".equalsIgnoreCase(entry.getKey()) &&
                    !"sortField".equalsIgnoreCase(entry.getKey()) &&
                        !"sortRule".equalsIgnoreCase(entry.getKey()) &&
                            !"pageNum".equalsIgnoreCase(entry.getKey())){
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        });

        //去掉最后一个&
        return sb.substring(0, sb.length() - 1);
    }

}
