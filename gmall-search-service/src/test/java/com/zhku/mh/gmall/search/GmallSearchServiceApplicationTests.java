package com.zhku.mh.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.zhku.mh.gmall.bean.PmsSearchSkuInfo;
import com.zhku.mh.gmall.bean.PmsSkuInfo;
import com.zhku.mh.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EnableDubbo
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    private JestClient jestClient;

    //创建索引
    @Test
    public void contextLoads() throws IOException {
        //查询mysql
        List<PmsSkuInfo> skuInfos = skuService.getAllSku("61");
        //转化为es的数据结构
        List<PmsSearchSkuInfo> searchSkuInfos = new ArrayList<>();
        for (PmsSkuInfo skuInfo : skuInfos){
            PmsSearchSkuInfo searchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(skuInfo,searchSkuInfo);
            searchSkuInfos.add(searchSkuInfo);
        }
        //导入es
        for (PmsSearchSkuInfo searchSkuInfo:searchSkuInfos) {
            Index build = new Index.Builder(searchSkuInfo).index("gmall_pms").type("PmsSkuInfo").id(searchSkuInfo.getId() + "").build();
            jestClient.execute(build);
        }
    }

    //复杂查询
    @Test
    public void testSelect() throws IOException {
        List<PmsSearchSkuInfo> searchSkuInfos = new ArrayList<>();
        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId","48","51");
        boolQueryBuilder.filter(termsQueryBuilder);
        //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","小米");
        boolQueryBuilder.must(matchQueryBuilder);

        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
//        searchSourceBuilder.highlighter(null);

        String dsl = searchSourceBuilder.toString();

        System.out.println(dsl);

        //查询不需要主键
        Search select = new Search.Builder(dsl).addIndex("gmall_pms").addType("PmsSkuInfo").build();

        SearchResult result = jestClient.execute(select);

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = result.getHits(PmsSearchSkuInfo.class);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit:hits) {
            PmsSearchSkuInfo source = hit.source;

            searchSkuInfos.add(source);
        }

        System.out.println(searchSkuInfos.size());
        System.out.println(searchSkuInfos);
    }

}
