package edu.farmingdale.draganddropanim_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.farmingdale.draganddropanim_demo.ui.theme.DragAndDropAnim_DemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DragAndDropAnim_DemoTheme {

                DragAndDropBoxes()


            }
        }
    }
}



// This should be completed in a group setting
// ToDo x: Analyze the requirements for Individual Project 3
// ToDo x: Show the DragAndDropBoxes composable
// ToDo x: Change the circle to a rect
// ToDo x: Replace the command right with a image or icon
// ToDo x: Make this works in landscape mode only
// ToDo x: Rotate the rect around itself
// ToDo x: Move - translate the rect horizontally and vertically
// ToDo 8: Add a button to reset the rect to the center of the screen
// ToDo 9: Enable certain animation based on the drop event (like up or down)
// ToDo 10: Make sure to commit for each one of the above and submit this individually