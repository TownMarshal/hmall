package com.hmall.search.listen;


import com.hmall.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchListen {
    @Autowired
    private SearchService searchService;

    @RabbitListener(queuesToDeclare = @Queue("item.update"))
    public void insert(Long id) {
        // feign  根据id 远程查询商品信息
        searchService.insert(id);
        log.info("增加或修改成功");
    }

    @RabbitListener(queuesToDeclare = @Queue("item.delete"))
    public void delete(Long id){
        searchService.delete(id);
        log.info("删除成功");
    }
}
