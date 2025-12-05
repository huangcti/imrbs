import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SetupDatabase {
    public static void main(String[] args) {
        String adminUrl = "jdbc:postgresql://localhost:5432/postgres";
        String adminUser = "postgres";
        String adminPass = args.length > 0 ? args[0] : "postgres";
        
        try (Connection conn = DriverManager.getConnection(adminUrl, adminUser, adminPass)) {
            Statement stmt = conn.createStatement();
            
            // 終止所有連接到 imrbs 資料庫的連線
            System.out.println("終止現有連線...");
            stmt.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity " +
                        "WHERE datname = 'imrbs' AND pid <> pg_backend_pid()");
            
            // 檢查用戶是否存在
            System.out.println("檢查用戶...");
            var rs = stmt.executeQuery("SELECT 1 FROM pg_roles WHERE rolname='imrbs_user'");
            boolean userExists = rs.next();
            rs.close();
            
            // 刪除資料庫
            System.out.println("刪除舊資料庫...");
            stmt.execute("DROP DATABASE IF EXISTS imrbs");
            
            // 刪除用戶
            if (userExists) {
                System.out.println("刪除舊用戶...");
                stmt.execute("DROP USER IF EXISTS imrbs_user");
            }
            
            // 建立用戶
            System.out.println("建立用戶 imrbs_user...");
            stmt.execute("CREATE USER imrbs_user WITH PASSWORD 'imrbs_pass'");
            
            // 建立資料庫
            System.out.println("建立資料庫 imrbs...");
            stmt.execute("CREATE DATABASE imrbs OWNER imrbs_user ENCODING 'UTF8'");
            
            // 授予權限
            System.out.println("授予權限...");
            stmt.execute("GRANT ALL PRIVILEGES ON DATABASE imrbs TO imrbs_user");
            
            System.out.println("\n✓ 資料庫設定完成!");
            System.out.println("  資料庫名稱: imrbs");
            System.out.println("  用戶名稱: imrbs_user");
            System.out.println("  密碼: imrbs_pass");
            System.out.println("  連線 URL: jdbc:postgresql://localhost:5432/imrbs");
            
        } catch (Exception e) {
            System.err.println("錯誤: " + e.getMessage());
            System.err.println("\n使用方式: java SetupDatabase [postgres密碼]");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
