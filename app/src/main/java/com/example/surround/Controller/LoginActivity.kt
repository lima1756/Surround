package com.example.surround.Controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.surround.Controller.Utils.ControllerSocket
import com.example.surround.R
import com.example.surround.Utils.Constants
import com.github.nkzawa.emitter.Emitter
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private var app = ControllerSocket.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (!app.socket.connected()) app.socket.connect()
        app.socket.on(Constants.SOCKET_ON_LOGIN_RESPONSE, socketOnLogin)


        btn_controller_create.setOnClickListener(View.OnClickListener {
            val name = et_controller_user.text.toString()
            if (name == "") {
                Toast.makeText(applicationContext, "Please insert your name", Toast.LENGTH_LONG)
            }
            else {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put(Constants.SOCKET_PARAM_NAME, name)
                    app.socket.emit(Constants.SOCKET_EMIT_LOGIN_CONTROLLER, jsonObject)
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, "Error while sending token and name.", Toast.LENGTH_LONG)
                }

            }
        })

    }

    private val socketOnLogin = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        try {
            if (app == null) app = ControllerSocket.getInstance()
            app.name = et_controller_user.text.toString()
            app.roomToken = data.getString(Constants.SOCKET_PARAM_ROOM)
            val intent = Intent(this, ControllerActivity::class.java)
            startActivity(intent)
        } catch (e: JSONException) {
            Log.d("LOGIN", "login failed ")
        }
    }
}
