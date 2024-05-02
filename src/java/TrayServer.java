import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TrayServer {

    public static final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void debug(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(pattern.format(ZonedDateTime.now()));
        sb.append(" DEBUG ");
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        //sout(sb.toString());
    }

    public static void info(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(pattern.format(ZonedDateTime.now()));
        sb.append(" INFO ");
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sout(sb.toString());
    }

    public synchronized static void sout(String txt) {
        System.out.println(txt);
    }

    public static void warn(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(pattern.format(ZonedDateTime.now()));
        sb.append(" WARN ");
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sout(sb.toString());
    }

    public static void error(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(pattern.format(ZonedDateTime.now()));
        sb.append(" ERROR ");
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sout(sb.toString());
    }

    public static final Base64.Decoder decoder = Base64.getDecoder();

    public static byte[] decode(String b64) {
        return decoder.decode(b64);
    }

    // https://www.iconsdb.com/green-icons/circle-icon.html 32x32 png
    public static final Image greenCircle = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB20lEQVRYhcXXvWsUQRjH8U/GQ8UqHBLBQoKdICGoXXwJhIBFGv8AGxEL0cLKwsIBEUURrATBShHBzkYscr70KlekCIIxhBAsNIQrYrhCLMbhYu4km+Pu9tft7rDf78zsy/MMKZqogpOYxjEcxjACGlhEHe8wK9ooctuhAuD9uIqLOFhQdw1PcV+03J1AFHAZt6SZdpMN3MNtUbO4QDSMFzjbJXhrPuOcaGl7gWgENRztETxnGdOi+f8LpJl/wFiP4TkrmBAt5hNhEzzgWR/hpIf4lWhvuwCXMNNHeM4YbuaDtAVp6b+iOgABaOKIaCGvwJUBwmE3rsPQ373/hkMDFIB1HAg4UQIc9mEmYLIEeM5UwPESBcYDRksUGA26/9H0IpWw/Zj+JhcTZaUZaP0YSshSwKcSBeoBb0sUqAV8VM42rON1EP3G4xIEXooa+TV8hB8DhDdxh1yQRA3cGKDAQ9GXlkDKE7wZAHxOW0WUE1WlorTXFXHOCk6JFvKJfz/F0arUes31CT61Gd4ukCS+4wxmewivS+X4/NYLuzoOf++XSc+xigns6RK8gbs4L/rZaUCR5nQE13ABIwXBuTl90Kkd25lAS6SC09IzMi6151VpG9ewJC11zQ7a8z8geGNhy88fnQAAAABJRU5ErkJggg==");
    // https://www.iconsdb.com/guacamole-green-icons/circle-icon.html
    public static final Image softGreen = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB9UlEQVRYhcXXO2gUURSH8d9egohVCBLBQoLdgISggoVPCEGLNLZTiliIIlYWt7DQQVAEK0GwUljBzkYssoq9SorABsEYQggWIiFFCFuIxeyENYlksu7u/Lt5MN83Zx73nJqSqTezIZzBFI7jKIYRsIZFzOIDZtIkbpS5bq0E+CBu4ioOl/RdxQs8SpO43JVAvZkFXMc9+Z12kw08RJYmsVVaoN7MhvEKl7oEb80XXE6TuLSrQL2ZjaKBYz2CF1nGVJrE+X8KtO/8I8Z7DC+ygtNpEheLHaEDHvCyj3Dyl/hNvZnt3yaAa5juI7zIOO4WGzU2S/8NIwMQgBaSNIkLRQVuDBAO+3AHau1n/x1HBigA6zgUcLICOBzAdMCFCuBFJgNOVCgwETBWocBY0P1C04sMhd3P6W+KZqKqtIK8k6kqSwGfKxSYDXhfoUAj4JNqHsM63oY0ib/xrAKB12kS14rP8Cl+DhDewgPaDUmaxDXEAQo8SZP4dVOgned4NwD4nK0dUZF6MxuRN6W97oiLrOBsmsSFYsdfv+I0ib/ko9dcn+CTnfBtAm2JHziPmR7CZ+Xt+PzWAzsuRu1KXMQt/7dWbOA+TnXOAp0pM5yO4jauYLQkuBhOH+80ju1JoENkCOfk78iEfDwfkVdxFUvyUjfsYTz/AyNDf+XwTOOTAAAAAElFTkSuQmCC");

    // https://www.iconsdb.com/soylent-red-icons/circle-icon.html 32x32 png
    public static final Image redCircle = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB8klEQVRYhcXXu2sUURiG8d8ewiBWIUgECznYCRKCChbeFkLQIo3/goiFKGJlYWGhIiiClSBYaSHY2QSLRMdeJUWKIBiHEIKFSEgRwinEYnbCmkSyWXd33m4uzPPMN5fzfQ0dpmhmQziDSRzHEQwjYA0F5vABMzFPG51ct9EB+ABu4AoOdei7ipd4HPO03JVA0cwCruGe8k67yQYe4UHMU+pYoGhmw3iNi12Ct+YLLsU8Le0qUDSzUcziWI/gVZYxGfO08E+B1p1/xFiP4VVWcDrmqah2hDZ4wKs+wilf4rdFM9u3TQBXMdVHeJUx3K02GmyW/htGBiAACUdjnharClwfIBwy3IZG69l/x+EBCsA6DgacrAEO+zEV0KwBXmUi4ESNAuMBsUaBGHS/0PQiQ2H3c/qbqpmoKykoO5m6shTwuUaBuYD3NQrMBnxSz2NYx3SIefqN5zUIvIl5Wqs+w2f4OUB4wkNaDUnM0xruDFDgaczT102BVl7g3QDg87Z2RFWKZjaibEp73RFXWcHZmKfFasdfv+KYp1/K0Wu+T/CJdvg2gZbED5zHTA/hc8p2fGHrgR0Xo1YlLuCm/1srNnAfp9pngfZ0MpyO4hYuY7RDcDWcPtlpHNuTQJvIEM4p35Fx5Xg+oqziKpaUpZ61h/H8D5n/helMUpbSAAAAAElFTkSuQmCC");
    public static final Image softRed = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB8UlEQVRYhcXXu2sUURiG8d85BBGrECSChQQ7QUJQwcIrSNAijf+CiIUoYiUHCwuVA4pgJQhWWgh2NmKRKPYqKVIEwRhCCBYiIUUIW7gWMxM2F8lm3d15u7kwzzPfXM73BW2mmfMATmMcx3AYg4hYwTym8RGTIaW1dq4b2gDvx01cxcE2fZfxEo9DSosdCTRzjriO+4o77SRreISHIaVG2wLNnAfxGpc6BG/OV1wOKS3sKNDMeRhTONoleJVFjIeUZv8pUN75J4x2GV5lCadCSvPVjtgCj3jVQzjFS/y2mfPeLQK4hokewquM4l61EVgv/XcM9UEAGjgSUpqrKnCjj3DYgzsQymf/A4f6KACrOBBxogY47MNExPka4FUuRByvUWAsYqRGgZGo84WmGxmIO5/T21TNRF1pREUnU1cWIr7UKDAd8aFGgamIz+p5DKt4F0NKf/C8BoE3IaWV6jN8hl99hDeQKRuSkNIK7vZR4GlI6du6QJkXeN8H+IzNHVGVZs5Diqa02x1xlSWcCSnNVTs2/IpDSr8Vo9dMj+AXWuFbBEqJnziHyS7CpxXt+OzmA9suRmUlLuKW/1sr1vAAJ1tngQ2sna5QTkq3cQXDbYKr4fTJduPYrgRaRAZwVvGOjCnG8yFFFZexoCj1lF2M538Bp3Z/mOPDH1UAAAAASUVORK5CYII=");

    // https://www.iconsdb.com/orange-icons/circle-icon.html 32x32 png
    public static final Image orangeCircle = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB6ElEQVRYhcXXO2gUURSH8d+9BBGrECSChQQ7QUJQwcInhKBFGuvbiViIIlYWFhYqgiJYCYKVsgjpbMQiUexVUqQIgjGEECxEQooQtnAtZm7cPCSbdXfn382D+b4587jnBC2mUdOH0xjDMRxGPyJWMI9pfMBkSNZauW5oAbwfN3AFB1v0XcZLPA7JYlsCjZqIa7inuNN2soZHeBCSessCjZp+vMbFNsGb8wWXQrKwo0CjZhBTONoheM4ixkIy+0+B8s4/YrjD8JwlnArJfN4Rm+ARr7oIp3iJ3zRq9m4RwFWMdxGeM4y7eSOwXvpvGOiBANRxJCRzuQLXewiHPbgNoXz233GohwKwigMRJyqAwz6MR5yvAJ4zGnG8QoGRiKEKBYai9heaTqQv7nxOd5ObiapSj/xdGCrIQsTnCgWmI95XKDAV8Uk1j2EVb2NIfuN5BQITIVnJn+Ez/OwhvI6HlA1JSFZwp4cCT0PydV2gzAu86wF8xuaOKKdRM6BoSjvdEecs4UxI5vKODb/ikPxSjF4zXYKPNsO3CJQSP3AOkx2ETyva8dnNB7ZdjMpKXMBN/7dWrOE+TjbPAhtYO12hnJRu4TIGWwTn4fTJduPYrgSaRPpwVvGOjCjG8wFFFZexoCj1lF2M538ASU9ud/YbD64AAAAASUVORK5CYII=");
    public static final Image softOrange = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB9UlEQVRYhcXXO2gUURSH8d+9BBGrECSChQQ7QUK4ChY+IQQt0thvJWIhilhZWFioCIpgJQhWOiDY2YhFotjrkCJFEIwhhGAhElKEsIVrMTth85Bs1t2dfzcP5vvmzOOeE7SZRl4bwBlMIOEoBhGxigXM4BOmQsrW27luaAN8EDdxFYfb9F3BKzwJKVvqSKCR1yKu477iTjvJOh7jYUhZvW2BRl4bxBtc6hC8NTkuh5Qt7irQyGvDmMbxLsHLLGEipGzunwLNO/+M0S7DyyzjdEjZQrkjtsAjXvcQTvESv2vktf3bBHANkz2ElxnFvXIjsFH67xjqgwDUcSykbL6swI0+wmEf7kBoPvsfONJHAVjDoYiTFcDhACYjLlQALzMecaJCgbGIkQoFRqLOF5puZCDufk5vUzYTVaUeFZ1MVVmM+FqhwEzExwoFpiO+qOYxrOF9DCn7gxcVCLwNKVstP8Pn+NVHeB2PaDYkIWWruNtHgWchZd82BJp5iQ99gM/a2hGVaeS1IUVT2u2OuMwyzoaUzZc7Nv2KQ8p+K0av2R7Bx1vh2wSaEj9xHlNdhM8o2vG5rQd2XIyalbiIW/5vrVjHA5xqnQU2sXa7QnNSuo0rGG4TXA6nT3cax/Yk0CIygHOKd2RMMZ4PKaq4gkVFqaftYTz/C/zoftTxqXxBAAAAAElFTkSuQmCC");

    public static final Map<String, Image> images = new TreeMap<>() {{
        put("green", greenCircle);
        put("orange", orangeCircle);
        put("red", redCircle);

        put("softgreen", softGreen);
        put("softorange", softOrange);
        put("softred", softRed);
    }};

    public static void sendStringResponse(int code, String body, HttpExchange exchange) {
        try (OutputStream os = exchange.getResponseBody();
             ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))) {
            exchange.sendResponseHeaders(code, body.length());
            bis.transferTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getQueryParamValue(HttpExchange exchange, String key, String defaultValue) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            return defaultValue;
        }
        Map<String, String> queryMap = Pattern.compile("&")
                .splitAsStream(query)
                .map(s -> Arrays.copyOf(s.split("=", 2), 2))
                .collect(Collectors.toMap(e -> e[0], e -> e[1]));
        return queryMap.getOrDefault(key, defaultValue);
    }

    public static final Image startImage = greenCircle;

    public static Image getImage(String base64) {
        return (new ImageIcon(decode(base64))).getImage();
    }

    public static synchronized void setImage(Config cfg, Image newImage) {
        if (cfg.currImage.get() == newImage) {
            debug("same image requested, dropping request");
        } else {
            cfg.currImage.set(newImage);
            cfg.trayIcon.setImage(newImage);
        }
    }

    public static TrayIcon createAndShowTray(String origin) {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            error("SystemTray is not supported");
            System.exit(1);
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(startImage);
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("origin: " + origin);
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
        Menu displayMenu = new Menu("Display");
        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem infoItem = new MenuItem("Info");
        MenuItem noneItem = new MenuItem("None");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        displayMenu.add(infoItem);
        displayMenu.add(noneItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            error("TrayIcon could not be added.");
            System.exit(1);
        }

        trayIcon.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(originToConfig.get(origin).link.get()));
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
//            JOptionPane.showMessageDialog(null, "This dialog box is run from System Tray");
        });

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null,
                "This dialog box is run from the About menu item"));

        cb1.addItemListener(e -> {
            int cb1Id = e.getStateChange();
            trayIcon.setImageAutoSize(cb1Id == ItemEvent.SELECTED);
        });

        trayIcon.setToolTip("mjau...");

        cb2.addItemListener(e -> {
            int cb2Id = e.getStateChange();
            if (cb2Id == ItemEvent.SELECTED) {
                trayIcon.setToolTip("Sun TrayIcon");
            } else {
                trayIcon.setToolTip(null);
            }
        });

        ActionListener listener = e -> {
            MenuItem item = (MenuItem) e.getSource();
            //TrayIcon.MessageType type = null;
            if ("Error".equals(item.getLabel())) {
                //type = TrayIcon.MessageType.ERROR;
                trayIcon.displayMessage("Sun TrayIcon Demo",
                        "This is an error message", TrayIcon.MessageType.ERROR);

            } else if ("Warning".equals(item.getLabel())) {
                //type = TrayIcon.MessageType.WARNING;
                trayIcon.displayMessage("Sun TrayIcon Demo",
                        "This is a warning message", TrayIcon.MessageType.WARNING);

            } else if ("Info".equals(item.getLabel())) {
                //type = TrayIcon.MessageType.INFO;
                trayIcon.displayMessage("Sun TrayIcon Demo",
                        "This is an info message", TrayIcon.MessageType.INFO);

            } else if ("None".equals(item.getLabel())) {
                //type = TrayIcon.MessageType.NONE;
                trayIcon.displayMessage("Sun TrayIcon Demo",
                        "This is an ordinary message", TrayIcon.MessageType.NONE);
            }
        };

        errorItem.addActionListener(listener);
        warningItem.addActionListener(listener);
        infoItem.addActionListener(listener);
        noneItem.addActionListener(listener);

        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            originToConfig.remove(origin);
            if (originToConfig.isEmpty()) {
                info("Exiting");
                System.exit(0);
            }
            {
                info("TrayIcon for origin " + origin + " exiting");
            }
        });
        info("Started TrayIcon for origin " + origin);
        return trayIcon;
    }

    public static final class Config {
        public final TrayIcon trayIcon;
        public final AtomicLong lastImageSet;
        public final AtomicReference<Image> currImage = new AtomicReference<>(startImage);
        public final AtomicReference<String> lastImageStr = new AtomicReference<>("green");
        public final AtomicReference<String> link = new AtomicReference<>("https://github.com/ivarref/tray-server");

        public Config(TrayIcon trayIcon, long lastImageSet) {
            this.trayIcon = trayIcon;
            this.lastImageSet = new AtomicLong(lastImageSet);
        }
    }

    public static final ConcurrentHashMap<String, Config> originToConfig = new ConcurrentHashMap<>();

    public static Config getConfig(String origin) {
        return originToConfig.computeIfAbsent(origin, o -> new Config(createAndShowTray(origin), 0));
    }

    public static void handleRequest(HttpExchange exchange) throws Exception {
        long startNanos = System.nanoTime();
        String path = exchange.getRequestURI().getPath();
        String newImgStr = getQueryParamValue(exchange, "img", "<missing>");
        String origin = getQueryParamValue(exchange, "origin", "default");
        Image newImage = images.getOrDefault(newImgStr, null);
        long nowMs = System.currentTimeMillis();
        String newLink = getQueryParamValue(exchange, "link", "::none");

        if (newImage == null) {
            warn("Unknown img parameter: " + newImgStr);
            warn("Valid img values are: " + images.keySet());
            sendStringResponse(200, "OK: no image requested. See usage at https://github.com/ivarref/tray-server\n", exchange);
        } else {
            Config cfg = getConfig(origin);
            cfg.lastImageSet.set(nowMs);
            cfg.lastImageStr.set(newImgStr);
            if (!newLink.equals("::none")) {
                cfg.link.set(newLink);
            }
            setImage(cfg, newImage);
            sendStringResponse(200, "OK\n", exchange);
            long usedMicros = (System.nanoTime() - startNanos) / 1000;
            debug("request for path: " + path + " with img: " + newImgStr + " link: " + newLink + " origin: " + origin
                    + " in " + String.format(Locale.US, "%,d", usedMicros) + " µs");
        }
    }

    public static void main(String[] args) throws IOException {
        debug("TrayMonitor starting");
        UIManager.put("swing.boldMetal", Boolean.FALSE);
//        SwingUtilities.invokeLater(TrayServer::createAndShowGUI);

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 17999), 0);
        } catch (BindException e) {
            error("Could not bind to localhost:17999. Is TrayServer already running?");
            error("Exiting");
            System.exit(1);
            return;
        }

        server.createContext("/", exchange -> {
            try {
                handleRequest(exchange);
            } catch (Throwable t) {
                t.printStackTrace();
                sendStringResponse(500, "500 Internal server error: " + t.getMessage() + " of type " + t.getClass().getSimpleName() + "\n", exchange);
            }
        });
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.scheduleAtFixedRate(() -> {
            for (Config cfg : originToConfig.values()) {
                long noDataMillis = System.currentTimeMillis() - cfg.lastImageSet.get();
                if (Duration.ofMillis(noDataMillis).toMinutes() >= 1) {
                    Image maybeSoftImage = images.getOrDefault("soft" + cfg.lastImageStr.get(), cfg.currImage.get());
                    setImage(cfg, maybeSoftImage);
                }
            }
        }, 3, 3, TimeUnit.SECONDS);

        server.setExecutor(executor);
        server.start();
        info("WebServer running at http://localhost:17999");
    }
}
