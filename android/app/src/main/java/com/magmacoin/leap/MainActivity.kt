package com.magmacoin.leap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.magmacoin.leap.core.di.ServiceLocator
import com.magmacoin.leap.core.navigation.Navigator
import com.magmacoin.leap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        val inflated = ActivityMainBinding.inflate(layoutInflater)
        binding = inflated
        setContentView(inflated.root)
        if (savedInstanceState == null) {
            Navigator.showSplash(supportFragmentManager)
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
