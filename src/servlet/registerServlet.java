package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import models.AuthHandler;

public class registerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/_view/Register.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        AuthHandler auth = (AuthHandler)getServletContext()
                                   .getAttribute("authHandler");
        if (auth == null) {
            throw new ServletException("AuthHandler not available");
        }

        boolean ok = auth.createUser(username, email, password);
        if (ok) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // determine which field was duplicate
        String error;
        if (auth.usernameExists(username)) {
            error = "Username already in use";
        } else if (auth.emailExists(email)) {
            error = "Email already in use";
        } else {
            error = "Username or email already in use";
        }
        req.setAttribute("errorMessage", error);
        req.getRequestDispatcher("/_view/Register.jsp")
           .forward(req, resp);
    }
}
