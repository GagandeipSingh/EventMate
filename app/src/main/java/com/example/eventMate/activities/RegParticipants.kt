package com.example.eventMate.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventMate.R
import com.example.eventMate.adapters.RegistrantsAdapter
import com.example.eventMate.databinding.ActivityRegParticipantsBinding
import com.example.eventMate.databinding.SortOutBinding
import com.example.eventMate.domains.EventDomain
import com.example.eventMate.domains.RegDetailsDomain
import com.example.eventMate.progress.BaseActivity
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.gson.Gson
import java.io.OutputStream


class RegParticipants : BaseActivity() {
    private lateinit var binding: ActivityRegParticipantsBinding
    private var position = 0
    private lateinit var list: ArrayList<RegDetailsDomain>
    private var tempList : ArrayList<RegDetailsDomain> = arrayListOf()
    private lateinit var adapter: RegistrantsAdapter
    private lateinit var eventObj: EventDomain
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var csvString : String
    private var departSet : HashSet<String> = HashSet()
    private var classSet : HashSet<String> = HashSet()
    private var departList : ArrayList<String> = arrayListOf()
    private var classList : ArrayList<String> = arrayListOf()
    private var selectedDepart : Int = 0
    private var selectedClass : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegParticipantsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left,0, systemBars.right, systemBars.bottom)
            insets
        }
        val objString = intent.getStringExtra("objString")
        position = intent.getIntExtra("position", 0)
        val gson = Gson()
        eventObj = gson.fromJson(objString, EventDomain::class.java)
        position = intent.getIntExtra("position", 0)

        val prefix = R.drawable.img_1
        binding.image.setImageResource(prefix + position % 7)
        binding.eventTitle.text = eventObj.name
        binding.creator.text = eventObj.creator
        list = arrayListOf()
        adapter = RegistrantsAdapter(list, this)
        binding.participants.adapter = adapter
        binding.participants.layoutManager = LinearLayoutManager(this)
        retrieveReg()
        binding.options.setOnClickListener {
            showMenu(it)
        }
        binding.sortOut.setOnClickListener {
            val dialog = Dialog(this)
            val dialogBinding = SortOutBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.filterDepart.setOnClickListener {
                filterDepart()
                dialog.dismiss()
            }
            dialogBinding.filterClass.setOnClickListener {
                filterClass()
                dialog.dismiss()
            }
            dialog.show()
            // Set dialog window attributes for full width and bottom positioning
            val window = dialog.window
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.attributes?.gravity = Gravity.BOTTOM

            // Set background drawable for customization
            window?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.dialog_bg_reg))
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                try {
                    val contentResolver: ContentResolver = contentResolver
                    val outputStream: OutputStream = contentResolver.openOutputStream(uri)!!
                    outputStream.write(csvString.toByteArray())
                    outputStream.close()
                    Toast.makeText(this@RegParticipants, "SpreadSheet is Exported..", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("CSV Export", "Error saving CSV file", e)
                }
            }
        }
    }

    private fun filterDepart() {
        val builder = AlertDialog.Builder(this)
        builder.context.setTheme(R.style.AlertDialogTheme)
        builder.setTitle("Choose Department")
        builder.setPositiveButton("Done") { dialog, _ ->
            if(selectedDepart != 0){
                tempList = list.filter { it.department.uppercase() == departList[selectedDepart]} as ArrayList<RegDetailsDomain>
                val tempAdapter = RegistrantsAdapter(tempList, this)
                binding.participants.adapter = tempAdapter
            }
            else{
                val tempAdapter = RegistrantsAdapter(list, this)
                binding.participants.adapter = tempAdapter
            }
            Toast.makeText(this,"Selected : ${departList[selectedDepart]}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") {dialog,_ ->
            dialog.dismiss()
        }
            .setCancelable(false)
            .setSingleChoiceItems(departList.toTypedArray<String>(), selectedDepart) { _, which ->
                selectedDepart = which
            }
            .show()
    }

    private fun filterClass() {
        val builder = AlertDialog.Builder(this)
        builder.context.setTheme(R.style.AlertDialogTheme)
        builder.setTitle("Choose Class")
        builder.setPositiveButton("Done") { dialog, _ ->
            if(selectedClass != 0){
                tempList = list.filter { it.classValue.uppercase() == classList[selectedClass] } as ArrayList<RegDetailsDomain>
                val tempAdapter = RegistrantsAdapter(tempList, this)
                binding.participants.adapter = tempAdapter
            }
            else{
                val tempAdapter = RegistrantsAdapter(list, this)
                binding.participants.adapter = tempAdapter
            }
            Toast.makeText(this,"Selected : ${classList[selectedClass]}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") {dialog,_ ->
            dialog.dismiss()
        }
            .setCancelable(false)
            .setSingleChoiceItems(classList.toTypedArray<String>(), selectedClass) { _, which ->
                selectedClass = which
            }
            .show()
    }

    private fun showMenu(view: View) {
        val optionsMenu = PopupMenu(this, view, Gravity.END)
        optionsMenu.inflate(R.menu.options_reg)
        optionsMenu.show()
        optionsMenu.menu.findItem(R.id.sort_Asc).setOnMenuItemClickListener {
            val tempList = adapter.getList()
            tempList.sortWith(ascTimeComparator)
            val tempAdapter = RegistrantsAdapter(tempList, this)
            binding.participants.adapter = tempAdapter
            true
        }
        optionsMenu.menu.findItem(R.id.sort_Desc).setOnMenuItemClickListener {
            val tempList = adapter.getList()
            tempList.sortWith(descTimeComparator)
            val tempAdapter = RegistrantsAdapter(tempList, this)
            binding.participants.adapter = tempAdapter
            true
        }
        optionsMenu.menu.findItem(R.id.export_Excel).setOnMenuItemClickListener {
            exportSpreadSheet()
            true
        }
    }

    @SuppressLint("InlinedApi")
    private fun exportSpreadSheet() {
        val arrayList = adapter.getList()
        csvString = arrayList.toCSV()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/csv"
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
            Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()))
        launcher.launch(intent)
    }

    private val ascTimeComparator = Comparator<RegDetailsDomain> { event1, event2 ->
        event2.regDate.compareTo(event1.regDate)
    }
    private val descTimeComparator = Comparator<RegDetailsDomain> { event1, event2 ->
        event1.regDate.compareTo(event2.regDate)
    }

    private fun retrieveReg() {
        showProgressBar()
        val regRef = Firebase.database.getReference("Events").child(eventObj.key).child("Registrations")
        regRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (regId in snapshot.children) {
                    val regValue = regId.getValue(RegDetailsDomain::class.java)
                    list.add(regValue!!)
                    departSet.add(regValue.department.uppercase())
                    classSet.add(regValue.classValue.uppercase())
                }
                departList = ArrayList(departSet)
                departList.add(0,"All Departments")
                classList = ArrayList(classSet)
                classList.add(0,"All Classes")
                adapter.notifyDataSetChanged()
                refreshEvents()
                dismissProgessBar()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun refreshEvents() {
        if (adapter.itemCount > 0) {
            binding.emptyImg.visibility = View.GONE
            binding.emptyLine.visibility = View.GONE
        } else {
            binding.emptyImg.visibility = View.VISIBLE
            binding.emptyLine.visibility = View.VISIBLE
        }
    }

    private fun ArrayList<RegDetailsDomain>.toCSV(): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Name,Class,Department,Email\n")
        forEach { item ->
            val dataLine = "${item.studentId},${item.name},${item.classValue},${item.department},${item.email}\n"
            csvBuilder.append(dataLine)
        }
        return csvBuilder.toString()
    }
}