@file:OptIn(ExperimentalFoundationApi::class)

package edu.farmingdale.draganddropanim_demo

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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DragAndDropBoxes(modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(true) }
    var targetOffset by remember { mutableStateOf(IntOffset(130, 100)) }
    var containerSizePx by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top drag targets ───────────────────────────────────────────────────────
        Row(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.2f)
        ) {
            val boxCount = 4
            var dragBoxIndex by remember { mutableIntStateOf(0) }

            repeat(boxCount) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp)
                        .border(1.dp, Color.Black)
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { event ->
                                event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = remember {
                                object : DragAndDropTarget {
                                    override fun onDrop(event: DragAndDropEvent): Boolean {
                                        isPlaying = !isPlaying
                                        dragBoxIndex = index
                                        return true
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    this@Row.AnimatedVisibility(
                        visible = index == dragBoxIndex,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hand2),
                            contentDescription = "Right hand image",
                            modifier = Modifier
                                .fillMaxSize()
                                .dragAndDropSource {
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

        // ── Position animation (offset) ───────────────────────────────────────────
        val pOffset by animateIntOffsetAsState(
            targetValue = targetOffset,
            animationSpec = tween(3000, easing = LinearEasing)
        )

        // ── Rotation animation ────────────────────────────────────────────────────
        val rtatView by animateFloatAsState(
            targetValue = if (isPlaying) 360f else 0f,
            animationSpec = repeatable(
                iterations = if (isPlaying) 10 else 1,
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rect-rotation"
        )

        // ── Main red area (captures its size) ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .background(Color.Red)
                .onSizeChanged { size -> containerSizePx = size } // capture size in px
        ) {
            val rectW = 120  // dp
            val rectH = 60   // dp

            // Convert px -> dp as floats (no .value usage)
            val containerWidthDp  = containerSizePx.width  / density.density
            val containerHeightDp = containerSizePx.height / density.density

            val centerX = ((containerWidthDp  - rectW) / 2f).roundToInt()
            val centerY = ((containerHeightDp - rectH) / 2f).roundToInt()

            // Reset button (correct scope: inside a Box, so full Alignment is valid)
            Button(
                onClick = {
                    isPlaying = false
                    targetOffset = IntOffset(centerX, centerY)
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Text("Reset to Center")
            }

            // The animated rectangle
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
