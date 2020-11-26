package com.jsyncpj.floatingview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jsyncpj.floating.FloatingCtrl
import com.jsyncpj.floating.floatingview.AdsorptionFloatingView
import com.jsyncpj.floating.model.FloatingIntent
import com.jsyncpj.floatingview.entity.MessageBean
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClick()
    }

    private fun initClick(){
        bt_show_once.setOnClickListener {
            FloatingCtrl.setSystemFloat(false)
            FloatingCtrl.install(application)
            FloatingCtrl.showOnce(this,AdsorptionFloatingView::class.java)
        }
        bt_show_always.setOnClickListener {
            FloatingCtrl.setSystemFloat(false)
            FloatingCtrl.install(application)
            FloatingCtrl.showAlways(this,AdsorptionFloatingView::class.java)
        }
        bt_show_system.setOnClickListener {
            FloatingCtrl.setSystemFloat(true)
            FloatingCtrl.install(application)
            FloatingCtrl.showAlways(this,AdsorptionFloatingView::class.java)
        }
        bt_next_act.setOnClickListener {
            startActivity(Intent(this,NextActivity::class.java))
        }
        bt_message.setOnClickListener {
            FloatingCtrl.setSystemFloat(false)
            FloatingCtrl.install(application)
            val intent = FloatingIntent(MessageFloatingView::class.java)
            intent.mode = FloatingIntent.MODE_ONCE
            intent.bundle = Bundle().apply { putSerializable("MessageFloatingView",MessageBean()) }
            FloatingCtrl.show(this,intent)
        }
    }
}