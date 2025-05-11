package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import models.AuthHandler;

public class loginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/_view/Login.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id   = req.getParameter("usernameOrEmail");
        String pass = req.getParameter("password");

        AuthHandler auth = (AuthHandler)getServletContext()
                                   .getAttribute("authHandler");
        if (auth == null) {
            throw new ServletException("AuthHandler not available");
        }

        if (!auth.validateUser(id, pass)) {
            req.setAttribute("errorMessage", "Invalid username/email or password");
            req.getRequestDispatcher("/_view/Login.jsp")
               .forward(req, resp);
            return;
        }

        // Success
        int userId = auth.getUserID(id);
        HttpSession session = req.getSession(true);
        session.setAttribute("user_id", userId);
        resp.sendRedirect(req.getContextPath() + "/home");
    }
}
