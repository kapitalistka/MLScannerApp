package com.maslo.mlscanner

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.pytorch.*
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


private const val TAG = "BreedsViewModel"

class BreedsViewModel : ViewModel() {

    val predictionLiveData = MutableLiveData<Prediction>()

    private var model: Module? = null

    fun setModel(model: Module?) {
        this.model = model
    }

    fun processBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {

            val scaledBitmap = bitmap
                .run { createSquaredBitmap() }
                ?.run { createScaledBitmap(256, 256, false) }
                ?.run { cropCenter(224) }

            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                scaledBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                MemoryFormat.CHANNELS_LAST
            )

            val outputTensor: Tensor? = model?.forward(IValue.from(inputTensor))?.toTensor()

            val scores = outputTensor?.dataAsFloatArray?.let { softMax(it) }

            if (scores != null) {

                val pairs: MutableList<Pair<Int, Float>> = mutableListOf()
                scores.forEachIndexed { i, v -> pairs.add(Pair(i, v)) }
                val sortedPairs = pairs.sortedWith(compareBy { it.second }).asReversed()

                Log.d(TAG, "processBitmap: max predicted index ${sortedPairs[0].first}")
                val score1 = sortedPairs[0].second
                val score2 = sortedPairs[1].second
                val q = score2 / score1 * 100
                Log.d(TAG, "processBitmap: diff between 1st and 2nd: $q%. ($score1 , ${score2})")

                val breed = Pair(Breeds.BREEDS[sortedPairs[0].first], sortedPairs[0].second)
                val alterBreed =
                    if (q > PERCENT_FOR_ALTER_RESULT) Pair(
                        Breeds.BREEDS[sortedPairs[1].first],
                        sortedPairs[1].second
                    )
                    else null

                val isAmbiguity = score1 < QUOTA_FOR_AMBIGOUOS

                val prediction = Prediction(breed, alterBreed, isAmbiguity)
                predictionLiveData.postValue(prediction)

            }
        }
    }
}

class Prediction(
    val breed: Pair<String, Float>,
    val alterBreed: Pair<String, Float>? = null,
    val isAmbiguity:Boolean = false
)

fun readModelFromAsset(context: Context): Module? {
    return try {
        LiteModuleLoader.load(assetFilePath(context, MODEL_ASSET_FILE))
    } catch (e: IOException) {
        Log.e(TAG, "Error reading assets", e)
        null
    }
}

@Throws(IOException::class)
private fun assetFilePath(context: Context, assetName: String): String? {
    val file = File(context.filesDir, assetName)
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    context.assets.open(assetName).use { inputStream ->
        FileOutputStream(file).use { os ->
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                os.write(buffer, 0, read)
            }
            os.flush()
        }
        return file.absolutePath
    }
}
