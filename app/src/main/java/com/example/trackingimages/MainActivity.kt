package com.example.trackingimages


import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    //wassa
    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null
    private var modelRenderable: ModelRenderable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFrag = supportFragmentManager.findFragmentById(R.id.fragArImg) as ArFragment
        val uri = Uri.parse("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf")
        val renderableFuture = ModelRenderable.builder().setSource(this, RenderableSource.builder().setSource(this, uri, RenderableSource.SourceType.GLTF2)
            .setScale(0.2f) // Scale the original to 20%.
            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
            .build())
            .setRegistryId("Duck").build()
        renderableFuture.thenAccept { modelRenderable = it }
        arFrag.arSceneView.scene.addOnUpdateListener { frameUpdate() }
    }

    private fun frameUpdate() {
        val fitToScanImg = findViewById<ImageView>(R.id.fitToScanImg)
        val arFrame = arFrag.arSceneView.arFrame
        if (arFrame != null) {
            if (arFrame.camera.trackingState != TrackingState.TRACKING) return
        }

        arFrame?.getUpdatedTrackables(AugmentedImage::class.java)?.forEach {
            when (it.trackingState) {
                null -> return@forEach

                TrackingState.PAUSED -> {
                    // Image initially detected, but not enough data available to estimate its location in 3D space.
                    // Do not use the image's pose and size estimates until the image's tracking state is tracking
                    val text = "${R.string.detected_img_need_more_info} ${it.name}"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                TrackingState.STOPPED -> {
                    val text = "${R.string.track_stop} ${it.name}"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                TrackingState.TRACKING -> {
                    val anchors = it.anchors
                    if (anchors.isEmpty()) {
                        fitToScanImg.visibility = View.GONE
                        // Create anchor and anchor node in the center of the image.
                        val pose = it.centerPose
                        val anchor = it.createAnchor(pose)
                        val anchorNode = AnchorNode(anchor)
                        //Attach anchor node in the scene
                        anchorNode.setParent(arFrag.arSceneView.scene)
                        // Create a node as a child node of anchor node, and define node's renderable according to augmented image
                        val mNode = TransformableNode(arFrag.transformationSystem)
                        mNode.setParent(anchorNode)

                            if(it.name == "ree"){
                                mNode.renderable = modelRenderable
                                mNode.select()
                            }

                    }

                }
            }
        }
    }
}