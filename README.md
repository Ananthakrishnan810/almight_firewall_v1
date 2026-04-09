
Firewall
--------
A firewall is a critical network security system that monitors and controls incoming and outgoing network traffic based on predefined security rules.

Programming languages and framework used in FireWall-Engine
-----------------------------------------------------------
Java is a widely used, high-level, object-oriented programming language and software platform known for its guiding principle: "Write Once, Run Anywhere" (WORA)
Spring-boot is an open-source Java framework that simplifies the process of building and deploying production-ready applications.
Python is a popular, high-level, general-purpose programming language known for its simple, readable syntax that emphasizes code clarity through the use of significant indentation.

Project folder structure
------------------------
src/main/java: This directory contains all the main Java source code for your application

    controller: Handles incoming HTTP requests and exposes API endpoints.
    service: Contains business logic and orchestrates operations.
    dao: Manages data access and interacts with the database.
    entity: Defines data models and database entities.
    config: Stores application-wide configuration classes
    
src/main/resources: This folder is for configuration files and static assets.

    application.properties: The main configuration file for the application, used for settings like server port or database connections.
    static: Contains static resources like CSS, JavaScript files, and images.
    templates: Stores server-side HTML templates if a template engine like Thymeleaf is used

pom.xml (Maven) or build.gradle (Gradle): The build configuration file that manages project dependencies and plugins.


How to Run the Project
----------------------
To Run the Spring-boot:mvn clean compile spring-boot:run;
To Run the python:python3 -m uvicorn main:app --reload --port 5000(port is changable)

Java and python versions required
---------------------------------

Use java 21 + and python 3.10 +
