package com.homemade.akhilez.syllabus.db

import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

/**
 * Created by Akhil on 8/6/2017.
 *
 */

open class RequestHandler {

    fun sendPostRequest(requestURL: String, postDataParams: HashMap<String, String>): String{
        var input = ""
        try{
            val con = setUpConnection(requestURL)
            writePostData(con, postDataParams)
            input = getInputStream(con)
        } catch(e: Exception){
            e.printStackTrace()
        }
        return input
    }

    fun sendGetRequest(requestURL: String, id: String = ""): String{
        val sb = StringBuilder()
        try{
            val conn = URL(requestURL+id).openConnection() as HttpURLConnection
            val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream) as Reader?)
            var line = bufferedReader.readLine()
            while(line != null){
                sb.append(line+"\n")
                line = bufferedReader.readLine()
            }
        }catch(e: Exception){}
        return sb.toString()
    }

    private fun getInputStream(conn: HttpURLConnection): String{
        val sb = StringBuilder()
        if (conn.responseCode == HttpsURLConnection.HTTP_OK) {
            val br = BufferedReader(InputStreamReader(conn.inputStream))
            var response = br.readLine()

            while (response != null) {
                sb.append(response)
                response = br.readLine()
            }
        }
        return sb.toString()
    }

    private fun writePostData(conn: HttpURLConnection,postDataParams: java.util.HashMap<String, String>){
        val out = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))

        writer.write(getPostDataString(postDataParams))

        writer.flush()
        writer.close()
        out.close()
    }

    private fun getPostDataString(params: java.util.HashMap<String, String>): String {
        val result = StringBuilder()
        var first = true

        for ((k, v) in params) {
            if (first) first = false else result.append("&")

            result.append(URLEncoder.encode(k, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(v, "UTF-8"))
        }

        return result.toString()
    }

    private fun setUpConnection(requestURL: String): HttpURLConnection {
        val conn = URL(requestURL).openConnection() as HttpURLConnection

        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = "POST"
        conn.doInput = true
        conn.doOutput = true

        return conn
    }

    protected fun jsonStringToMapList(jsonString: String, vararg keys: String): List<HashMap<String, String>>{
        val list = ArrayList<HashMap<String, String>>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jObj = jsonArray.getJSONObject(i)
                val map = HashMap<String, String>()
                for (j in 0 until keys.size)
                    map.put(keys[j], jObj.getString(keys[j]))
                list.add(map)
            }
        } catch (e: JSONException){
            e.printStackTrace()
        }
        return list
    }

}