package com.naposystems.pepito.ui.actionMode

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import com.naposystems.pepito.R

class ActionModeMenu(private val clickDelete: (Boolean) -> Unit,
                     private val clickCopy: (Boolean) -> Unit,
                     private val clickBack: (Boolean) -> Unit) : ActionMode.Callback {

    var mode: ActionMode? = null
    var hideCopyButton = false
    var quantityMessageOtherUser = 0

    @MenuRes
    private var menuResId :  Int  =  0

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                if(quantityMessageOtherUser > 0){
                    clickDelete(true)
                }
                else {
                    clickDelete(false)
                }
            }
            R.id.copy -> {
                clickCopy(true)
            }
        }
        return true
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        this.mode = mode
        mode!!.menuInflater.inflate(menuResId, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val item = menu?.findItem(R.id.copy)
        item?.isVisible = !hideCopyButton
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        clickBack(true)
        this.mode = null
    }

    fun changeTitle (text : String) {
        mode?.title = text
    }

    fun startActionMode(view: View?, @MenuRes menuResId: Int) {
        this.menuResId = menuResId
        view!!.startActionMode(this)
    }

}