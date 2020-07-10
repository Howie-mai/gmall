package com.zhku.mh.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zhku.mh.gmall.bean.PmsSearchParam;
import com.zhku.mh.gmall.bean.PmsSearchSkuInfo;
import com.zhku.mh.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/5/25 15:56
 * Description：
 * Author： mh
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam param) {

        String dsl = getSearchDSl(param);

        System.out.println(dsl);

        //查询不需要主键
        Search select = new Search.Builder(dsl).addIndex("gmall_pms").addType("PmsSkuInfo").build();

        SearchResult result = null;
        try {
            result = jestClient.execute(select);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = result.getHits(PmsSearchSkuInfo.class);

        List<PmsSearchSkuInfo> searchSkuInfos = new ArrayList<>();

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit:hits) {
            PmsSearchSkuInfo source = hit.source;

            Map<String, List<String>> highlight = hit.highlight;

            String skuName = highlight.get("skuName").get(0);

            source.setSkuName(skuName);

            searchSkuInfos.add(source);
        }

        System.out.println(searchSkuInfos.size());
        System.out.println(searchSkuInfos);

        return searchSkuInfos;
    }

    private String getSearchDSl(PmsSearchParam param) {
        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter
        if(StringUtils.isNoneBlank(param.getCatalog3Id())){
            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("catalog3Id",param.getCatalog3Id());
            boolQueryBuilder.filter(termsQueryBuilder);
        }
        if(param.getValueId() != null){
            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId",param.getValueId());
            boolQueryBuilder.filter(termsQueryBuilder);
        }

        //must
        if(StringUtils.isNoneBlank(param.getKeyword())){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",param.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //sort
        searchSourceBuilder.sort("hotScore",SortOrder.DESC);

        //aggs聚合
        TermsAggregationBuilder group_by = AggregationBuilders.terms("group_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(group_by);

       return searchSourceBuilder.toString();

    }
}
