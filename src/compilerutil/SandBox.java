package compilerutil;


import com.google.gson.Gson;
import compilerutil.dto.*;
import compilerutil.stream.CacheOutputStream;
import compilerutil.stream.ThreadInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SandBox {
	// 每加载超过100个类后，就替换一个新的ClassLoader
	private static final int UPDATE_CLASSLOADER_GAP = 5;
	// 记录一共加载过的类数量
	private int loadClassCount = 0;
	private Gson gson = new Gson();
	private boolean isBusy = false;
	private String pid = null;
	private Socket communicateSocket;
	//通道管理器
//	private Selector selector;
	private MemoryMXBean systemMemoryBean = ManagementFactory.getMemoryMXBean();
	// 用一个线程池去处理每个判题请求
	private ExecutorService problemThreadPool = Executors
			.newSingleThreadExecutor(r -> {
				Thread thread = new Thread(r);
				thread.setName("problemThreadPool");
				thread.setUncaughtExceptionHandler((t, e) -> writeResponse(null,
						CommunicationSignal.ResponseSignal.ERROR,
						null, e.getMessage()));
				return thread;
			});
	// 用一个线程池去等待每个判题请求的结果返回
	private ExecutorService problemResultThreadPool = Executors
			.newSingleThreadExecutor(r -> {
				Thread thread = new Thread(r);
				thread.setName("problemResultThreadPool");
				thread.setUncaughtExceptionHandler((t, e) -> writeResponse(null,
						CommunicationSignal.ResponseSignal.ERROR,
						null, e.getMessage()));
				return thread;
			});

	// 用于重定向输出流，即代码输出的结果，将会输出到这个缓冲区中
	private volatile CacheOutputStream resultBuffer = new CacheOutputStream();
	private volatile ThreadInputStream systemThreadIn = new ThreadInputStream();

    private SandBox(String port) {
        initSandbox(port);
    }

	/**
	 * 沙箱初始化函数
	 */
	private void initSandbox(String port) {
		// 获取进程id，用于向外界反馈
		getPid();
		// 打开用于与外界沟通的通道
		openServerSocketWaitToConnect(Integer.parseInt(port));
		// 等外界与沙箱，通过socket沟通上之后，就会进行业务上的沟通
		service();

	}


	/**
	 * 获取进程ID
	 */
	private void getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		pid = name.split("@")[0];
	}

	/**
	 * 打开连接，等待建立连接
	 * @param port 监听端口
	 */
	private void openServerSocketWaitToConnect(int port) {
		try {
//			//获取一个ServerSocket通道
//			ServerSocketChannel serverChannel = ServerSocketChannel.open();
//			serverChannel.configureBlocking(false);
//			serverChannel.socket().bind(new InetSocketAddress(port));
//			//获取通道管理器
//			selector=Selector.open();
//			//将通道管理器与通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件，
//			//只有当该事件到达时，Selector.select()会返回，否则一直阻塞。
//			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("sandbox" + port + "开始工作");
			communicateSocket = serverSocket.accept();
			System.out.println("pid:" + pid);
			// 只与外部建立一个沟通的连接
			//serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("无法打开沙箱端Socket，可能是端口被占用了");
		}
	}

//	private void nioListen(){
//		try {
//			//使用轮询访问selector
//			while(true){
//				//当有注册的事件到达时，方法返回，否则阻塞。
//				selector.select();
//				//获取selector中的迭代器，选中项为注册的事件
//				Iterator<SelectionKey> ite=selector.selectedKeys().iterator();
//				while(ite.hasNext()){
//					SelectionKey key = ite.next();
//					//删除已选key，防止重复处理
//					ite.remove();
//					//客户端请求连接事件
//					if(key.isAcceptable()){
//						ServerSocketChannel server = (ServerSocketChannel)key.channel();
//						//获得客户端连接通道
//						SocketChannel channel = server.accept();
//						channel.configureBlocking(false);
//						//向客户端发消息
//						channel.write(ByteBuffer.wrap(new String("send message to client").getBytes()));
//						//在与客户端连接成功后，为客户端通道注册SelectionKey.OP_READ事件。
//						channel.register(selector, SelectionKey.OP_READ);
//
//						System.out.println("客户端请求连接事件");
//					}else if(key.isReadable()){//有可读数据事件
//						//获取客户端传输数据可读取消息通道。
//						SocketChannel channel = (SocketChannel)key.channel();
//						//创建读取数据缓冲器
//						ByteBuffer buffer = ByteBuffer.allocate(10);
//						int read = channel.read(buffer);
//						byte[] data = buffer.array();
//						String message = new String(data);
//
//						System.out.println("receive message from client, size:" + buffer.position() + " msg: " + message);
////                    ByteBuffer outbuffer = ByteBuffer.wrap(("server.".concat(msg)).getBytes());
////                    channel.write(outbuffer);
//					}
//				}
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}

	/**
	 * 检查沙箱是否正忙
	 * @param signalId 信号量
	 */
	private void checkBusy(String signalId) {
		String responseCommand;

		if (isBusy) {
			responseCommand = CommunicationSignal.ResponseSignal.YES;
		} else {
			responseCommand = CommunicationSignal.ResponseSignal.NO;
		}

		writeResponse(signalId, responseCommand,
				CommunicationSignal.RequestSignal.IS_BUSY, null);
	}

	/**
	 * 发送回复
	 * @param signalId 信号
	 * @param responseCommand 回复的命令
	 * @param requestCommand 请求的命令
	 * @param data 数据
	 */
	private void writeResponse(String signalId, String responseCommand,
							   String requestCommand, String data) {
		try {
			OutputStream outputStream = communicateSocket.getOutputStream();
			Response response = new Response();
			response.setExamId(signalId);
			response.setResponseCommand(responseCommand);
			//a
			response.setRequestCommand(requestCommand);
			response.setData(data);
			String resStr = gson.toJson(response);
			outputStream
					.write((resStr + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("无法对外输出数据:"+e.getMessage());
		}

	}


	/**
	 * 系统服务函数
	 */
	private void service() {
		try {
			Scanner scanner = new Scanner(communicateSocket.getInputStream());
			// 必须建立了连接和流之后，才能设置这里的权限
			System.setSecurityManager(new SandboxSecurityManager());
			String data;
			while (scanner.hasNext()) {
				// 每一次交流，都是一行一行的形式交流，即本次沟通内容发送完之后，发送方会在最后，加上一个"\n"，表示发送完了这条消息
				data = scanner.nextLine();
				Request request = gson.fromJson(data, Request.class);
				dispatchRequest(request);
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
			writeResponse(null, CommunicationSignal.ResponseSignal.ERROR, null,
					e.getMessage());
		}
	}

	/**
	 * 请求分发函数
	 * @param request 请求内容
	 */
	private void dispatchRequest(Request request) {
		if (CommunicationSignal.RequestSignal.CLOSE_SANDBOX.equals(request
				.getCommand())) {
			closeSandboxService(request.getExamId());
		} else if (CommunicationSignal.RequestSignal.SANDBOX_STATUS
				.equals(request.getCommand())) {
			feedbackSandboxStatusService(request.getExamId());
		} else if (CommunicationSignal.RequestSignal.REQUSET_JUDGED_PROBLEM
				.equals(request.getCommand())) {
			if (loadClassCount >= UPDATE_CLASSLOADER_GAP) {
				loadClassCount = 0;
				System.gc();
			}
			TestCaseDto testCaseDto = gson.fromJson(request.getData(),TestCaseDto.class);
			if(beforeRunCheckCode(testCaseDto.getCode())) {
				Future<ExamResult> future = problemThreadPool.submit(new ExamCallable(systemThreadIn, testCaseDto, resultBuffer));
				returnJudgedProblemResult(request.getExamId(), future);
				isBusy = true;
				loadClassCount++;
			}
		} else if (CommunicationSignal.RequestSignal.IS_BUSY.equals(request
				.getCommand())) {
			checkBusy(request.getExamId());
		}
	}

	/**
	 * 返回题目运行结果
	 * @param signalId 信号
	 * @param result 题目运行结果
	 */
	private void returnJudgedProblemResult(final String signalId,
										   final Future<ExamResult> result) {
		problemResultThreadPool.execute(() -> {
			if (result != null) {
				try {
                    ExamResult dto = result.get();
                    String resultStr = gson.toJson(dto);
					writeResponse(
							signalId,
							CommunicationSignal.ResponseSignal.OK,
							CommunicationSignal.RequestSignal.REQUSET_JUDGED_PROBLEM,
                            resultStr);
					isBusy = false;

					// 通知对方，主动告诉对方，自己已经空闲了，已经准备好下一次判题
					writeResponse(null,
							CommunicationSignal.ResponseSignal.IDLE, null,
							null);
				} catch (Exception e) {
					e.printStackTrace();
					writeResponse(null,
							CommunicationSignal.ResponseSignal.ERROR, null,
							e.getMessage());
				}
			}
		});
	}

	/**
	 * 关闭沙箱服务
	 * @param signalId 关闭信号
	 */
	private void closeSandboxService(String signalId) {
		writeResponse(signalId, CommunicationSignal.ResponseSignal.OK,
				CommunicationSignal.RequestSignal.CLOSE_SANDBOX, null);
		try {
			communicateSocket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		closeSandbox();
	}

	/**
	 * 返回沙箱状态的服务
	 * @param signalId 信号
	 */
	private void feedbackSandboxStatusService(String signalId) {
		SandBoxStatus sandBoxStatus = new SandBoxStatus();
		sandBoxStatus.setPid(pid);
		sandBoxStatus.setBeginStartTime(System.currentTimeMillis());
		sandBoxStatus.setBusy(isBusy);
		// 由堆内存和非堆内存组成
		long useMemory = systemMemoryBean.getHeapMemoryUsage().getUsed()
				+ systemMemoryBean.getNonHeapMemoryUsage().getUsed();
		sandBoxStatus.setUseMemory(useMemory);
		// 由堆内存和非堆内存组成
		long maxMemory = systemMemoryBean.getHeapMemoryUsage().getMax()
				+ systemMemoryBean.getNonHeapMemoryUsage().getMax();
		sandBoxStatus.setMaxMemory(maxMemory);
		writeResponse(signalId, CommunicationSignal.ResponseSignal.OK,
				CommunicationSignal.RequestSignal.SANDBOX_STATUS,
				gson.toJson(sandBoxStatus));

	}


	/**
	 * @Author 李如豪
	 * @Description 编译前进行代码检查
	 * @Date 12:04 2019/1/24
	 * @Param code 提交的代码
	 * @return 代码是否符合规范
	 **/
	private boolean beforeRunCheckCode(String code){
		return code != null && !code.equals("");
	}

	public static void main(String[] args){
		try {
            new SandBox(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

//	private void test(){
//		writeResponse(null, CommunicationSignal.ResponseSignal.ERROR, null,
//				"你好，这是一个测试");
//	}

	/**
	 * 关闭沙箱
	 */
	private void closeSandbox() {
		try {
			communicateSocket.close();
		} catch (IOException ignored) {
		}
		System.exit(1238888);
	}
}
