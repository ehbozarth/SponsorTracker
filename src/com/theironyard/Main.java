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
	    ArrayList<Golfer> golferArrayList = new ArrayList();

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String loginName = session.attribute("login_name");
                    if (loginName == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }//End of If loginName == Null
                    HashMap m = new HashMap();
                    m.put("login-name", loginName);
                    m.put("golfers", golferArrayList);
                    return new ModelAndView(m, "logged-in.html");

                }),
                new MustacheTemplateEngine()
        );//End of Spark.get() "/" Main page

        Spark.post(
                "/login",
                ((request, response) -> {
                    String loginName = request.queryParams("login_name");
                    String logPass = request.queryParams("password");
                    if (loginName.isEmpty() || logPass.isEmpty()) {
                        Spark.halt(403);
                    }
                    User tempUser = userHashMap.get(loginName);
                    if (tempUser == null) {
                        tempUser = new User();
                        tempUser.password = logPass;
                        userHashMap.put(loginName, tempUser);
                    }//End of if loginName == Null
                    else if (!logPass.equals(tempUser.password)) {
                        Spark.halt(403);
                    }
                    Session session = request.session();
                    session.attribute("login_name", loginName);

                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post "/logged-in" Logged-In Page


        Spark.post(
                "/create-golfer",
                ((request, response) -> {
                    Golfer tempGolfer = new Golfer();
                    tempGolfer.id = golferArrayList.size() + 1;
                    tempGolfer.golferName = request.queryParams("golfer_name");
                    tempGolfer.sponsorName = request.queryParams("sponsor_name");
                    golferArrayList.add(tempGolfer);
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() /create-golfer

        Spark.post(
                "/delete-golfer",
                ((request, response) -> {
                    String id = request.queryParams("golfer_id");
                    try {
                        int idNum = Integer.valueOf(id);
                        golferArrayList.remove(idNum-1);
                        for(int i = 0; i < golferArrayList.size(); i++){
                            golferArrayList.get(i).id = i + 1;
                        }
                    } catch (Exception e){

                    }
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() /delete-golfer


    }//End of Main Method

}//End of Main Class
