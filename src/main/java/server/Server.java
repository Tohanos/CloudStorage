package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

	private int commandPort;
	private int dataPort;
	public static final int CHUNK_SIZE = 1024;

	public Server(int commandPort, int dataPort) {
		this.commandPort = commandPort;
		this.dataPort = dataPort;
	}

	public void run () throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		UserManagement.readAllUsers();

		try {

			//Add bootstrap for command transmission
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							if (ch.localAddress().getPort() == commandPort) {
								ch.pipeline().addLast(new CommandHandler());
							}
							if (ch.localAddress().getPort() == dataPort) {
								ch.pipeline().addLast(new DataHandler());
							}
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);


			// Bind and start to accept incoming connections.
			ChannelFuture commandFuture = serverBootstrap.bind(commandPort).sync();
			ChannelFuture dataFuture = serverBootstrap.bind(dataPort).sync();
			System.out.println(" Server started");

			// Wait until the server socket is closed.
			commandFuture.channel().closeFuture().sync();
			StateMachinesPool.remove(commandFuture.channel());

		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}



	public static void main(String[] args) throws InterruptedException {
		new Server(1234, 1235).run();
	}
}
