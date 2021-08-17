package com.mobdeve.s15.group8.mobdeve_mp.controller.activities

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.mobdeve.s15.group8.mobdeve_mp.F
import com.mobdeve.s15.group8.mobdeve_mp.R
import com.mobdeve.s15.group8.mobdeve_mp.controller.interfaces.ImageUploadCallback
import com.mobdeve.s15.group8.mobdeve_mp.controller.adapters.AddPlantTasksAdapter
import com.mobdeve.s15.group8.mobdeve_mp.model.dataobjects.Task
import com.mobdeve.s15.group8.mobdeve_mp.model.repositories.NewPlantInstance
import com.mobdeve.s15.group8.mobdeve_mp.model.services.DBService
import com.mobdeve.s15.group8.mobdeve_mp.model.services.ImageUploadService
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPlantActivity : AppCompatActivity(), ImageUploadCallback {
    private lateinit var tasksRV: RecyclerView
    private lateinit var ivPlant: ImageView
    private lateinit var btnAddTask: Button
    private lateinit var btnSave: Button
    private lateinit var btnAddPhoto: Button
    private lateinit var etPlantName: EditText
    private lateinit var etPlantNickname: EditText
    private lateinit var ibtnDelete: Button
    private lateinit var mPhotoFilename: String
    private val mTasks = NewPlantInstance.plant["tasks"] as ArrayList<Task>
    private val mPlantId = UUID.randomUUID().toString()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)
        ImageUploadService.setOnUploadSuccessListener(this)

        ivPlant = findViewById(R.id.img_plant)
        etPlantName = findViewById(R.id.et_plant_name)
        etPlantNickname = findViewById(R.id.et_plant_nickname)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        btnAddTask = findViewById(R.id.btn_add_task)
        btnSave = findViewById(R.id.btn_save_plant)
        tasksRV = findViewById(R.id.rv_tasks)
        tasksRV.adapter = AddPlantTasksAdapter(mTasks)
        tasksRV.layoutManager = LinearLayoutManager(this)

        btnAddPhoto.setOnClickListener { mOpenCamera() }
        btnSave.setOnClickListener { mSavePlant() }
        btnAddTask.setOnClickListener {
            val fragment = AddTaskDialogFragment()
            fragment.show(supportFragmentManager, "add_task")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(getString(R.string.SAVED_PLANT_KEY), NewPlantInstance.plant)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val plant = savedInstanceState.getSerializable(getString(R.string.SAVED_PLANT_KEY))
        Log.d("HATDOG", plant.toString())
    }

    override fun onCloudinaryUploadSuccess(imageUrl: String) {
        DBService.updateDocument(
            collection= F.plantsCollection,
            id=mPlantId,
            field="imageUrl",
            value=imageUrl)
    }

    private fun mSavePlant() {
        // Compile final map to write to firebase
        NewPlantInstance.setPlantName(etPlantName.text.toString())
        NewPlantInstance.setPlantNickname(etPlantNickname.text.toString())

        // Write plant to firebase first
        DBService.addDocument(
            collection=F.plantsCollection,
            id=mPlantId,
            data=NewPlantInstance.plant)
        DBService.updateDocument(
            collection=F.usersCollection,
            id=F.auth.currentUser?.uid,
            field="plants",
            value=FieldValue.arrayUnion(mPlantId))

        // Then upload to cloudinary and reset the new plant instance
        ImageUploadService.uploadToCloud(mPhotoFilename)
        NewPlantInstance.resetPlant()
    }

    private val cameraLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = Uri.fromFile(File(mPhotoFilename))
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                ivPlant.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mOpenCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    mCreateImageFile()
                } catch (ex: IOException) {
                    Log.e("CAM", "$ex")
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        getString(R.string.file_provider_authority),
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun mCreateImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val filename = "plant_${timeStamp}"
        return File.createTempFile(filename, ".jpg", storageDir).apply {
            mPhotoFilename = absolutePath
        }
    }
}