package ziox.ramiro.saes.utils

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter


enum class BarcodeTypes{
    QRCode,
    Barcode,
    DataMatrix,
    PDF417,
    Barcode39,
    Barcode93,
    Aztec
}

@Composable
fun createBarcodeImage(message: String, type: BarcodeTypes = BarcodeTypes.QRCode, widthDp: Dp = Dp.Unspecified, heightDp: Dp = Dp.Unspecified): Bitmap {
    val width = with(LocalDensity.current){
        widthDp.toPx()
    }

    val height = with(LocalDensity.current){
        heightDp.toPx()
    }

    val bitMatrix = when (type) {
        BarcodeTypes.QRCode -> MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, width.toInt(), height.toInt())
        BarcodeTypes.Barcode -> MultiFormatWriter().encode(
            message,
            BarcodeFormat.CODE_128,
            width.toInt(),
            height.toInt()
        )
        BarcodeTypes.DataMatrix -> MultiFormatWriter().encode(message, BarcodeFormat.DATA_MATRIX, width.toInt(), width.toInt())
        BarcodeTypes.PDF417 -> MultiFormatWriter().encode(
            message,
            BarcodeFormat.PDF_417,
            width.toInt(),
            height.toInt()
        )
        BarcodeTypes.Barcode39 -> MultiFormatWriter().encode(
            message,
            BarcodeFormat.CODE_39,
            width.toInt(),
            height.toInt()
        )
        BarcodeTypes.Barcode93 -> MultiFormatWriter().encode(
            message,
            BarcodeFormat.CODE_93,
            width.toInt(),
            height.toInt()
        )
        BarcodeTypes.Aztec -> MultiFormatWriter().encode(message, BarcodeFormat.AZTEC, width.toInt(), width.toInt())
    }

    val bitmapWidth = bitMatrix.width
    val bitmapHeight = bitMatrix.height

    val pixels = IntArray(bitmapWidth * bitmapHeight)
    for (i in 0 until bitmapHeight) {
        for (j in 0 until bitmapWidth) {
            if (bitMatrix[j, i]) {
                pixels[i * bitmapWidth + j] = -0x1000000
            } else {
                pixels[i * bitmapWidth + j] = -0x1
            }
        }
    }
    val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)
    return bitmap
}