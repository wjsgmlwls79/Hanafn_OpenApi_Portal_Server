package com.hanafn.openapi.portal.testModule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/b2b2c")
@CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
public class bizServlet extends HttpServlet {

    @RequestMapping(value="/b-ti",  method = RequestMethod.POST)
    public void bizToBank (HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("param1: "+request.getParameter("param1"));
        System.out.println("param2: "+request.getParameter("param2"));
        System.out.println("redirect_url: "+request.getParameter("redirect_url"));

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head>");
        out.println("<title>Help Page</title></head><body>");
        out.println("<h2>Please submit your information</h2>");
        out.println("<form name=\"frm\" method=\"post\" action =\"" + request.getParameter("redirect_url") +"\" >");
        out.println("<table border=\"0\"><tr><td valign=\"top\">");
        out.println("param1 : </td>  <td valign=\"top\">");
        out.println("<input type=\"text\" name=\"param1\" value =\"" + request.getParameter("param1") +"\" size=\"20\">");
        out.println("</td></tr><tr><td valign=\"top\">");
        out.println("param2 : </td>  <td valign=\"top\">");
        out.println("<input type=\"text\" name=\"param2\" value =\"" + request.getParameter("param2") +"\" size=\"20\">");
        out.println("</td></tr><tr><td valign=\"top\">");
        out.println("</td></tr><tr><td valign=\"top\">");
        out.println("<input type=\"submit\" value=\"Submit Info\"></td></tr>");
        out.println("</table></form>");
        out.println("</body></html>");
        out.println("<script>");
        out.println("alert('bank')");
//        out.println("window.onload=function(){frm.submit();}");
        out.println("</script>");
    }
}