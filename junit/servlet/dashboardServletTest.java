
package servlet;

import GameEngine.GameEngine;
import models.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class dashboardServletTest extends Mockito {

    @Test
    public void testDoGetInitializesGameEngine() throws Exception {
        dashboardServlet servlet = new dashboardServlet();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("gameEngine")).thenReturn(null);

        servlet.doGet(request, response);
        verify(session).setAttribute(eq("gameEngine"), any(GameEngine.class));
    }
}
