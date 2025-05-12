package servlet.websocket;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebSocketContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        // Pass the ServletContext to our WebSocket endpoint
        GameWebSocketEndpoint.setServletContext(ctx);
        System.out.println("WebSocket context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
        System.out.println("WebSocket context destroyed");
    }
} 