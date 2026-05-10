import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebUI {
    private Controller_GUI controller;

    public WebUI(Controller_GUI controller) {
        this.controller = controller;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/menu", new MenuHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Web UI started on http://localhost:3000");
    }

    class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<html><body><h1>Restaurant Management System</h1><a href='/menu'>View Menu</a></body></html>";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    class MenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder response = new StringBuilder("<html><body><h1>Menu</h1><ul>");
            for (MenuItem item : controller.getAllMenuItems()) {
                response.append("<li>").append(item.getName()).append(" - ₹").append(item.getPrice()).append("</li>");
            }
            response.append("</ul></body></html>");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }

    public static void main(String[] args) {
        Controller_GUI cController = new Controller_GUI();
        WebUI webUI = new WebUI(cController);
        try {
            webUI.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}