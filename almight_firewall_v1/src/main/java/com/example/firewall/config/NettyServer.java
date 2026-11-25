package com.example.firewall.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    public void start() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println("Packet: " + msg);
                            }
                        });
                    }
                });

        bootstrap.bind(9000).sync();
    }
}