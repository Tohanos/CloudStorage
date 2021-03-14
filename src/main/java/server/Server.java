package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

	private int commandPort;
	private int dataPort;

	public Server(int commandPort, int dataPort) {
		this.commandPort = commandPort;
		this.dataPort = dataPort;
	}

	public void run () throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {

			//Add bootstrap for command transmission
			ServerBootstrap commandBootstrap = new ServerBootstrap();
			commandBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(new CommandHandler());
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			// Bind and start to accept incoming connections.
			ChannelFuture commandFuture = commandBootstrap.bind(commandPort).sync();

			// Add new bootstrap for data transmission
			ServerBootstrap dataBootstrap = new ServerBootstrap();

			dataBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(new DataHandler());
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			// Bind and start to accept incoming connections.
			ChannelFuture dataFuture = commandBootstrap.bind(dataPort).sync();

			// Wait until the server socket is closed.
			commandFuture.channel().closeFuture().sync();
			dataFuture.channel().closeFuture().sync();

		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		new Server(1234, 1235).run();
	}
}
