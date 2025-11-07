@file:OptIn(ExperimentalFoundationApi::class)

package edu.farmingdale.draganddropanim_demo

import kotlin.math.roundToInt
import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DragAndDropBoxes(modifier: Modifier = Modifier) {
    // Controls whether the rectangle is currently rotating
    var isPlaying by remember { mutableStateOf(true) }

    // Stores the current position (offset) of the rectangle
    var targetOffset by remember { mutableStateOf(IntOffset(130, 100)) }

    // Will hold the red container’s pixel size (for boundaries)
    var containerSizePx by remember { mutableStateOf(IntSize.Zero) }

    // Density used to convert between pixels and dp units
    val density = LocalDensity.current

    // Rectangle dimensions (in dp)
    val rectW = 120
    val rectH = 60

    // How far the rectangle moves per drop
    val moveStepDp = 60

    // Rotation direction: 1 = clockwise, -1 = counterclockwise
    var rotationDir by remember { mutableIntStateOf(1) }

    // Layout structure: Column containing controls (Row) + red container
    Column(modifier = Modifier.fillMaxSize()) {

        // ───────────────────────────────────────────────
        // Top section: 4 drag-and-drop control boxes
        // Each box moves the rectangle in one direction.
        // ───────────────────────────────────────────────
        Row(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.2f)
        ) {
            val boxCount = 4
            var dragBoxIndex by remember { mutableIntStateOf(0) }

            // Create 4 equally spaced boxes: Up, Right, Down, Left
            repeat(boxCount) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp)
                        .border(1.dp, Color.Black)
                        .dragAndDropTarget(
                            // Only start DnD if it’s a text MIME type
                            shouldStartDragAndDrop = { event ->
                                event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = remember {
                                // Defines behavior when an item is dropped on this box
                                object : DragAndDropTarget {
                                    override fun onDrop(event: DragAndDropEvent): Boolean {
                                        // Map boxes (left→right): 0=Up, 1=Right, 2=Down, 3=Left
                                        val (dx, dy) = when (index) {
                                            0 -> 0 to -moveStepDp
                                            1 -> moveStepDp to 0
                                            2 -> 0 to moveStepDp
                                            else -> -moveStepDp to 0
                                        }

                                        // Convert container px → dp for movement clamping
                                        val cwDp = containerSizePx.width / density.density
                                        val chDp = containerSizePx.height / density.density

                                        // Clamp so rectangle stays inside red box
                                        val maxX = (cwDp - rectW).coerceAtLeast(0f).roundToInt()
                                        val maxY = (chDp - rectH).coerceAtLeast(0f).roundToInt()

                                        // Apply movement and clamp values
                                        val nx = (targetOffset.x + dx).coerceIn(0, maxX)
                                        val ny = (targetOffset.y + dy).coerceIn(0, maxY)
                                        targetOffset = IntOffset(nx, ny)

                                        // Choose spin direction based on which box was hit
                                        rotationDir = when (index) {
                                            0 -> -1 // Up = counterclockwise
                                            2 ->  1 // Down = clockwise
                                            else -> rotationDir
                                        }

                                        // Start rotation after movement
                                        isPlaying = true

                                        // Highlight the active box
                                        dragBoxIndex = index
                                        return true
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Displays direction arrow inside each control box
                    val label = when (index) {
                        0 -> "↑"
                        1 -> "→"
                        2 -> "↓"
                        else -> "←"
                    }

                    Text(
                        text = label,
                        fontSize = 40.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    // The draggable “hand” image appears on the active box
                    this@Row.AnimatedVisibility(
                        visible = index == dragBoxIndex,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hand2),
                            contentDescription = "Hand image",
                            modifier = Modifier
                                .fillMaxSize()
                                .dragAndDropSource {
                                    // When user long-presses, begin drag operation
                                    detectTapGestures(
                                        onLongPress = {
                                            startTransfer(
                                                transferData = DragAndDropTransferData(
                                                    clipData = ClipData.newPlainText("text", "")
                                                )
                                            )
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // Smoothly animate the rectangle’s position offset
        // ───────────────────────────────────────────────
        val pOffset by animateIntOffsetAsState(
            targetValue = targetOffset,
            animationSpec = tween(3000, easing = LinearEasing)
        )

        // ───────────────────────────────────────────────
        // Animate rectangle rotation (360° loop)
        // Direction depends on rotationDir
        // ───────────────────────────────────────────────
        val rtatView by animateFloatAsState(
            targetValue = if (isPlaying) 360f * rotationDir else 0f,
            animationSpec = repeatable(
                iterations = if (isPlaying) 10 else 1,
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rect-rotation"
        )

        // ───────────────────────────────────────────────
        // Bottom red container — holds the blue rectangle
        // ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .background(Color.Red)
                // Captures red container’s size in pixels
                .onSizeChanged { size -> containerSizePx = size }
        ) {
            // Compute the center of the red container in dp units
            val cwDp = containerSizePx.width / density.density
            val chDp = containerSizePx.height / density.density
            val centerX = ((cwDp - rectW) / 2f).roundToInt()
            val centerY = ((chDp - rectH) / 2f).roundToInt()

            // Button to reset the rectangle’s position to center
            Button(
                onClick = {
                    isPlaying = false // stop rotation
                    targetOffset = IntOffset(centerX, centerY) // move to center
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Text("Reset to Center")
            }

            // The animated blue rectangle that moves and rotates
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(width = rectW.dp, height = rectH.dp)
                    .offset(pOffset.x.dp, pOffset.y.dp)
                    .rotate(rtatView)
                    .background(Color.Blue)
            )
        }
    }
}
