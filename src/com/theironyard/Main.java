package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        HashMap<String, User> userHashMap = new HashMap();
	    ArrayList<Sponsor> sponsorArrayList = new ArrayList();

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String loginName = session.attribute("login_name");
                    if(loginName == null){
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }//End of If loginName == Null
                    HashMap m = new HashMap();
                    m.put("login-name", loginName);
                    return new ModelAndView(new HashMap(), "logged-in.html");

                }),
                new MustacheTemplateEngine()
        );//End of Spark.get() "/" Main page

        Spark.post(
                "/login",
                ((request, response) -> {
                    String loginName = request.queryParams("login_name");
                    String logPass = request.queryParams("password");
                    if(loginName.isEmpty() || logPass.isEmpty()){
                        Spark.halt(403);
                    }
                    User tempUser = userHashMap.get(loginName);
                    if(tempUser == null){
                        tempUser = new User();
                        tempUser.password = logPass;
                        userHashMap.put(loginName, tempUser);
                    }//End of if loginName == Null
                    else if(!logPass.equals(tempUser.password)){
                        Spark.halt(403);
                    }
                    Session session = request.session();
                    session.attribute("login_name", loginName);

                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post "/logged-in" Logged-In Page


    }//End of Main Method

}//End of Main Class
