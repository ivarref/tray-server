import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TrayServer {
    private static volatile TrayIcon trayIcon;

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

    // https://www.iconsdb.com/soylent-red-icons/circle-icon.html 32x32 png
    public static final Image redCircle = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB8klEQVRYhcXXu2sUURiG8d8ewiBWIUgECznYCRKCChbeFkLQIo3/goiFKGJlYWGhIiiClSBYaSHY2QSLRMdeJUWKIBiHEIKFSEgRwinEYnbCmkSyWXd33m4uzPPMN5fzfQ0dpmhmQziDSRzHEQwjYA0F5vABMzFPG51ct9EB+ABu4AoOdei7ipd4HPO03JVA0cwCruGe8k67yQYe4UHMU+pYoGhmw3iNi12Ct+YLLsU8Le0qUDSzUcziWI/gVZYxGfO08E+B1p1/xFiP4VVWcDrmqah2hDZ4wKs+wilf4rdFM9u3TQBXMdVHeJUx3K02GmyW/htGBiAACUdjnharClwfIBwy3IZG69l/x+EBCsA6DgacrAEO+zEV0KwBXmUi4ESNAuMBsUaBGHS/0PQiQ2H3c/qbqpmoKykoO5m6shTwuUaBuYD3NQrMBnxSz2NYx3SIefqN5zUIvIl5Wqs+w2f4OUB4wkNaDUnM0xruDFDgaczT102BVl7g3QDg87Z2RFWKZjaibEp73RFXWcHZmKfFasdfv+KYp1/K0Wu+T/CJdvg2gZbED5zHTA/hc8p2fGHrgR0Xo1YlLuCm/1srNnAfp9pngfZ0MpyO4hYuY7RDcDWcPtlpHNuTQJvIEM4p35Fx5Xg+oqziKpaUpZ61h/H8D5n/helMUpbSAAAAAElFTkSuQmCC");

    // https://www.iconsdb.com/orange-icons/circle-icon.html 32x32 png
    public static final Image orangeCircle = getImage("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAB6ElEQVRYhcXXO2gUURSH8d+9BBGrECSChQQ7QUJQwcInhKBFGuvbiViIIlYWFhYqgiJYCYKVsgjpbMQiUexVUqQIgjGEECxEQooQtnAtZm7cPCSbdXfn382D+b4587jnBC2mUdOH0xjDMRxGPyJWMI9pfMBkSNZauW5oAbwfN3AFB1v0XcZLPA7JYlsCjZqIa7inuNN2soZHeBCSessCjZp+vMbFNsGb8wWXQrKwo0CjZhBTONoheM4ixkIy+0+B8s4/YrjD8JwlnArJfN4Rm+ARr7oIp3iJ3zRq9m4RwFWMdxGeM4y7eSOwXvpvGOiBANRxJCRzuQLXewiHPbgNoXz233GohwKwigMRJyqAwz6MR5yvAJ4zGnG8QoGRiKEKBYai9heaTqQv7nxOd5ObiapSj/xdGCrIQsTnCgWmI95XKDAV8Uk1j2EVb2NIfuN5BQITIVnJn+Ez/OwhvI6HlA1JSFZwp4cCT0PydV2gzAu86wF8xuaOKKdRM6BoSjvdEecs4UxI5vKODb/ikPxSjF4zXYKPNsO3CJQSP3AOkx2ETyva8dnNB7ZdjMpKXMBN/7dWrOE+TjbPAhtYO12hnJRu4TIGWwTn4fTJduPYrgSaRPpwVvGOjCjG8wFFFZexoCj1lF2M538ASU9ud/YbD64AAAAASUVORK5CYII=");

    public static Image getImage(String base64) {
        return (new ImageIcon(decode(base64))).getImage();
    }

    public static final AtomicReference<Image> currImage = new AtomicReference<>(orangeCircle);

    public static synchronized void setImage(Image newImage) {
        if (currImage.get() == newImage) {
            info("dropping setting new image");
        } else {
            currImage.set(newImage);
            trayIcon.setImage(newImage);
        }
    }

    private static void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            error("SystemTray is not supported");
            System.exit(1);
            return;
        }
        final PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(orangeCircle);
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
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
            return;
        }

        trayIcon.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("http://www.example.com"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
//            JOptionPane.showMessageDialog(null, "This dialog box is run from System Tray");
        });

        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null,
                "This dialog box is run from the About menu item"));

        cb1.addItemListener(e -> {
            int cb1Id = e.getStateChange();
            if (cb1Id == ItemEvent.SELECTED) {
                trayIcon.setImageAutoSize(true);
            } else {
                trayIcon.setImageAutoSize(false);
            }
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
            info("Exiting");
            System.exit(0);
        });
        info("Started!");
    }

    public static void sendStringResponse(int code, String body, HttpExchange exchange) {
        try (OutputStream os = exchange.getResponseBody();
             ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))) {
            exchange.sendResponseHeaders(200, body.length());
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

    public static void main(String[] args) throws IOException {
        info("TrayMonitor starting");
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(TrayServer::createAndShowGUI);

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 17999), 0);
        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                String newImg = getQueryParamValue(exchange, "img", "<missing>");
                if ("red".equalsIgnoreCase(newImg)) {
                    setImage(redCircle);
                } else if ("green".equalsIgnoreCase(newImg)) {
                    setImage(greenCircle);
                } else if ("orange".equalsIgnoreCase(newImg)) {
                    setImage(orangeCircle);
                } else {
                    warn("Unknown img parameter: " + newImg);
                }
                info("request for path: " + path + " with img: " + newImg);
                sendStringResponse(200, "OK\n", exchange);
            } catch (Throwable t) {
                t.printStackTrace();
                sendStringResponse(500, "500 Internal server error: " + t.getMessage() + " of type " + t.getClass().getSimpleName() + "\n", exchange);
            }
        });
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        info("WebServer running at http://localhost:17999");
    }
}
