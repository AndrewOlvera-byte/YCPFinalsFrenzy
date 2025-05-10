package servlet.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import models.AuthHandler;

public class registerAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        AuthHandler auth = (AuthHandler)getServletContext()
                                   .getAttribute("authHandler");
        if (auth == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "AuthHandler not available");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        boolean ok = auth.createUser(username, email, password);
        if (ok) {
            String redirect = req.getContextPath() + "/login";
            resp.getWriter()
                .write("{\"redirectUrl\":\"" + redirect + "\"}");
            return;
        }

        // duplicate, figure out which field
        String error;
        if (auth.usernameExists(username)) {
            error = "Username already in use";
        } else if (auth.emailExists(email)) {
            error = "Email already in use";
        } else {
            error = "Username or email already in use";
        }
        resp.getWriter()
            .write("{\"errorMessage\":\"" + error + "\"}");
    }
}
