import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.lindorm.vectorstore.LindormConfig;
import com.alibaba.langengine.lindorm.vectorstore.LindormConstants;
import com.alibaba.langengine.lindorm.vectorstore.LindormCreateRequest;
import com.alibaba.langengine.lindorm.vectorstore.LindormDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BeanConfiguration.class, LindormConfig.class})
public class LindormDBTest {
    @Resource
    private LindormDB lindormDB;

    @BeforeEach
    public void setIndexName() {
        String indexName = "ali-langengine-test-index";
        lindormDB.setIndexName(indexName);
    }

    @Test
    public void test_createIndex() {
        String indexName = "ali-langengine-hnsw-test-index";
        Document doc = new Document("content", null);
        Integer dimension = lindormDB.getEmbedding().embedDocument(Arrays.asList(new Document[] { doc })).get(0).getEmbedding().size();
        LindormCreateRequest.HNSWParams hnswParams = LindormCreateRequest.HNSWParams.builder().build();
        LindormCreateRequest createRequest =
                LindormCreateRequest.builder()
                        .indexName(indexName)
                        .dimension(dimension)
                        .indexParams(hnswParams).build();
        lindormDB.createIndex(createRequest);
    }

    @Test
    public void test_addDocument() {
        List<Document> documents = new ArrayList<>();
        Document document = new Document();
        document.setPageContent("问题:接口taobao.traderates.get没有获取评价信息，追评后可以获取到评价信息了？\n" +
                "答案:您好；\n" +
                "在主评期限内买家没有评价订单，直接追评了，主评是系统默认评价了，所以追评后taobao.traderates.get 获取到了系统默认的评价信息了。");
        document.setUniqueId("4359");
        document.setIndex(1);
        document.setMetadata(new HashMap<>());
        document.getMetadata().put("name", "demo1");
        documents.add(document);
        document = new Document();
        document.setPageContent("问题:alibaba.ascp.logistics.offline.send报错“周期购发货需要传期数 #MISSING_PERIOD”\n" +
                "答案:您好；\n" +
                "入参feature：seqNo=期数");
        document.setUniqueId("4283");
        document.setIndex(1);
        document.setMetadata(new HashMap<>());
        document.getMetadata().put("name", "demo2");
        documents.add(document);
        document = new Document();
        document.setPageContent("问题:用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致\n" +
                "答案:Q:为什么用不同的token请求alibaba.item.edit.schema.get接口，返回的字段不一致，一个返回skuOuterId，一个返回sku_outerId\n" +
                "A:一个是天猫的返回，一个是淘宝的返回，信息是不一样的");
        document.setUniqueId("4262");
        document.setIndex(1);
        document.setMetadata(new HashMap<>());
        document.getMetadata().put("name", "demo3");
        documents.add(document);
        lindormDB.addDocuments(documents);
    }

    @Test
    public void test_similaritySearch() {
        List<Document> result = lindormDB.similaritySearch("接口taobao.traderates.get没有获取评价信息", 3);

        System.out.println(JSONObject.toJSONString(result));
        System.out.println(result.get(0).getPageContent());
        result = lindormDB.similaritySearch("alibaba.ascp.logistics.offline.send报错是什么原因？", 1);
        System.out.println(result.get(0).getPageContent());

        result = lindormDB.similaritySearch("alibaba.item.edit.schema.get", 1);
        System.out.println(result.get(0).getPageContent());

        // search with type filter
        System.out.println("search with type");
        int type = Integer.parseInt(lindormDB.getEmbedding().getModelType());
        result = lindormDB.similaritySearch("alibaba.item.edit.schema.get", 1, null, type);
        System.out.println(result.get(0).getPageContent());

        result = lindormDB.similaritySearch("alibaba.item.edit.schema.get", 1, null, type-2);
        assert result.isEmpty();
    }

    @Test
    public void test_ivfpq_createIndex() {
        String indexName = "ali-langengine-ivfpq-test-index";
        Document doc = new Document("content", null);
        Integer dimension = lindormDB.getEmbedding().embedDocument(Arrays.asList(new Document[] { doc })).get(0).getEmbedding().size();
        LindormCreateRequest.IVFPQParams ivfpqParams = LindormCreateRequest.IVFPQParams.builder()
                .m(dimension)
                .nlist(20)
                .build();
        LindormCreateRequest createRequest =
                LindormCreateRequest.builder()
                        .indexName(indexName)
                        .indexMethod(LindormConstants.LINDORM_VECTOR_METHOD_IVFPQ)
                        .dimension(dimension)
                        .indexParams(ivfpqParams).build();
        lindormDB.createIndex(createRequest);
    }

    @Test
    public void test_ivfpq_addDocuments() {
        // should add at least 256 records before start build index
        int recordNum = 300;
        List<Document> documents = new ArrayList<>();
        for (int i = 300; i < 300 + recordNum; i++) {
            Document document = new Document();
            document.setPageContent("content" + i);
            document.setUniqueId("" + i);
            document.setIndex(1);
            document.setMetadata(new HashMap<>());
            document.getMetadata().put("name", "demo" + i);
            documents.add(document);
        }
        lindormDB.addDocuments(documents);
    }

    @Test
    public void test_ivfpq_buildIndex() throws IOException, InterruptedException, TimeoutException {
        // Before build index, should add at least 256 records
        lindormDB.buildIndex();
    }

    @Test
    public void test_ivfpq_similaritySearch() {
        List<Document> result = lindormDB.similaritySearch("3", 3);
        System.out.println(JSONObject.toJSONString(result));
        System.out.println(result.get(0).getPageContent());
        result = lindormDB.similaritySearch("content 2是什么", 1);
        System.out.println(result.get(0).getPageContent());
        result = lindormDB.similaritySearch("content1是啥", 1);
        System.out.println(result.get(0).getPageContent());
    }
}
