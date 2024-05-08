package com.hmall.search.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.common.domain.doc.ItemDoc;
import com.hmall.common.domain.po.Item;
import com.hmall.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SearchListener {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //使用lazy进行优化
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "item.add.queue", durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")),
            exchange = @Exchange(value = "item.direct", type = ExchangeTypes.DIRECT),
            key = {"add"}
    ))
    public void add(Item item) throws IOException {

        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 1.准备Request
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());

        // 2.准备请求参数
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);

        // 3.发送
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "item.delete.queue", durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")),
            exchange = @Exchange(value = "item.direct", type = ExchangeTypes.DIRECT),
            key = {"delete"}
    ))
    public void delete(Long id) throws IOException {
        DeleteRequest request = new DeleteRequest("items", id.toString());

        restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "item.update.queue", durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")),
            exchange = @Exchange(value = "item.direct", type = ExchangeTypes.DIRECT),
            key = {"update"}
    ))
    private void update(Item item) throws IOException {
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 1.准备Request
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());

        // 2.准备请求参数
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);

        // 3.发送
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

}
