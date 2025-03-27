import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
class postgresql {
    fun main() {
        val url = "jdbc:postgresql://181.188.156.195:18028/postgres"
        val user = "postgres"
        val password = "geDyJyOpDZ9qWmFtid0RgTOv"


        try {
            val connection: Connection = DriverManager.getConnection(url, user, password)
            println("Conexión exitosa a PostgreSQL!")

            val statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT * FROM tu_tabla")

            while (resultSet.next()) {
                println("Dato: ${resultSet.getString("tu_columna")}")
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}