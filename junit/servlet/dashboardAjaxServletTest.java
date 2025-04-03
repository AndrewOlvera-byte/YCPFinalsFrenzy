
package servlet.ajax;

import GameEngine.GameEngine;
import models.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class dashboardAjaxServletTest extends Mockito {

    @Test
    public void testDoPostReturnsJson() throws Exception {
        dashboardAjaxServlet servlet = new dashboardAjaxServlet();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter writer = new StringWriter();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("gameEngine")).thenReturn(new GameEngine());
        when(request.getParameter("input")).thenReturn("help");
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doPost(request, response);
        String output = writer.toString();
        assertTrue(output.contains("{")); // Basic check for JSON format
    }
}
