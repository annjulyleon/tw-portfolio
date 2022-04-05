package ru.inforion.soapui

import groovy.sql.Sql

import java.sql.SQLException

class DBUtils {
    def context, log, runner

    /**
     * Simplify sql connection
     *
     * @param dbConString Connection string (jdbc:) or name of Test Case property where it stored
     * @param dbUser User for database or name of Test Case property where it stored
     * @param dbPassword Password for database or name of Test Case property where it stored
     * @param dbDriver Name of driver or name of Test Case property where it stored
     *
     * @return sql connection
     */

     def createDbConnection(String dbConString, String dbUser,
                           String dbPassword, String dbDriver) {

         try {
         if (dbConString.contains("jdbc:")) {
            def sql = Sql.newInstance(dbConString, dbUser, dbPassword, dbDriver)
            log.info ("Connection established with provided parameters")
            return sql

            } else {
            if (runner.getClass().getSimpleName() == "MockTestRunner") {
                dbConString = runner.testCase.getPropertyValue("$dbConString")
                dbUser = runner.testCase.getPropertyValue("$dbUser")
                dbPassword = runner.testCase.getPropertyValue("$dbPassword")
                dbDriver = runner.testCase.getPropertyValue("$dbDriver")
            } else {
                dbConString = runner.modelItem.testStep.testCase.getPropertyValue("$dbConString")
                dbUser = runner.modelItem.testStep.testCase.getPropertyValue("$dbUser")
                dbPassword = runner.modelItem.testStep.testCase.getPropertyValue("$dbPassword")
                dbDriver = runner.modelItem.testStep.testCase.getPropertyValue("$dbDriver")
            }

            def sql = Sql.newInstance(dbConString, dbUser, dbPassword, dbDriver)
            log.info ("Connection established with extracted from Test Case properties parameters")
            return sql
            }
         }
         catch (SQLException e) {
             log.error (e)
         }
     }
}
