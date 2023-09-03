package threading;

import com.sun.management.OperatingSystemMXBean;
import io.Session;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import patch.battle.GBattle;
import patch.Resource;
import patch.candybattle.CandyBattleManager;
import patch.interfaces.IBattle;
import patch.RmiRemoteImpl;
import patch.tournament.GeninTournament;
import patch.tournament.KageTournament;
import patch.tournament.Tournament;
import real.*;
import server.*;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import static patch.interfaces.IBattle.*;
import static threading.Manager.MOMENT_REFRESH_BATTLE;

public class Server extends WindowAdapter implements ActionListener {
    public static long TIME_SLEEP_SHINWA_THREAD;
    public static Server instance;
    public static Runnable updateBattle;
    public IBattle globalBattle;
    private ServerSocket listenSocket;
    public volatile static boolean start;
    public Manager manager;
    public static Object LOCK_MYSQL = new Object();

    @NotNull
    public MenuController menu;
    public ServerController controllerManager;
    public Controller serverMessageHandler;
    private static Map[] maps;
    public static short[] MOMENT_BOSS_REFRESH;
    public static short[] Server_REFRESH;
    private static final boolean[] isRefreshBoss;
    private static final boolean[] isRefreshServer;
    private static final short[] mapBossVDMQ;
    private static final short[] mapBoss45;
    private static final short[] mapBoss55;
    private static final short[] mapBoss65;
    private static final short[] mapBoss75;
    public static Runnable updateServer;
    public static Runnable updateRefreshBoss;
    public static ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static ClanTerritoryManager clanTerritoryManager = new ClanTerritoryManager();
    public static Tournament kageTournament;
    public static Tournament geninTournament;
    public static java.util.Map<String, Resource> resource;
    public DaemonThread daemonThread;

    @NotNull
    public static CandyBattleManager candyBattleManager;
    
    public static long timeTTA;
    public static long timeWaitTTA;
    public static boolean dangKyTTA = false;
    public static Runnable updateTTA;
    public static String[] adminName = new String[] {"admin", "tester"};
    public static String[] Tips = new String[]{
        "Chúc các bạn chơi game vui vẻ, không chơi được thì cũng phải vui vẻ",
        "Thêm 2 tờ 500k là đủ một triệu, thêm em nữa là đủ một tình yêu.",
        "Đàn ông khi im lặng họ thường không nói gì.",
        "Máy Chủ đang cập nhật dữ liệu"}; 

    private Frame frame;
    public Server() {
        this.listenSocket = null;
        resource = new ConcurrentHashMap<>();
        try {
            frame = new Frame("Manger");
            InputStream is = getClass().getClassLoader().getResourceAsStream("icon.png");
            byte[] data = new byte[is.available()];
            is.read(data);
            ImageIcon img = new ImageIcon(data);
            frame.setIconImage(img.getImage());
            frame.setSize(200, 360);
            frame.setBackground(Color.BLACK);
            frame.setResizable(false);
            frame.addWindowListener(this);
            Button b = new Button("Bảo trì");
            b.setBounds(30, 60, 140, 30);
            b.setActionCommand("stop");
            b.addActionListener(this);
            frame.add(b);
            Button b2 = new Button("Lưu Shinwa");
            b2.setBounds(30, 100, 140, 30);
            b2.setActionCommand("shinwa");
            b2.addActionListener(this);
            frame.add(b2);
            Button b3 = new Button("Lưu dữ liệu gia tộc");
            b3.setBounds(30, 140, 140, 30);
            b3.setActionCommand("clan");
            b3.addActionListener(this);
            frame.add(b3);
            Button b4 = new Button("Lưu dữ liệu người chơi");
            b4.setBounds(30, 180, 140, 30);
            b4.setActionCommand("player");
            b4.addActionListener(this);
            frame.add(b4);
            Button b5 = new Button("Làm mới TOP");
            b5.setBounds(30, 220, 140, 30);
            b5.setActionCommand("rank");
            b5.addActionListener(this);
            frame.add(b5);
            Button b6 = new Button("Restart DB");
            b6.setBounds(30, 260, 140, 30);
            b6.setActionCommand("restartDB");
            b6.addActionListener(this);
            frame.add(b6);
            frame.setLocationRelativeTo(null);
            frame.setLayout(null);
            frame.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void windowClosing(WindowEvent e) {
        frame.dispose();
        Server.start = false;
            System.out.println("Đóng máy chủ.");
            
            System.exit(0);
        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("shinwa")) {

            
            System.out.println("Mãy chủ chưa bật");
            
        }
        if (e.getActionCommand().equals("stop")) {
            
            this.maintance();
            System.out.println("Máy chủ chưa bật.");
            
        }
        if (e.getActionCommand().equals("clan")) {
            
            util.Debug("Lưu xong");
        }
        if (e.getActionCommand().equals("rank")) {
            for (int i = 0; i < BXHManager.bangXH.length; ++i) {
                BXHManager.initBXH(i);
            }
            BXHManager.init();
            System.out.println("Làm mới bảng xếp hạng");
            
        }
        if (e.getActionCommand().equals("player")) {
            System.out.println("Lưu dữ liệu người chơi");
        
        
        }
        if (e.getActionCommand().equals("restartDB")) {
            util.Debug("Bắt đầu khởi động lại!");
            
            util.Debug("Khởi động xong!");
        }
    }

    public void maintance() {
        try {
            System.out.println("Chuẩn bị đóng máy chủ.");
            manager.serverChat("Admin", "Máy chủ bảo trì sau 1 phút, vui lòng thoát game để tránh mất dữ liệu. Nếu cố tình không thoát chúng tôi không chịu trách nhiệm!");
            Service.startOKDlgServer("Máy chủ bảo trì sau 1 phút, vui lòng thoát game để tránh mất dữ liệu. Nếu cố tình không thoát chúng tôi không chịu trách nhiệm!");
            System.out.println("Hệ thống Đóng s1au 20s.");
            Thread.sleep(5000);
            System.out.println("Hệ thống Bắt đầu đóng máy chủ.");
            frame.dispose();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void init() {
        this.manager = new Manager();
        this.menu = new MenuController();
        this.controllerManager = new RealController();
        this.serverMessageHandler = new Controller();
        this.globalBattle = new GBattle();
        Server.kageTournament = KageTournament.gi();
        Server.geninTournament = GeninTournament.gi();
        Server.candyBattleManager = new CandyBattleManager();

/*        updateServer = () -> {
            for (int j = 0; j < Server.Server_REFRESH.length; ++j) {
                Calendar rightNow = Calendar.getInstance();
                int min = rightNow.get(12);
                int sec = rightNow.get(13);

                final short moment = (short) rightNow.get(Manager.Server_WAIT_TIME_UNIT);
                if (4 == moment && min == 0) {
                    int timeCount = 5;
                    while (timeCount > 0) {
                        Manager.serverChat("Thông báo Bảo trì", "Hệ thống sẽ bảo trì sau " + timeCount + " phút. Vui lòng thoát game trước thời gian bảo trì, để tránh mất vật phẩm và điểm kinh nghiệm. Xin cảm ơn!");
                        timeCount--;
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }
                    if (timeCount == 0) {
                        Server.isRefreshServer[j] = true;
                        stop();
                    }
                } else {
                    Server.isRefreshServer[j] = false;
                }
            }
        }; */

        updateRefreshBoss = () -> {

            synchronized (ClanManager.entrys) {
                for (int i = ClanManager.entrys.size() - 1; i >= 0; --i) {
                    final ClanManager clan = ClanManager.entrys.get(i);
                    if (util.compare_Week(Date.from(Instant.now()), util.getDate(clan.week))) {
                        clan.payfeesClan();
                    }
                }
            }

            final Calendar rightNow = Calendar.getInstance();
            final short moment = (short) rightNow.get(Manager.BOSS_WAIT_TIME_UNIT);
            for (int j = 0; j < Server.MOMENT_BOSS_REFRESH.length; ++j) {
                if (Server.MOMENT_BOSS_REFRESH[j] == moment) {
                    if (!Server.isRefreshBoss[j]) {
                        String textchat = "Thần thú đã xuất hiện tại";
                        for (byte k = 0; k < util.nextInt(1, 1); ++k) {
                            final Map map = Manager.getMapid(Server.mapBoss75[util.nextInt(Server.mapBoss75.length)]);
                            if (map != null) {
                                map.refreshBoss(util.nextInt(15, 29));
                                textchat = textchat + " " + map.template.name;
                                Server.isRefreshBoss[j] = true;
                            }
                        }
                        for (byte k = 0; k < util.nextInt(1, 2); ++k) {
                            final Map map = Manager.getMapid(Server.mapBoss65[util.nextInt(Server.mapBoss65.length)]);
                            if (map != null) {
                                map.refreshBoss(util.nextInt(15, 30));
                                textchat = textchat + ", " + map.template.name;
                                Server.isRefreshBoss[j] = true;
                            }
                        }
                        for (byte k = 0; k < util.nextInt(1, 2); ++k) {
                            final Map map = Manager.getMapid(Server.mapBoss55[util.nextInt(Server.mapBoss55.length)]);
                            if (map != null) {
                                map.refreshBoss(util.nextInt(15, 30));
                                textchat = textchat + ", " + map.template.name;
                                Server.isRefreshBoss[j] = true;
                            }
                        }
                        for (byte k = 0; k < util.nextInt(1, 2); ++k) {
                            final Map map = Manager.getMapid(Server.mapBoss45[util.nextInt(Server.mapBoss45.length)]);
                            if (map != null) {
                                map.refreshBoss(util.nextInt(15, 30));
                                textchat = textchat + ", " + map.template.name;
                                Server.isRefreshBoss[j] = true;
                            }
                        }
                        for (byte k = 0; k < Server.mapBossVDMQ.length; ++k) {
                            final Map map = Manager.getMapid(Server.mapBossVDMQ[k]);
                            if (map != null) {
                                map.refreshBoss(util.nextInt(15, 30));
                                textchat = textchat + ", " + map.template.name;
                                Server.isRefreshBoss[j] = true;
                            }
                        }

                       for (short i : mapBossLC) {
                            val map = Manager.getMapid(i);
                            if (map != null) {
                                map.refreshBoss(util.nextInt(0, 3));
                                textchat = textchat + ", " + map.template.name;
                                Server.isRefreshBoss[j] = true;
                            }
                        }
                 //     textchat = textchat + ", Làng cổ tạm bảo trì boss để thêm item";
                        try {
                            Manager.chatKTG(textchat);
                        } catch (IOException e) {
                        }
                    }
                } else {
                    Server.isRefreshBoss[j] = false;
                }
            }
        };
        updateBattle = () -> {
            final Calendar rightNow = Calendar.getInstance();
            final short moment = (short) rightNow.get(Manager.BOSS_WAIT_TIME_UNIT);
            for (int i = 0; i < MOMENT_REFRESH_BATTLE.length; i++) {

                if (MOMENT_REFRESH_BATTLE[i] == moment) {
                    if (this.globalBattle.getState() == INITIAL_STATE) {
                        this.globalBattle.reset();
                        this.globalBattle.setState(WAITING_STATE);
                    }
                    long second = Server.this.globalBattle.getTimeInSeconds();
                    if (second > 0 && this.globalBattle.getState() == WAITING_STATE) {
                        int phut = (int) (second / 60);
                        Manager.serverChat("Server", "Chiến trường sẽ bắt đầu trong " + (phut > 0 ? phut : second) + (phut > 0 ? " phút" : " giây"));
                    }
                }
            }
        };
        
        updateTTA = () -> {
            final Calendar rightNow = Calendar.getInstance();
            final short moment = (short) rightNow.get(11);
            short phut = (short) rightNow.get(12);
            for (int i = 0; i < 1; i++) {
                if (19 == moment && phut == 00) {
                    Server.dangKyTTA = true;
                    Server.timeTTA = System.currentTimeMillis() + 5400000L;
                    Server.timeWaitTTA = System.currentTimeMillis() + 1800000L;
                    Manager.serverChat("Server", "Thất thủ ải đã mở Báo Danh. Thời gian Báo Danh đến 19h30p.");
                }
                if (moment == 19 && phut == 30) {
                    Server.dangKyTTA = false;
                    Manager.serverChat("Server", "Thất thủ ải đã đóng Báo Danh");
                }
                if (moment == 20 && phut == 30) {
                    Server.timeTTA = -1;
                    Server.timeWaitTTA = -1;
                }
            }
        };

        clanTerritoryManager.start();


    }

    private static final Object MUTEX = new Object();


    public static Server getInstance() {
        if (Server.instance == null) {
            synchronized (MUTEX) {
                (Server.instance = new Server()).init();
            }
            instance.daemonThread = new DaemonThread();
            BXHManager.init();
            instance.daemonThread.addRunner(Server.updateTTA);
            instance.daemonThread.addRunner(Server.updateRefreshBoss);
            instance.daemonThread.addRunner(Server.updateBattle);
//            instance.daemonThread.addRunner(Server.updateServer);
        }
        return Server.instance;
    }

    public static boolean threadRunning = true;
    public static Thread t;

    public static void main(final String[] args) {
        Server.start = true;
        getInstance().run();

     /*   Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            threadRunning = false;
            if (t != null) {
                t.interrupt();
            }
        }));
        t = new Thread(() -> {
            while (threadRunning) {

                OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                int pt = (int) (osBean.getProcessCpuLoad() * 100);
                if (pt > 80) {
                    getInstance().stop();
                    getInstance().run();
                }

            }
        });
        t.start(); */
        try {
            t.join();
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void run() {
        this.setMaps(new Map[MapTemplate.arrTemplate.length]);
        for (short i = 0; i < Server.maps.length; ++i) {
            Server.maps[i] = new Map(i, null, null, null);
        }
        this.listenSocket = null;
        try {
            this.listenSocket = new ServerSocket(this.manager.PORT);
            System.out.println("Listenning port " + this.manager.PORT);

            try {
//                if (!util.debug) {
                Naming.rebind("rmi://127.0.0.1:16666/tinhtoan", new RmiRemoteImpl());
//                }
                System.out.println("Start rmi success");
            } catch (Exception e) {
                System.out.println("Start rmi fail");
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("RUN HOOK");
                for (Session conn : PlayerManager.getInstance().conns) {
                    if (conn != null && conn.user != null) {
                        conn.user.flush();
                    }
                }
                for (ClanManager entry : ClanManager.entrys) {
                    entry.flush();
                }
                System.out.println("CLOSE SERVER");
                stop();

            }));

            while (Server.start) {

                final Socket clientSocket = this.listenSocket.accept();
                final Session conn = new Session(clientSocket, this.serverMessageHandler);
                PlayerManager.getInstance().put(conn);
                conn.start();
                System.out.println("Accept socket size :" + PlayerManager.getInstance().conns_size());

            }
        } catch (BindException bindEx) {
            System.exit(0);
        } catch (SocketException genEx) {
            System.out.println("Socket Closed");
        } catch (IOException e) {
            System.out.println("IO EXCEPTION");
        }


        try {
            if (this.listenSocket != null) {
                this.listenSocket.close();
            }
            util.Debug("Close server socket");
        } catch (Exception ex) {
        }

    }

    public void stop() {
        if (Server.start) {
            Server.start = false;
            try {
                Tournament.closeAll();
            } catch (Exception e) {

            }
            try {
                Server.candyBattleManager.close();
            } catch (Exception e) {

            }
            try {
                this.listenSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                ClanManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                this.daemonThread.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                executorService.shutdown();
                if (executorService.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                    System.out.println("CLOSE");
                }
            } catch (Exception e) {
            }

            try {
                if (executorService.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (Exception e) {
                executorService.shutdownNow();
                e.printStackTrace();
            }

            try {
                if (executorService.isShutdown()) {
                    util.Debug("Shut down executor success");
                    executorService = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                for (Session conn : PlayerManager.getInstance().conns) {
                    if (conn != null && conn.user != null) {
                        PlayerManager.getInstance().kickSession_Fix(conn);
                    }
                }
                PlayerManager.getInstance().Clear();
                PlayerManager.getInstance().close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.manager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Server.clanTerritoryManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.manager = null;
            this.menu = null;
            this.controllerManager = null;
            this.serverMessageHandler = null;

            try {
                SQLManager.close();
            } catch (Exception e) {

            }

            System.gc();
        }
    }

    private static final short[] mapBossLC;

    static {
        Server.instance = null;
        Server.start = false;
        isRefreshBoss = new boolean[]{false, false, false, false, false, false};
        isRefreshServer = new boolean[]{false, false};
        mapBossVDMQ = new short[]{141, 142, 143};
        mapBoss45 = new short[]{14, 15, 16, 34, 35, 52, 68};
        mapBoss55 = new short[]{44, 67};
        mapBoss65 = new short[]{24, 41, 45, 59};
        mapBoss75 = new short[]{18, 36, 54};
        mapBossLC = new short[]{134, 135, 136, 137};
    }

    public Map[] getMaps() {
        return Server.maps;
    }

    public void setMaps(Map[] maps) {
        Server.maps = maps;
    }

    public static Map getMapById(int i) {
        return maps[i];
    }
    
}
