package com.example.cr3renamer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import com.example.cr3renamer.databinding.ActivityMainBinding
import android.widget.ProgressBar

import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var uriTree: Uri
    private lateinit var binding: ActivityMainBinding
    private lateinit var textInfo: TextView
    private var openDocumentTreeRequestCode = 2
    private lateinit var processingBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        textInfo = binding.contentmain.info
        binding.contentmain.opendocument.setOnClickListener {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, openDocumentTreeRequestCode)
        }

        binding.contentmain.beginrename.setOnClickListener { view ->
            if (!this::uriTree.isInitialized || uriTree == null)
            {
                Snackbar.make(view, "No directory selected", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var files = DocumentFile.fromTreeUri(this, uriTree)?.listFiles()
            var renameJob = GlobalScope.launch() {
                Thread.sleep(10_000)

                var totalRenamed = 0
                files?.forEach { file ->
                    var fileName = file.name
                    var fileNameWOExtension = fileName?.substringBeforeLast(".", "")
                    var extension = fileName?.substringAfterLast(".", "")
                    if (file.isFile) {
                        if (extension.equals("CR3")) {
                            file.renameTo("$fileNameWOExtension.CR2")
                            totalRenamed++
                        }
                        else if (extension.equals("cr3")) {
                            file.renameTo("$fileNameWOExtension.cr2")
                            totalRenamed++
                        }
                    }
                }

                runOnUiThread {
                    processingBar.dismiss()
                    Snackbar.make(view, "Renamed $totalRenamed file(s)", Snackbar.LENGTH_LONG).show()
                }
            }

            processingBar =
                Snackbar.make(view, "Renaming files", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel") {
                        renameJob?.cancel()
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
            val contentLay =
                processingBar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
            val item = ProgressBar(this)
            contentLay.addView(item, 0)
            processingBar.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == openDocumentTreeRequestCode)
        {
            textInfo.text = ""
            uriTree = data?.data!!
            var directory = data?.data?.path?.split(":")?.get(1)
            textInfo.append(directory)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}