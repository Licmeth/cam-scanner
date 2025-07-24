package org.agera.camscanner

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap

class CameraActivity : ComponentActivity() {

    companion object {
        const val DIM_LIMIT = 1080 // Maximum dimension for image processing
        const val MORPH_KERNEL_SIZE = 10.0 // Size of the morphological kernel
        const val MORPH_ITERATIONS = 3 // Number of iterations for morphological operations
        const val GRAB_CUT_ITERATIONS = 5 // Number of iterations for GrabCut algorithm
        const val GRAB_CUT_RECT_X_SIZE = 200 // X size of the rectangle for GrabCut
        const val GRAB_CUT_RECT_Y_SIZE = 200 // Y size of the rectangle for GrabCut
        const val ALLOW_IMAGE_ROTATION = true // Allow image rotation
    }

    private lateinit var previewView: PreviewView
    private lateinit var backButton: ImageButton
    private lateinit var toggleCameraButton: ImageButton
    private lateinit var overlayImageView: AppCompatImageView
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageProcessor: ImageProcessor? = null

    private val requestPermissionLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, start the camera
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan a document", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_camera)

        // Initialize views
        previewView = findViewById<PreviewView>(R.id.previewView)
        overlayImageView = findViewById<AppCompatImageView>(R.id.overlayImageView)
        backButton = findViewById<ImageButton>(R.id.backButton)
        toggleCameraButton = findViewById<ImageButton>(R.id.toggleCameraButton)

        // Load OpenCV
        if (!OpenCVLoader.initLocal()) {
            Toast.makeText(this, "OpenCV failed to load", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Add callbacks
        backButton.setOnClickListener {
            finish()
        }

        toggleCameraButton.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }


        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        } else {
            // Permission is already granted, start the camera
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        // Disable the camera view to stop processing frames
        imageProcessor = null
    }

    override fun onResume() {
        super.onResume()
        // Enable the camera view to start processing frames
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources if needed
        imageProcessor = null
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                // Check if the imageProcessor is initialized
                if (imageProcessor == null) {
                    imageProcessor = ImageProcessor(imageProxy.width, imageProxy.height)
                }
                // Convert imageProxy to OpenCV Mat, process, and display result
                val mat = imageProcessor!!.imageProxyToGreyscaleMat(imageProxy)
                // val processed = processCameraImage(mat)
                showOverlay(imageProcessor!!.matToBitmap(mat))
                imageProxy.close()
            }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, this.cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun showOverlay(bitmap: Bitmap) {
        runOnUiThread {
            overlayImageView.setImageBitmap(bitmap)
            overlayImageView.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * Processes the camera image to detect the document and apply an overlay with it's borders.
     * The process is taken from https://learnopencv.com/automatic-document-scanner-using-opencv/
     */
    private fun processCameraImage(image: Mat): Mat {
        val img = prepareImageForProcessing(image)

        // Morphological closing
        //removeDocumentContent(img)

        // GrabCut
        //val imgMasked = removeBackground(img)

        return img // Return the masked image for now

        /*
        // Grayscale and blur
        val gray = Mat()
        Imgproc.cvtColor(imgMasked, gray, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(11.0, 11.0), 0.0)

        // Edge detection
        val canny = Mat()
        Imgproc.Canny(gray, canny, 0.0, 200.0)
        val dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0))
        Imgproc.dilate(canny, canny, dilateKernel)

        // Find contours
        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(canny, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)
        if (contours.isEmpty()) return origImg

        // Keep largest 5 contours
        contours.sortByDescending { Imgproc.contourArea(it) }
        val page = contours.take(5)

        var corners: MatOfPoint2f? = null
        for (c in page) {
            val peri = Imgproc.arcLength(MatOfPoint2f(*c.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*c.toArray()), approx, 0.02 * peri, true)
            if (approx.total() == 4L) {
                corners = approx
                break
            }
        }
        if (corners == null) return origImg

        val sortedCorners = orderPoints(corners)
        val destinationCorners = findDest(sortedCorners)

        val h = origImg.rows()
        val w = origImg.cols()
        val srcMat = MatOfPoint2f(*sortedCorners)
        val dstMat = MatOfPoint2f(*destinationCorners)
        val M = Imgproc.getPerspectiveTransform(srcMat, dstMat)
        val final = Mat()
        Imgproc.warpPerspective(origImg, final, M, Size(destinationCorners[2].x, destinationCorners[2].y), Imgproc.INTER_LINEAR)
        return final */
    }

    private fun prepareImageForProcessing(image: Mat): Mat {
        val maxDim = maxOf(image.rows(), image.cols())
        val img = image.clone()

        // limit dimensions
        if (maxDim > DIM_LIMIT) {
            val resizeScale = DIM_LIMIT.toDouble() / maxDim
            Imgproc.resize(img, img, Size(), resizeScale, resizeScale, Imgproc.INTER_AREA)
        }

        // Convert to greyscale
        if (img.channels() == 4) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2GRAY)
        } else if (img.channels() == 3) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY)
        }

        // Rotate by 90 degrees if needed
        val img2 = img.clone()
        if (ALLOW_IMAGE_ROTATION && img.cols() > img.rows()) {
           // Core.rotate(img, img2, Core.ROTATE_90_CLOCKWISE)
        }

        return img2
    }

    /**
     * Removes the background of the image using GrabCut algorithm.
     * It assumes that the document is centered and has a white background.
     */
    private fun removeBackground(img: Mat): Mat {
        val mask = Mat.zeros(img.size(), CvType.CV_8UC1)
        val backgroundModel = Mat.zeros(1, 65, CvType.CV_64FC1)
        val foregroundModel = Mat.zeros(1, 65, CvType.CV_64FC1)

        // Define the rectangle for GrabCut, in which the document is expected to be.
        val xOffset = (img.cols() - GRAB_CUT_RECT_X_SIZE) / 2
        val yOffset = (img.rows() - GRAB_CUT_RECT_Y_SIZE) / 2
        val rect = Rect(xOffset, yOffset, img.cols() - 2*xOffset, img.rows() - 2*yOffset)

        Imgproc.grabCut(img, mask, rect, backgroundModel, foregroundModel, GRAB_CUT_ITERATIONS, Imgproc.GC_INIT_WITH_RECT)

        val resultCompareToZero = Mat(mask.rows(), mask.cols(), CvType.CV_8UC1)
        val resultCompareToTwo = Mat(mask.rows(), mask.cols(), CvType.CV_8UC1)
        val mask2 = Mat(mask.rows(), mask.cols(), CvType.CV_8UC1)

        Core.compare(mask, Scalar(2.0), resultCompareToTwo, Core.CMP_EQ)
        Core.compare(mask, Scalar(0.0), resultCompareToZero, Core.CMP_EQ)
        // Combine the results of the comparisons and limit the mask's values to 1.0
        Core.max(resultCompareToZero, resultCompareToTwo, mask2)
        Core.min(mask2, Scalar(1.0), mask2)


        // Debug
        Log.i("CameraActivity12341234", "Log debugging GrabCut results:")
        //Log.i("CameraActivity12341234", "resultCompareToZero: ${resultCompareToZero.dump()}")
        //Log.i("CameraActivity12341234", "resultCompareToTwo: ${resultCompareToTwo.dump()}")
        //Log.i("CameraActivity12341234", "GrabCut mask: ${mask.dump()}")
        //Log.i("CameraActivity12341234", "GrabCut mask2: ${mask2.dump()}")

        // Apply the mask to the original image
        val maskedImage = img.clone()
        Core.copyTo(img, maskedImage, mask2)



        return maskedImage
    }

    /**
     * Removes the content of the document by applying morphological closing.
     */
    private fun removeDocumentContent(img: Mat) {
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(MORPH_KERNEL_SIZE, MORPH_KERNEL_SIZE))
        Imgproc.morphologyEx(img, img, Imgproc.MORPH_CLOSE, kernel, Point(-1.0, -1.0), MORPH_ITERATIONS)
    }

    /*
    private fun orderPoints(points: MatOfPoint2f): Array<Point> {
        val pts = points.toArray()
        val sum = pts.map { it.x + it.y }
        val diff = pts.map { it.y - it.x }
        val ordered = Array(4) { Point() }
        ordered[0] = pts[sum.indexOf(sum.minOrNull()!!)] // top-left
        ordered[2] = pts[sum.indexOf(sum.maxOrNull()!!)] // bottom-right
        ordered[1] = pts[diff.indexOf(diff.minOrNull()!!)] // top-right
        ordered[3] = pts[diff.indexOf(diff.maxOrNull()!!)] // bottom-left
        return ordered
    }

    // Helper to find destination points for perspective transform
    private fun findDest(corners: Array<Point>): Array<Point> {
        val widthA = Math.hypot((corners[2].x - corners[3].x), (corners[2].y - corners[3].y))
        val widthB = Math.hypot((corners[1].x - corners[0].x), (corners[1].y - corners[0].y))
        val maxWidth = Math.max(widthA, widthB).toInt()
        val heightA = Math.hypot((corners[1].x - corners[2].x), (corners[1].y - corners[2].y))
        val heightB = Math.hypot((corners[0].x - corners[3].x), (corners[0].y - corners[3].y))
        val maxHeight = Math.max(heightA, heightB).toInt()
        return arrayOf(
            Point(0.0, 0.0),
            Point(maxWidth - 1.0, 0.0),
            Point(maxWidth - 1.0, maxHeight - 1.0),
            Point(0.0, maxHeight - 1.0)
        )
    } */
}