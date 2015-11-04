package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS golfers (id IDENTITY, user_id INT, golfer_name VARCHAR, sponsor_name VARCHAR)");
    }//End of createTables Method

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO users VALUES(NULL , ? , ?)");
        statement.setString(1, name);
        statement.setString(2, password);
        statement.execute();
    }//End of insertUser Method

    public static User selectUser(Connection conn, String name) throws SQLException {
        User user = null;
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        statement.setString(1, name);
        ResultSet results = statement.executeQuery();
        while(results.next()){
            user = new User();
            user.id = results.getInt("id");
            user.password = results.getString("password");
        }
        return user;
    }//End of selectUser

    public static void insertEntry(Connection conn, int userId, String golfName, String sponsor) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO  golfers VALUES(NULL, ?, ?, ?)");
        statement.setInt(1, userId);
        statement.setString(2, golfName);
        statement.setString(3, sponsor);
        statement.execute();
    }//End of insertEntry Method


public static Golfer selectEntry(Connection conn, int id) throws SQLException{
        Golfer golfer = null;
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM golfers " +
                "INNER JOIN users ON golfers.user_id = users.id WHERE golfers.id = ?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        if(results.next()){
            golfer = new Golfer();
            golfer.id = results.getInt("golfers.id");
            golfer.golferName = results.getString("golfers.golfer_name");
            golfer.userName = results.getString("users.name");
            golfer.sponsorName = results.getString("golfers.sponsor_name");
        }//End of if
        return golfer;
    }

    public static ArrayList<Golfer> selectEntries(Connection conn, int id) throws SQLException {
        ArrayList<Golfer> golferArrayList = new ArrayList<>();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM golfers " +
                "INNER JOIN users ON golfers.user_id = users.id where golfers.id = ?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        while(results.next()){
            Golfer golfer = new Golfer();
            golfer.id = results.getInt("golfers.id");
            golfer.golferName = results.getString("golfers.golfer_name");
            golfer.userName = results.getString("users.name");
            golfer.sponsorName = results.getString("golfers.sponsor_name");
            golferArrayList.add(golfer);
        }
        return golferArrayList;
    }//End of selectEntries

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        /*
        HashMap<String, User> userHashMap = new HashMap();
	    ArrayList<Golfer> golferArrayList = new ArrayList();
        */
        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String loginName = session.attribute("login_name");
                    ArrayList<Golfer> golferArrayList = selectEntries(conn, 1);

                    HashMap m = new HashMap();
                    m.put("login-name", loginName);
                    m.put("golfers", golferArrayList);
                    if (loginName == null) {
                        return new ModelAndView(m, "not-logged-in.html");
                    }//End of If loginName == Null
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
                    User tempUser = selectUser(conn, loginName);
                    if (tempUser == null) {
                        //tempUser = new User();
                        //tempUser.password = logPass;
                        //userHashMap.put(loginName, tempUser);
                        insertUser(conn, loginName, logPass);
                    }//End of if loginName == Null
                    else if (!logPass.equals(tempUser.password)) {
                        Spark.halt(403);
                    }
                    Session session = request.session();
                    session.attribute("login_name", loginName);
                    /*
                    for(Golfer golfer : golferArrayList){
                        golfer.authorized = golfer.userName.equals(loginName);
                    }
                    */

                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post "/logged-in" Logged-In Page


        Spark.post(
                "/create-golfer",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = request.queryParams("login_name");
                    if(userName == null){
                        Spark.halt(403);
                    }

                    //String golfId = request.queryParams("id");
                    String golferName = request.queryParams("golfer_name");
                    String sponsorName = request.queryParams("sponsor_name");
                    try{
                        //int golfIdNum = Integer.valueOf(golfId);
                        User me = selectUser(conn, userName);
                        insertEntry(conn, me.id, golferName, sponsorName);
                    }
                    catch (Exception e){
                        System.out.println("An error occurred in Spark.post() /create-golfer");
                    }
                    //ArrayList<Golfer> tempList = golferArrayList;
                    /*Session session = request.session();
                    Golfer tempGolfer = new Golfer();
                    tempGolfer.id = golferArrayList.size() + 1;
                    tempGolfer.golferName = request.queryParams("golfer_name");
                    tempGolfer.sponsorName = request.queryParams("sponsor_name");
                    tempGolfer.userName = session.attribute("login_name");
                    tempGolfer.authorized = true;
                    //tempList.add(tempGolfer);
                    golferArrayList.add(tempGolfer);
                    response.redirect("/");
                    return "";
                    */
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() /create-golfer


        /*
        These next methods are commented out to help with assignment 5.2
        Will continue to incorporate these methods with more DataBase connectivity

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

        Spark.post(
                "/edit-golfer",
                ((request, response) -> {
                    String editId = request.queryParams("id");
                    try {
                        int editIdNum = Integer.valueOf(editId);
                        Golfer tempGolfer = golferArrayList.get(editIdNum - 1);
                        tempGolfer.golferName = request.queryParams("edit_golfer");
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post "/edit-golfer"

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() "/logout""

        Spark.get(
                "/edit-golfer",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");
                    m.put("id", id);
                    return new ModelAndView(m, "edit-golfer.html");
                }),
                new MustacheTemplateEngine()
        );//End of Spark.get() "/edit-golfer"

        Spark.get(
                "/delete-golfer",
                ((request, response) -> {
                    String idNum = request.queryParams("id");
                    try {
                        int id = Integer.valueOf(idNum);
                        golferArrayList.remove(id - 1);
                        for (int i = 0; i < golferArrayList.size(); i++) {
                            golferArrayList.get(i).id = i + 1;
                        }
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.get() "/delete-golfer

        */

    }//End of Main Method

}//End of Main Class
