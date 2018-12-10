package id.yuana.speech2text.android.data.remote

import id.yuana.speech2text.android.BuildConfig
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


/**
 * @author Yuana andhikayuana@gmail.com
 * @since Dec, Thu 06 2018 17.05
 **/
object CarikApi {

    fun request(param: String): CarikResponse {

        val client = URL(BuildConfig.CARIK_API)
        val con = client.openConnection() as HttpURLConnection

        con.apply {
            doOutput = true
            requestMethod = "POST"
        }.connect()

        val osw = OutputStreamWriter(con.outputStream, "UTF-8")
        osw.write(
            "{\n" +
                    "\t\"message\":{\n" +
                    "\t\t\"message_id\":0,\n" +
                    "\t\t\"chat\":{\n" +
                    "\t\t\t\"id\":0\n" +
                    "\t\t},\n" +
                    "\t\t\"text\":\"$param\"\n" +
                    "\t}\n" +
                    "\t\n" +
                    "}\n"
        )
        osw.flush()
        osw.close()


        val response = con.inputStream.bufferedReader().use { it.readText() }

        return CarikResponse(
            con.responseMessage,
            con.responseCode,
            JSONObject(response)
        )

    }

    data class CarikResponse(
        val msg: String,
        val code: Int,
        val body: JSONObject
    )

}