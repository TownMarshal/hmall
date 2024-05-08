package com.hmall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {
    //注册客户端

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("localhost:9200")
        ));
    }

/*    @Value("${elasticsearch.host:127.0.0.1}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Bean
    public RestHighLevelClient client(){
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host,port)
                )
        );
    }*/
}
