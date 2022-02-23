package com.maslo.mlscanner

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.maslo.mlscanner.databinding.ActivityBreedBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BreedActivity : AppCompatActivity() {

    private val viewModel: BreedsViewModel by viewModels()
    private lateinit var binding: ActivityBreedBinding

    companion object {
        private const val REQUEST_CODE = 34
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.fab.setOnClickListener { view ->
            dispatchTakePictureIntent()
        }
        viewModel.predictionLiveData.observe(this) {
            it?.let {
                binding.resultTv.text = it.breed.first
                val isAlter = it.alterBreed != null
                binding.or.visibility = if (isAlter) View.VISIBLE else View.INVISIBLE
                binding.resultAltTv.visibility = if (isAlter) View.VISIBLE else View.INVISIBLE
                binding.resultAltTv.text = it.alterBreed?.first ?: ""

                Toast.makeText(
                    this,
                    "${it.breed.second}, ${it.alterBreed?.second?:0}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.setModel(readModelFromAsset(this.applicationContext))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.image.setImageBitmap(imageBitmap)
            viewModel.processBitmap(imageBitmap)
        }
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            //TODO: display error state to the user
        }
    }
}





