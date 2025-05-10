package servlet.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import models.AuthHandler;

public class loginAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id   = req.getParameter("usernameOrEmail");
        String pass = req.getParameter("password");

        AuthHandler auth = (AuthHandler)getServletContext()
                                   .getAttribute("authHandler");
        if (auth == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "AuthHandler not available");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (!auth.validateUser(id, pass)) {
            resp.getWriter()
                .write("{\"errorMessage\":\"Invalid username/email or password\"}");
            return;
        }

        // Success
        HttpSession session = req.getSession(true);
        session.setAttribute("user_id", auth.getUserID(id));
        String redirect = req.getContextPath() + "/dashboard";
        resp.getWriter()
            .write("{\"redirectUrl\":\"" + redirect + "\"}");
    }
}
