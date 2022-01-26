package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.server.accept.HttpProxyAcceptHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    控制访问人数和频次
 */
public class AcceptHttpProxyServer {

    private static final Map<String, Integer> CLIENT_LIMIT_MAP = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHttpProxyAcceptHandler(new HttpProxyAcceptHandler() {
            /**
             * 代理配置处理实现：客户端建立连接的时候触发
             */
            @Override
            public boolean onAccept(final HttpRequest request, final Channel clientChannel) {
                // 获取客户端ip
                String ip = getClientIp(clientChannel);
                Integer count = CLIENT_LIMIT_MAP.getOrDefault(ip, 1);
                /**
                 * 校验指定ip的客户端连接数，超出指定限额则输出提示信息
                 */
                if (count > 5) {
                    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                    fullHttpResponse.content().writeBytes("<html><div>访问过于频繁！</div></html>".getBytes());
                    System.out.println("访问过于频繁！"+"count:"+count);
                    clientChannel.writeAndFlush(fullHttpResponse);
                    return false;
                }
                CLIENT_LIMIT_MAP.put(ip, count + 1);
                return true;
            }

            /**
             * 代理配置处理实现：客户端关闭连接的时候触发（返回当前客户端连接数）
             */
            @Override
            public void onClose(final Channel clientChannel) {
                CLIENT_LIMIT_MAP.computeIfPresent(getClientIp(clientChannel), (s, count) -> {
                    if (count > 0) {
                        return count - 1;
                    }
                    return count;
                });
            }

            /**
             * 自定义方法获取客户端ip信息
             * @param clientChannel
             * @return
             */
            private String getClientIp(Channel clientChannel) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) clientChannel.localAddress();
                return inetSocketAddress.getHostString();
            }
        });

        // 启动代理服务
        new HttpProxyServer()
                .serverConfig(config)
                .start(9999);
    }
}
