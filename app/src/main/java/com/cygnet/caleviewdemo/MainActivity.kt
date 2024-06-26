package com.cygnet.caleviewdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cygnet.caleviewdemo.databinding.ActivityMainBinding
import com.cygnet.caleviewdemo.view.colorful.ColorfulActivity
import com.cygnet.caleviewdemo.view.custom.CustomActivity
import com.cygnet.caleviewdemo.view.full.FullActivity
import com.cygnet.caleviewdemo.view.index.IndexActivity
import com.cygnet.caleviewdemo.view.meizu.MeizuActivity
import com.cygnet.caleviewdemo.view.mix.MixActivity
import com.cygnet.caleviewdemo.view.multi.MultiActivity
import com.cygnet.caleviewdemo.view.progress.ProgressActivity
import com.cygnet.caleviewdemo.view.range.RangeActivity
import com.cygnet.caleviewdemo.view.simple.SimpleActivity
import com.cygnet.caleviewdemo.view.single.SingleActivity
import com.cygnet.caleviewdemo.view.solar.SolarActivity
import com.cygnet.caleviewdemo.view.pager.ViewPagerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var bindMain: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindMain.root)

        bindMain.full.setOnClickListener {
            startActivity(Intent(this, FullActivity::class.java))
        }

        bindMain.simple.setOnClickListener {
            startActivity(Intent(this, SimpleActivity::class.java))
        }

        bindMain.custom.setOnClickListener {
            startActivity(Intent(this, CustomActivity::class.java))
        }

        bindMain.color.setOnClickListener {
            startActivity(Intent(this, ColorfulActivity::class.java))
        }

        bindMain.index.setOnClickListener {
            startActivity(Intent(this, IndexActivity::class.java))
        }

        bindMain.meizu.setOnClickListener {
            startActivity(Intent(this, MeizuActivity::class.java))
        }

        bindMain.mix.setOnClickListener {
            startActivity(Intent(this, MixActivity::class.java))
        }

        bindMain.multi.setOnClickListener {
            startActivity(Intent(this, MultiActivity::class.java))
        }

        bindMain.progress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }

        bindMain.range.setOnClickListener {
            startActivity(Intent(this, RangeActivity::class.java))
        }

        bindMain.single.setOnClickListener {
            startActivity(Intent(this, SingleActivity::class.java))
        }

        bindMain.solar.setOnClickListener {
            startActivity(Intent(this, SolarActivity::class.java))
        }

        bindMain.page.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }
    }

}
