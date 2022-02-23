package com.maslo.mlscanner

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    //TODO: do it in worker thread
    fun processBitmap(bitmap: Bitmap) {

        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, 256, 256, false).apply { cropCenter(224) };
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            scaledBitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
            MemoryFormat.CHANNELS_LAST
        )

        val outputTensor: Tensor? = model?.forward(IValue.from(inputTensor))?.toTensor()
        val scores = outputTensor?.dataAsFloatArray

        if (scores != null) {
            // searching for the index with maximum score
            /*var maxScore = -Float.MAX_VALUE
            var maxScoreIdx = -1
            scores.indices.forEach { i ->
                if (scores[i] > maxScore) {
                    maxScore = scores[i]
                    maxScoreIdx = i
                }
            }*/

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
                if (q > 80) Pair(Breeds.BREEDS[sortedPairs[1].first], sortedPairs[1].second)
                else null

            val prediction = Prediction(breed, alterBreed)
            predictionLiveData.value = prediction


        }
    }
}

class Prediction(
    val breed: Pair<String, Float>,
    val alterBreed: Pair<String, Float>? = null
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
fun assetFilePath(context: Context, assetName: String): String? {
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
