package listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import GameEngine.GameEngine;
import models.DatabaseInitializer;
import models.AuthHandler;

@WebListener
public class GameEngineContextListener implements ServletContextListener {

    private GameEngine engine;
    private AuthHandler auth;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        // 1) Initialize Derby schema if you like:
        DatabaseInitializer.initialize();  // create tables if missing

        // 2) Create and start your global engine
        engine = new GameEngine();
        engine.startWithoutPlayer();
        
        auth = new AuthHandler();

        // 3) Store it for all servlets to share
        ctx.setAttribute("gameEngine", engine);

        ctx.log("GameEngine initialized and stored in ServletContext");
        
        ctx.setAttribute("authHandler", auth);
        
        ctx.log("AuthHandler initialized and stored in ServletContext");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // clean up on webapp shutdown
        if (engine != null) {
            engine.reset();   // if you have a tear-down method
        }
    }
}
