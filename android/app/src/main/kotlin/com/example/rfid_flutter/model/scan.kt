import org.json.JSONObject

class Scan(val scanData: String?, val symbology: String?, val dateTime: String) {
    fun toJson(): String {
        val jsonObject = JSONObject()
        jsonObject.put("scanData", scanData)
        jsonObject.put("symbology", symbology)
        jsonObject.put("dateTime", dateTime)
        return jsonObject.toString()
    }
}
