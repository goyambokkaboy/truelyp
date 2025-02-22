package com.ulyp.ui.elements.recording.tree

import com.ulyp.storage.CallRecord
import com.ulyp.storage.Recording
import com.ulyp.ui.RenderSettings
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.TreeItem
import java.util.function.Consumer

class RecordingTreeNode(private val recording: Recording, private val callRecordId: Long, private val renderSettings: RenderSettings) :
        TreeItem<RecordingTreeNodeContent>(
                RecordingTreeNodeContent(
                        recording.getCallRecord(callRecordId),
                        renderSettings,
                        recording.callCount()
                )
        ) {

    private var loaded = false
    private var currentCallRecord: CallRecord = recording.getCallRecord(callRecordId)

    init {
        this.addEventHandler(branchCollapsedEvent(), EventHandler<TreeModificationEvent<RecordingTreeNodeContent>> {
            val s = it.treeItem as RecordingTreeNode
            s.unloadChildren()
        })
    }

    fun refresh() {
        currentCallRecord = recording.getCallRecord(callRecordId)
        value = RecordingTreeNodeContent(currentCallRecord, renderSettings, recording.callCount())

        if (loaded) {
            val newChildren = currentCallRecord.childrenCallIds
            val currentLoadedChildrenCount = children.size
            if (newChildren.size > currentLoadedChildrenCount) {
                for (i in currentLoadedChildrenCount until newChildren.size) {
                    children.add(RecordingTreeNode(recording, newChildren.getLong(i), renderSettings))
                }
            }
            children.forEach(Consumer { node: TreeItem<RecordingTreeNodeContent> -> (node as RecordingTreeNode).refresh() })
        }
    }

    override fun getChildren(): ObservableList<TreeItem<RecordingTreeNodeContent>> {
        if (!loaded) {
            loadChildren()
        }
        return super.getChildren()
    }

    override fun isLeaf(): Boolean {
        return if (loaded) {
            children.isEmpty()
        } else {
            currentCallRecord.childrenCallIds.isEmpty()
        }
    }

    private fun loadChildren() {
        val children: MutableList<RecordingTreeNode> = ArrayList()
        val childrenIds = currentCallRecord.childrenCallIds
        for (i in childrenIds.indices) {
            children.add(RecordingTreeNode(recording, childrenIds.getLong(i), renderSettings))
        }
        super.getChildren().setAll(children)
        loaded = true
    }

    private fun unloadChildren() {
        super.getChildren().setAll(ArrayList())
        loaded = false
    }

    val callRecord: CallRecord
        get() {
            return currentCallRecord
        }

    override fun toString(): String {
        return "FxCallRecord{" +
                "node=" + callRecord +
                '}'
    }
}