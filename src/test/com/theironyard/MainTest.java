package com.theironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by earlbozarth on 11/3/15.
 */
public class MainTest {

    public Connection startConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:./test");
        Main.createTables(connection);
        return connection;
    }//End of startConnection

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE users");
        stmt.execute("DROP TABLE golfers");
        conn.close();
    }//End of endConnection


    @Test
    public void testUser() throws SQLException {
        Connection connection = startConnection();
        Main.insertUser(connection, "Test User Name Here", "");
        User user = Main.selectUser(connection, "Test User Name Here");
        endConnection(connection);

        assertTrue(user != null);
    }//End of testUser Method


    @Test
    public void testEntry() throws SQLException{
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertEntry(conn, 1, "Tiger Woods", "Nike");
        Golfer golfer = Main.selectEntry(conn, 1);
        endConnection(conn);

        assertTrue(golfer != null);
    }//End of testEntry

    @Test
    public void testEntries() throws SQLException{
        Connection conn = startConnection();
        Main.insertUser(conn, "Duke", "");
        Main.insertEntry(conn, 1, "Jordan Speith", "Titleist");
        ArrayList<Golfer> golferArrayList = Main.selectEntries(conn, 1);
        endConnection(conn);

        assertTrue(golferArrayList != null);
    }






}//End of MainTest Class