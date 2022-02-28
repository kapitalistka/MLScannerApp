package com.maslo.mlscanner

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.maslo.mlscanner.databinding.ActivityBreedBinding


class BreedActivity : AppCompatActivity() {

    private val viewModel: BreedsViewModel by viewModels()
    private lateinit var binding: ActivityBreedBinding

    companion object {
        private const val PHOTO_REQUEST_CODE = 34
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val activityLauncher = registerForActivityResult(BreedActivityContract()) { resultBitmap ->
            resultBitmap?.let { processNewImage(resultBitmap) }
        }

        binding.fab.setOnClickListener { activityLauncher.launch(PHOTO_REQUEST_CODE) }

        viewModel.predictionLiveData.observe(this) {
            it?.let {
                binding.resultTv.text = it.breed.first
                val isAlter = it.alterBreed != null
                binding.or.visibility = if (isAlter) View.VISIBLE else View.INVISIBLE
                binding.resultAltTv.visibility = if (isAlter) View.VISIBLE else View.INVISIBLE
                binding.resultAltTv.text = it.alterBreed?.first ?: ""
                binding.ambiguousTv.visibility =
                    if (it.isAmbiguity) View.VISIBLE else View.INVISIBLE

                Toast.makeText(
                    this,
                    "${it.breed.second}, ${it.alterBreed?.second ?: 0}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.setModel(readModelFromAsset(this.applicationContext))
    }

    private fun processNewImage(imageBitmap: Bitmap) {
        binding.image.setImageBitmap(imageBitmap)
        viewModel.processBitmap(imageBitmap)
    }

}

class BreedActivityContract : ActivityResultContract<Int, Bitmap?>() {
    override fun createIntent(context: Context, input: Int): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        return if (resultCode == AppCompatActivity.RESULT_OK) {
            intent?.extras?.get("data") as Bitmap?
        } else null
    }
}





