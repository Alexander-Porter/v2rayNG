package com.voxon.sim.ui

import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.voxon.sim.AppConfig.ANG_PACKAGE
import com.voxon.sim.R
import com.voxon.sim.databinding.ActivityLogcatBinding
import com.voxon.sim.extension.toast
import com.voxon.sim.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.io.IOException
import java.util.LinkedHashSet

class LogcatActivity : BaseActivity() {
    private lateinit var binding: ActivityLogcatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogcatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        title = getString(R.string.title_logcat)

        logcat(false)
    }

    private fun logcat(shouldFlushLog: Boolean) {

        try {
            binding.pbWaiting.visibility = View.VISIBLE

            lifecycleScope.launch(Dispatchers.Default) {
                if (shouldFlushLog) {
                    val lst = LinkedHashSet<String>()
                    lst.add("logcat")
                    lst.add("-c")
                    val process = Runtime.getRuntime().exec(lst.toTypedArray())
                    process.waitFor()
                }
                val lst = LinkedHashSet<String>()
                lst.add("logcat")
                lst.add("-d")
                lst.add("-v")
                lst.add("time")
                lst.add("-s")
                lst.add("GoLog,tun2socks,${ANG_PACKAGE},AndroidRuntime,System.err")
                val process = Runtime.getRuntime().exec(lst.toTypedArray())
//                val bufferedReader = BufferedReader(
//                        InputStreamReader(process.inputStream))
//                val allText = bufferedReader.use(BufferedReader::readText)
                val allText = process.inputStream.bufferedReader().use { it.readText() }
                launch(Dispatchers.Main) {
                    binding.tvLogcat.text = allText
                    binding.tvLogcat.movementMethod = ScrollingMovementMethod()
                    binding.pbWaiting.visibility = View.GONE
                    Handler(Looper.getMainLooper()).post { binding.svLogcat.fullScroll(View.FOCUS_DOWN) }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_logcat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.copy_all -> {
            Utils.setClipboard(this, binding.tvLogcat.text.toString())
            toast(R.string.toast_success)
            true
        }
        R.id.clear_all -> {
            logcat(true)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
