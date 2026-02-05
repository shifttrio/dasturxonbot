package org.mohirlar;

public final class DbConfig {
    // Masalan: jdbc:postgresql://localhost:5432/mydb
    public static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    public static final String JDBC_USER = "postgres";
    public static final String JDBC_PASS = "1";

    private DbConfig() {}
}
