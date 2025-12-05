module com.example.opp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.opp to javafx.fxml;
    opens com.example.opp.controller to javafx.fxml;
    opens com.example.opp.model to javafx.fxml;

    exports com.example.opp;
    exports com.example.opp.controller;
    exports com.example.opp.service;
    exports com.example.opp.view;
    exports com.example.opp.util;
    exports com.example.opp.config;
    exports com.example.opp.database;
    exports com.example.opp.repository;
    exports com.example.opp.model;
}