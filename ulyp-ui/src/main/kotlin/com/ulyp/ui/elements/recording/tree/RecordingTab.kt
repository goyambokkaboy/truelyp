package com.ulyp.ui.elements.recording.tree

import com.ulyp.core.ProcessMetadata
import com.ulyp.core.RecordingMetadata
import com.ulyp.storage.CallRecord
import com.ulyp.storage.Recording
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.code.SourceCode
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.code.find.SourceCodeFinder
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Settings
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Component
@Scope(value = "prototype")
class RecordingTab(
        private val parent: Region,
        private val processMetadata: ProcessMetadata,
        private val recording: Recording
) : Tab() {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss.SSS")

    private var root: CallRecord? = null
    private var recordingMetadata: RecordingMetadata? = null
    private var treeView: TreeView<RecordingTreeNodeContent>? = null

    @Autowired
    private lateinit var sourceCodeView: SourceCodeView

    @Autowired
    private lateinit var renderSettings: RenderSettings

    @Autowired
    private lateinit var settings: Settings

    private var initialized = false

    @Synchronized
    fun init() {
        if (initialized) {
            return
        }

        treeView = TreeView(RecordingTreeNode(recording, root!!.id, renderSettings))

        treeView!!.prefHeightProperty().bind(parent.heightProperty())
        treeView!!.prefWidthProperty().bind(parent.widthProperty())

        val sourceCodeFinder = SourceCodeFinder(processMetadata.classPathFiles)
        treeView!!.selectionModel.selectedItemProperty()
                .addListener { observable: ObservableValue<out TreeItem<RecordingTreeNodeContent>?>?, oldValue: TreeItem<RecordingTreeNodeContent>?, newValue: TreeItem<RecordingTreeNodeContent>? ->
                    val selectedNode = newValue as RecordingTreeNode?
                    if (selectedNode?.callRecord != null) {
                        val sourceCodeFuture = sourceCodeFinder.find(
                                selectedNode.callRecord.method.declaringType.name
                        )
                        sourceCodeFuture.thenAccept { sourceCode: SourceCode? ->
                            Platform.runLater {
                                val currentlySelected = treeView!!.selectionModel.selectedItem
                                val currentlySelectedNode = currentlySelected as RecordingTreeNode
                                if (selectedNode.callRecord.id == currentlySelectedNode.callRecord.id) {
                                    sourceCodeView.setText(sourceCode, currentlySelectedNode.callRecord.method.name)
                                }
                            }
                        }
                    }
                }
        treeView!!.onKeyPressed = EventHandler { key: KeyEvent ->
            if (key.code == KeyCode.EQUALS) {
                settings.increaseFont()
            }
            if (key.code == KeyCode.MINUS) {
                settings.decreaseFont()
            }
        }
        text = tabName
        content = treeView
        onClosed = EventHandler { ev: Event? -> dispose() }
        tooltip = tooltipText
        initialized = true
    }

    @get:Synchronized
    val tabName: String
        get() = if (root == null || recordingMetadata == null) {
            "?"
        } else Timestamp(recordingMetadata!!.recordingStartedEpochMillis).toLocalDateTime().format(dateTimeFormatter) + " " +
                recordingMetadata!!.threadName + " " +
                toSimpleName(root!!.method.declaringType.name) +
                "." + root!!.method.name +
                "(" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")"

    @get:Synchronized
    private val tooltipText: Tooltip
        private get() {
            if (root == null || recordingMetadata == null) {
                return Tooltip("")
            }
            val builder = StringBuilder()
                    .append("Thread: ").append(recordingMetadata!!.threadName).append("\n")
                    .append("Created at: ").append(Timestamp(recordingMetadata!!.recordingStartedEpochMillis)).append("\n")
                    .append("Finished at: ")
                    .append(Timestamp(recordingMetadata!!.recordingCompletedEpochMillis)).append("\n")
                    .append("Lifetime: ").append(recording.lifetime.toMillis()).append(" millis").append("\n")

            return Tooltip(builder.toString())
        }

    fun getSelected(): RecordingTreeNode {
        return treeView!!.selectionModel.selectedItem as RecordingTreeNode
    }

    fun dispose() {
    }

    @Synchronized
    fun refreshTreeView() {
        init()
        val root = treeView!!.root as RecordingTreeNode
        text = tabName
        tooltip = tooltipText
        root.refresh()
    }

    @Synchronized
    fun update(recording: Recording) {
        if (recordingMetadata == null) {
            recordingMetadata = recording.metadata
        }
        if (root == null) {
            root = recording.root
        }
    }
}