package com.services;

import com.model.UserDAO;
import com.model.UserEntity;
import com.utils.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      Integer port = StringUtils.toInt(req.getParameter("port"));
      UserEntity user = UserDAO.getInstance().getByPort(port);

      CommandUtils.startContainer(null, user.getContainerName());

      MailGunUtils.sendHtmlMessage(
              user.getEmail(),
              "SSH Container information",
              "<h2>SSH to Container info</h2>" +
                      "<p>IP Address: " + JSchSessionUtils.getInstance().getHost() + "<br/>" +
                      "Port: " + port + "<br/>" +
                      "Username: root<br/>" +
                      "<br/>" +
                      "<p>Connect to your container using Window Command line by this command <br/>" +
                      "<pre>" + JSchSessionUtils.getInstance().getSshCommand("root", port) + "</pre></p>" +
                      "<br/>" +
                      "<p>Or using our SSH Connection at <a href=\"" + ServletUtils.getUrl(req) + "\">here</a></p>"
      );

      ServletUtils.forward(req, resp, "./start-success.jsp");
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String result = "";
      String fullName = req.getParameter("full-name");
      String email = req.getParameter("email");
      String memory = req.getParameter("memory");
      String cpu = req.getParameter("cpu");
      String password = req.getParameter("password");

      UserEntity newUser = new UserEntity(fullName, email);
      Integer port = UserDAO.getInstance().insert(newUser);
      if (port != 0) {
         Boolean createContainerResult = CommandUtils.createSshContainer(
                 null,
                 newUser.getContainerName(),
                 port,
                 memory,
                 cpu,
                 password
         );

         if (createContainerResult) {
            result += "true\n" + port.toString();
            String startLink = ServletUtils.getUrl(req, "register?port=" + port);
            MailGunUtils.sendHtmlMessage(
                    email,
                    "Container successfully created",
                    "<h2>Click this link to start your container</h2>" +
                            "<a href=\"" + startLink + "\">" + startLink + "</a>"
            );
         } else {
            result += "false\n" + "Can't create container";
         }
      } else {
         result += "false\n" + "Can't insert user";
      }

      try (PrintWriter out = resp.getWriter()) {
         out.write(result);
      }
   }
}
